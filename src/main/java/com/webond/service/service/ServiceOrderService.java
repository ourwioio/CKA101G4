package com.webond.service.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.repository.MemberRepository;
import com.webond.member.service.NotificationService;
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.model.ServiceSlotVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.repository.ServiceOrderRepository;
import com.webond.service.repository.ServiceRepository;
import com.webond.service.repository.ServiceSlotRepository;

@Service
@Transactional
public class ServiceOrderService {

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;
	
	//評價查詢
	 public List<ServiceOrderVO> getReviewsByServiceId(Integer serviceId) {
	        return serviceOrderRepository
	            .findByServiceIdAndBuyerRateSellerIsNotNullOrderByBuyerReviewedAtDesc(serviceId);
	    }

	 public Double getAverageRating(Integer serviceId) {
		 	List<ServiceOrderVO> list = getReviewsByServiceId(serviceId);
	        if (list.isEmpty()) return 0.0;
	        return list.stream()
	                .mapToInt(ServiceOrderVO::getBuyerRateSeller)
	                .average()
	                .orElse(0.0);
	 }

	// =========================================================
	// 訂單狀態
	// =========================================================

	// 0：待賣家確認
	private static final byte ORDER_PENDING_SELLER_CONFIRM = 0;

	// 1：待買家付款
	private static final byte ORDER_PENDING_PAYMENT = 1;

	// 2：已成立
	private static final byte ORDER_CONFIRMED = 2;

	// 3：已完成
	private static final byte ORDER_COMPLETED = 3;

	// 4：已取消
	private static final byte ORDER_CANCELLED = 4;

	// =========================================================
	// 時段狀態
	// =========================================================

	// 0：可預約
	private static final byte SLOT_AVAILABLE = 0;

	// 1：暫時鎖定
	private static final byte SLOT_LOCKED = 1;

	// 2：已預約
	private static final byte SLOT_BOOKED = 2;

	// =========================================================
	// 退款狀態
	// =========================================================

	// 0：無退款
	private static final byte REFUND_NONE = 0;

	// 1：待退款
	private static final byte REFUND_PENDING = 1;

	// 2：已退款
	private static final byte REFUND_COMPLETED = 2;

	// =========================================================
	// 撥款狀態
	// =========================================================

	// 0：未撥款
	private static final byte PAYOUT_UNPAID = 0;

	// 1：已撥款
	private static final byte PAYOUT_PAID = 1;

	// =========================================================
	// 取消方角色
	// =========================================================

	// 0：買家取消
	private static final byte CANCEL_BY_BUYER = 0;

	// 1：賣家取消
	private static final byte CANCEL_BY_SELLER = 1;

	// 2：後台取消
	private static final byte CANCEL_BY_ADMIN = 2;

	// 3：系統取消
	private static final byte CANCEL_BY_SYSTEM = 3;

	// =========================================================
	// 期限設定
	// =========================================================

	// 測試用：賣家有 180 秒確認申請
	private static final long SELLER_CONFIRM_SECONDS = 180;

	// 測試用：買家有 60 秒付款
	private static final long BUYER_PAYMENT_SECONDS = 60;

	// =========================================================
	// 通知標題
	//
	// 前端通知列表會依照這些標題，
	// 決定導向買家訂單頁或賣家訂單頁。
	// =========================================================

	// 賣家收到的通知
	private static final String NOTIFY_SELLER_NEW_REQUEST =
			"收到新的服務預約申請";

	private static final String NOTIFY_SELLER_PAYMENT_COMPLETED =
			"服務訂單付款完成";

	private static final String NOTIFY_SELLER_BUYER_CANCELLED =
			"服務訂單已由買家取消";

	private static final String NOTIFY_SELLER_BUYER_PAYMENT_EXPIRED =
			"服務訂單買家付款逾期";

	private static final String NOTIFY_SELLER_PLATFORM_CANCELLED =
			"平台已取消服務訂單";

	// 買家收到的通知
	private static final String NOTIFY_BUYER_REQUEST_ACCEPTED =
			"服務預約申請已通過";

	private static final String NOTIFY_BUYER_REQUEST_REJECTED =
			"服務預約申請未通過";

	private static final String NOTIFY_BUYER_SLOT_TAKEN =
			"服務預約時段已被其他會員預約";

	private static final String NOTIFY_BUYER_SELLER_CANCELLED =
			"服務訂單已由賣家取消";

	private static final String NOTIFY_BUYER_PLATFORM_CANCELLED =
			"平台已取消服務預約";

	private static final String NOTIFY_BUYER_REFUND_COMPLETED =
			"服務退款完成";

	private static final String NOTIFY_BUYER_REQUEST_EXPIRED =
			"服務預約申請已逾期";

	private static final String NOTIFY_BUYER_PAYMENT_EXPIRED =
			"服務付款期限已過";

	private final ServiceOrderRepository orderRepo;
	private final ServiceRepository serviceRepo;
	private final ServiceSlotRepository slotRepo;
	private final SlotStatusWebSocketService slotStatusWebSocketService;
	private final MemberRepository memberRepo;
	private final NotificationService notificationService;

	public ServiceOrderService(ServiceOrderRepository orderRepo,
			ServiceRepository serviceRepo,
			ServiceSlotRepository slotRepo,
			SlotStatusWebSocketService slotStatusWebSocketService,
			MemberRepository memberRepo,
			NotificationService notificationService) {

		this.orderRepo = orderRepo;
		this.serviceRepo = serviceRepo;
		this.slotRepo = slotRepo;
		this.slotStatusWebSocketService = slotStatusWebSocketService;
		this.memberRepo = memberRepo;
		this.notificationService = notificationService;
	}

	// =========================================================
	// 買家送出預約申請
	//
	// 此時不鎖定時段。
	// 賣家必須在 180 秒內確認。
	// =========================================================

	public ServiceOrderVO createRequest(
			Integer slotId,
			Integer buyerId,
			String buyerRequestNote) {

		if (buyerId == null) {
			throw new RuntimeException("請先登入");
		}
		
		ServiceSlotVO slot =
		        getSlotOrThrow(slotId);

		LocalDateTime now =
		        LocalDateTime.now();

		// 已經開始或時間異常的時段，不接受申請
		if (slot.getStartTime() == null
		        || !slot.getStartTime().isAfter(now)) {

		    throw new RuntimeException(
		            "此服務時段已經開始或已過期"
		    );
		}

		if (!Byte.valueOf(SLOT_AVAILABLE)
		        .equals(slot.getSlotStatus())) {

		    throw new RuntimeException(
		            "此時段目前不可申請"
		    );
		}
		
		ServiceVO service = serviceRepo
				.findById(slot.getServiceId())
				.orElseThrow(
						() -> new RuntimeException("找不到服務")
				);

		if (service.getMemberId() == null) {
			throw new RuntimeException("服務缺少賣家資料");
		}

		if (service.getMemberId().equals(buyerId)) {
			throw new RuntimeException("不能預約自己的服務");
		}


		ServiceOrderVO order = new ServiceOrderVO();

		// -----------------------------------------------------
		// 原始關聯資料
		// -----------------------------------------------------

		order.setServiceSlotId(
				slot.getServiceSlotId()
		);

		order.setServiceId(
				service.getServiceId()
		);

		order.setBuyerMemberId(
				buyerId
		);

		// -----------------------------------------------------
		// 訂單快照
		// -----------------------------------------------------

		order.setSellerMemberId(
				service.getMemberId()
		);

		order.setServiceNameSnapshot(
				service.getServiceName()
		);

		order.setServiceDescriptionSnapshot(
				service.getDescription()
		);

		if (service.getServiceType() != null
				&& service.getServiceType().getTypeName() != null) {

			order.setServiceTypeNameSnapshot(
					service.getServiceType().getTypeName()
			);

		} else {

			order.setServiceTypeNameSnapshot(
					"未分類"
			);
		}

		order.setSlotStartTimeSnapshot(
				slot.getStartTime()
		);

		order.setSlotEndTimeSnapshot(
				slot.getEndTime()
		);

		order.setServiceCitySnapshot(
				service.getServiceCity()
		);

		order.setServiceDistrictSnapshot(
				service.getServiceDistrict()
		);

		order.setServiceLocationSnapshot(
				service.getServiceLocation()
		);

		// -----------------------------------------------------
		// 金額快照
		// -----------------------------------------------------

		order.setOrderHourlyRate(
				service.getHourlyRate()
		);

		order.setTotalAmount(
				calculateTotalAmount(service, slot)
		);

		// -----------------------------------------------------
		// 初始狀態
		// -----------------------------------------------------

		order.setOrderStatus(
				ORDER_PENDING_SELLER_CONFIRM
		);

		order.setBuyerRequestNote(
				normalizeNullableText(buyerRequestNote)
		);

		order.setCreatedAt(now);

		order.setPayoutStatus(
				PAYOUT_UNPAID
		);

		order.setRefundStatus(
				REFUND_NONE
		);

		order.setRefundAmount(0);

		// 賣家確認期限為現在時間加 180 秒
		order.setSellerConfirmExpiresAt(
				now.plusSeconds(SELLER_CONFIRM_SECONDS)
		);

		order.setPaymentExpiresAt(null);

		/*
		 * 此時不改 SERVICE_SLOT。
		 *
		 * 同一時段仍可讓其他買家提出申請。
		 * 等賣家接受其中一筆時才鎖定。
		 */

		ServiceOrderVO savedOrder =
				orderRepo.save(order);

		// 通知賣方：收到新的預約申請
		sendNotification(
				savedOrder.getSellerMemberId(),
				NOTIFY_SELLER_NEW_REQUEST,
				"您的服務「"
						+ savedOrder.getServiceNameSnapshot()
						+ "」收到新的預約申請，預約時段為 "
						+ formatOrderSlot(savedOrder)
						+ "，請前往收到的預約查看。"
		);

		return savedOrder;
	}

	// =========================================================
	// 賣家接受其中一筆申請
	//
	// 1. 選中的訂單進入待付款
	// 2. 時段暫時鎖定
	// 3. 其他相同時段的申請先保留
	// 4. 等買家付款成功後，才取消其他申請
	// =========================================================

	public ServiceOrderVO acceptRequest(
			Integer orderId,
			String sellerRequirementNote) {

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_PENDING_SELLER_CONFIRM)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"此訂單不是待賣家確認狀態"
			);
		}

		LocalDateTime now =
				LocalDateTime.now();

		LocalDateTime sellerExpiresAt =
				order.getSellerConfirmExpiresAt();

		if (sellerExpiresAt != null
				&& !now.isBefore(sellerExpiresAt)) {

			throw new RuntimeException(
					"賣家確認期限已過"
			);
		}

		ServiceSlotVO slot =
				getSlotOrThrow(
						order.getServiceSlotId()
				);

		if (!Byte.valueOf(SLOT_AVAILABLE)
				.equals(slot.getSlotStatus())) {

			throw new RuntimeException(
					"此時段已有其他買家正在付款或已完成預約"
			);
		}

		LocalDateTime paymentExpiresAt =
				now.plusSeconds(
						BUYER_PAYMENT_SECONDS
				);

		// 選中的訂單進入待付款
		order.setOrderStatus(
				ORDER_PENDING_PAYMENT
		);

		order.setSellerRequirementNote(
				normalizeNullableText(
						sellerRequirementNote
				)
		);

		// 賣家已完成確認
		order.setSellerConfirmExpiresAt(null);

		// 設定付款期限
		order.setPaymentExpiresAt(
				paymentExpiresAt
		);

		// 時段暫時鎖定
		slot.setSlotStatus(
				SLOT_LOCKED
		);

		slot.setLockExpiresAt(
				paymentExpiresAt
		);

		slotRepo.save(slot);

		ServiceOrderVO savedOrder =
				orderRepo.save(order);

		// 通知買方：賣方已接受申請，請完成付款
		sendNotification(
				savedOrder.getBuyerMemberId(),
				NOTIFY_BUYER_REQUEST_ACCEPTED,
				"賣家已同意您對服務「"
						+ savedOrder.getServiceNameSnapshot()
						+ "」的預約申請，請於付款期限內完成付款。預約時段為 "
						+ formatOrderSlot(savedOrder)
		);

		/*
		 * 注意：
		 * 這裡不再取消其他相同時段的申請。
		 *
		 * 其他申請維持：
		 * ORDER_STATUS = 0 待賣家確認
		 *
		 * 如果目前買家付款逾期，
		 * 時段釋放後，賣家還可以接受其他申請。
		 */

		// WebSocket：通知前端此時段暫時鎖定
		publishSlotStatus(
				savedOrder,
				SLOT_LOCKED
		);

		return savedOrder;
	}

	// =========================================================
	// 賣家拒絕申請
	//
	// 買家尚未付款，因此不需要退款。
	// =========================================================

	public ServiceOrderVO rejectRequest(
			Integer orderId,
			String reason) {

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_PENDING_SELLER_CONFIRM)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"只有待賣家確認的申請可以拒絕"
			);
		}

		LocalDateTime now =
				LocalDateTime.now();

		order.setOrderStatus(
				ORDER_CANCELLED
		);

		order.setCancelledByRole(
				CANCEL_BY_SELLER
		);

		order.setCancelReason(
				normalizeCancelReason(
						reason,
						"賣家拒絕預約申請"
				)
		);

		order.setCancelledAt(now);

		order.setSellerConfirmExpiresAt(null);
		order.setPaymentExpiresAt(null);

		order.setRefundStatus(
				REFUND_NONE
		);

		order.setRefundAmount(0);

		order.setPayoutStatus(
				PAYOUT_UNPAID
		);

		order.setHandledAt(null);

		/*
		 * 買家申請時沒有鎖定時段，
		 * 所以拒絕時不用修改 SERVICE_SLOT。
		 */

		ServiceOrderVO savedOrder =
				orderRepo.save(order);

		// 通知買家：賣家拒絕預約申請
		sendNotification(
				savedOrder.getBuyerMemberId(),
				NOTIFY_BUYER_REQUEST_REJECTED,
				"賣家未接受您對服務「"
						+ savedOrder.getServiceNameSnapshot()
						+ "」的預約申請。預約時段為 "
						+ formatOrderSlot(savedOrder)
						+ "。原因："
						+ savedOrder.getCancelReason()
		);

		return savedOrder;
	}

	// =========================================================
	// 買家付款成功
	//
	// 1. 付款訂單變成已成立
	// 2. 時段變成已預約
	// 3. 取消其他相同時段仍在待確認的申請
	// 4. WebSocket 推播時段已預約
	// =========================================================

	public ServiceOrderVO paySuccess(
			Integer orderId,
			Byte paymentMethod) {

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_PENDING_PAYMENT)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"此訂單不是待付款狀態"
			);
		}

		validatePaymentMethod(paymentMethod);

		LocalDateTime now =
				LocalDateTime.now();

		LocalDateTime paymentExpiresAt =
				order.getPaymentExpiresAt();

		if (paymentExpiresAt != null
				&& !now.isBefore(paymentExpiresAt)) {

			throw new RuntimeException(
					"付款期限已過"
			);
		}

		ServiceSlotVO slot =
				getSlotOrThrow(
						order.getServiceSlotId()
				);

		if (!Byte.valueOf(SLOT_LOCKED)
				.equals(slot.getSlotStatus())) {

			throw new RuntimeException(
					"此時段目前不是待付款鎖定狀態"
			);
		}

		if (slot.getLockExpiresAt() != null
				&& !now.isBefore(
						slot.getLockExpiresAt()
				)) {

			throw new RuntimeException(
					"時段鎖定期限已過"
			);
		}

		// -----------------------------------------------------
		// 付款訂單正式成立
		// -----------------------------------------------------

		order.setOrderStatus(
				ORDER_CONFIRMED
		);

		order.setServicePaymentMethod(
				paymentMethod
		);

		order.setPaymentExpiresAt(null);

		// -----------------------------------------------------
		// 時段正式變成已預約
		// -----------------------------------------------------

		slot.setSlotStatus(
				SLOT_BOOKED
		);

		slot.setLockExpiresAt(null);

		slotRepo.save(slot);

		ServiceOrderVO savedOrder =
				orderRepo.save(order);

		// -----------------------------------------------------
		// 付款成功後，才取消其他相同時段的待確認申請
		// -----------------------------------------------------

		List<ServiceOrderVO> otherOrders =
				orderRepo
					.findByServiceSlotIdAndOrderStatusAndServiceOrderIdNot(
							order.getServiceSlotId(),
							ORDER_PENDING_SELLER_CONFIRM,
							order.getServiceOrderId()
					);

		for (ServiceOrderVO otherOrder : otherOrders) {

			otherOrder.setOrderStatus(
					ORDER_CANCELLED
			);

			otherOrder.setCancelledByRole(
					CANCEL_BY_SYSTEM
			);

			otherOrder.setCancelReason(
					"此時段已有其他買家完成付款，預約名額已被取得"
			);

			otherOrder.setCancelledAt(now);

			otherOrder.setSellerConfirmExpiresAt(null);
			otherOrder.setPaymentExpiresAt(null);

			// 尚未付款，不需要退款
			otherOrder.setRefundStatus(
					REFUND_NONE
			);

			otherOrder.setRefundAmount(0);

			otherOrder.setPayoutStatus(
					PAYOUT_UNPAID
			);

			otherOrder.setHandledAt(null);
		}

		if (!otherOrders.isEmpty()) {
			orderRepo.saveAll(otherOrders);
		}

		// 通知賣方：買方已完成付款
		sendNotification(
				savedOrder.getSellerMemberId(),
				NOTIFY_SELLER_PAYMENT_COMPLETED,
				"您的服務「"
						+ savedOrder.getServiceNameSnapshot()
						+ "」已有買家完成付款，訂單已正式成立。預約時段為 "
						+ formatOrderSlot(savedOrder)
		);

		// 通知其他申請人：此時段已由其他買家完成付款
		for (ServiceOrderVO otherOrder : otherOrders) {

			sendNotification(
					otherOrder.getBuyerMemberId(),
					NOTIFY_BUYER_SLOT_TAKEN,
					"您申請的服務「"
							+ otherOrder.getServiceNameSnapshot()
							+ "」時段 "
							+ formatOrderSlot(otherOrder)
							+ " 已由其他會員完成付款，因此您的預約申請已由系統取消。"
			);
		}

		// WebSocket：通知前端此時段已預約
		publishSlotStatus(
				savedOrder,
				SLOT_BOOKED
		);

		return savedOrder;
	}

	// =========================================================
	// 買家取消已成立訂單
	//
	// 距離開始超過 3 天：
	// 全額退款，進入待退款。
	//
	// 距離開始 3 天內：
	// 可以取消，但不退款。
	// =========================================================

	public ServiceOrderVO cancelByBuyer(
			Integer orderId,
			String reason) {

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_CONFIRMED)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"只有已成立訂單可以由買家取消"
			);
		}

		LocalDateTime now =
				LocalDateTime.now();

		LocalDateTime serviceStartTime =
				order.getSlotStartTimeSnapshot();

		if (serviceStartTime == null) {
			throw new RuntimeException(
					"訂單缺少服務開始時間"
			);
		}

		if (!serviceStartTime.isAfter(now)) {
			throw new RuntimeException(
					"服務已開始或已結束，無法取消"
			);
		}

		ServiceSlotVO slot =
				getSlotOrThrow(
						order.getServiceSlotId()
				);

		order.setOrderStatus(
				ORDER_CANCELLED
		);

		order.setCancelledByRole(
				CANCEL_BY_BUYER
		);

		order.setCancelReason(
				normalizeCancelReason(
						reason,
						"買家取消訂單"
				)
		);

		order.setCancelledAt(now);

		order.setSellerConfirmExpiresAt(null);
		order.setPaymentExpiresAt(null);

		order.setPayoutStatus(
				PAYOUT_UNPAID
		);

		// 嚴格超過 3 天才全額退款
		boolean refundable =
				serviceStartTime.isAfter(
						now.plusDays(3)
				);

		if (refundable) {

			order.setRefundStatus(
					REFUND_PENDING
			);

			order.setRefundAmount(
					order.getTotalAmount()
			);

		} else {

			order.setRefundStatus(
					REFUND_NONE
			);

			order.setRefundAmount(0);
		}

		order.setHandledAt(null);

		releaseSlot(slot);

		slotRepo.save(slot);

		ServiceOrderVO savedOrder =
				orderRepo.save(order);

		// 通知賣家：買家取消訂單
		String notificationContent =
				"買家已取消您提供的服務「"
				+ savedOrder.getServiceNameSnapshot()
				+ "」，預約時段為 "
				+ formatOrderSlot(savedOrder)
				+ "。取消原因："
				+ savedOrder.getCancelReason()
				+ "。時段已重新開放。";

		sendNotification(
				savedOrder.getSellerMemberId(),
				NOTIFY_SELLER_BUYER_CANCELLED,
				notificationContent
		);

		publishSlotStatus(
				savedOrder,
				SLOT_AVAILABLE
		);

		return savedOrder;
	}

	// =========================================================
	// 賣家取消訂單
	//
	// 可以取消：
	// 0 待賣家確認
	// 1 待買家付款
	// 2 已成立
	//
	// 已付款成立的訂單：
	// 已取消＋待退款。
	//
	// 尚未付款的訂單：
	// 已取消＋無退款。
	// =========================================================

	public ServiceOrderVO cancelBySeller(
			Integer orderId,
			String reason) {

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		Byte currentStatus =
				order.getOrderStatus();

		boolean pendingSellerConfirm =
				Byte.valueOf(ORDER_PENDING_SELLER_CONFIRM)
					.equals(currentStatus);

		boolean pendingPayment =
				Byte.valueOf(ORDER_PENDING_PAYMENT)
					.equals(currentStatus);

		boolean confirmed =
				Byte.valueOf(ORDER_CONFIRMED)
					.equals(currentStatus);

		if (!pendingSellerConfirm
				&& !pendingPayment
				&& !confirmed) {

			throw new RuntimeException(
					"此訂單目前不能由賣家取消"
			);
		}

		LocalDateTime now =
				LocalDateTime.now();

		order.setOrderStatus(
				ORDER_CANCELLED
		);

		order.setCancelledByRole(
				CANCEL_BY_SELLER
		);

		order.setCancelReason(
				normalizeCancelReason(
						reason,
						"賣家取消訂單"
				)
		);

		order.setCancelledAt(now);

		order.setSellerConfirmExpiresAt(null);
		order.setPaymentExpiresAt(null);

		order.setPayoutStatus(
				PAYOUT_UNPAID
		);

		if (confirmed) {

			// 已付款，取消後等待後台退款
			order.setRefundStatus(
					REFUND_PENDING
			);

			order.setRefundAmount(
					order.getTotalAmount()
			);

		} else {

			// 尚未付款，不需要退款
			order.setRefundStatus(
					REFUND_NONE
			);

			order.setRefundAmount(0);
		}

		order.setHandledAt(null);

		boolean slotReleased =
				pendingPayment || confirmed;

		/*
		 * 待付款時段為暫時鎖定。
		 * 已成立時段為已預約。
		 * 兩者取消後都必須釋放。
		 */
		if (slotReleased) {

			ServiceSlotVO slot =
					getSlotOrThrow(
							order.getServiceSlotId()
					);

			releaseSlot(slot);

			slotRepo.save(slot);
		}

		ServiceOrderVO savedOrder =
				orderRepo.save(order);

		if (slotReleased) {
			publishSlotStatus(
					savedOrder,
					SLOT_AVAILABLE
			);
		}

		// 通知買家：賣家取消訂單
		String notificationContent =
				"賣家已取消服務「"
				+ savedOrder.getServiceNameSnapshot()
				+ "」的訂單，預約時段為 "
				+ formatOrderSlot(savedOrder)
				+ "。取消原因："
				+ savedOrder.getCancelReason()
				+ "。";

		if (confirmed) {

			notificationContent +=
					"此訂單已付款，將等待後台處理全額退款。";

		} else if (pendingPayment) {

			notificationContent +=
					"您尚未完成付款，因此不會產生退款。";

		} else {

			notificationContent +=
					"您的預約申請已取消。";
		}

		sendNotification(
				savedOrder.getBuyerMemberId(),
				NOTIFY_BUYER_SELLER_CANCELLED,
				notificationContent
		);

		return savedOrder;
	}

	// =========================================================
	// 後台取消已成立訂單
	//
	// 取消後進入待退款。
	// =========================================================

	public ServiceOrderVO cancelByAdmin(
			Integer orderId,
			Integer employeeId,
			String reason) {

		if (employeeId == null) {
			throw new RuntimeException(
					"缺少處理員工資料"
			);
		}

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_CONFIRMED)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"只有已成立訂單可以由後台取消"
			);
		}

		ServiceSlotVO slot =
				getSlotOrThrow(
						order.getServiceSlotId()
				);

		LocalDateTime now =
				LocalDateTime.now();

		order.setOrderStatus(
				ORDER_CANCELLED
		);

		order.setCancelledByRole(
				CANCEL_BY_ADMIN
		);

		order.setCancelReason(
				normalizeCancelReason(
						reason,
						"後台取消訂單"
				)
		);

		order.setCancelledAt(now);

		order.setSellerConfirmExpiresAt(null);
		order.setPaymentExpiresAt(null);

		order.setRefundStatus(
				REFUND_PENDING
		);

		order.setRefundAmount(
				order.getTotalAmount()
		);

		order.setPayoutStatus(
				PAYOUT_UNPAID
		);

		order.setEmployeeId(
				employeeId
		);

		/*
		 * 現在只是取消，
		 * 還沒有實際完成退款。
		 */
		order.setHandledAt(null);

		releaseSlot(slot);

		slotRepo.save(slot);

		ServiceOrderVO savedOrder =
				orderRepo.save(order);

		publishSlotStatus(
				savedOrder,
				SLOT_AVAILABLE
		);

		String notificationContent =
				"平台已取消服務「"
				+ savedOrder.getServiceNameSnapshot()
				+ "」的訂單，預約時段為 "
				+ formatOrderSlot(savedOrder)
				+ "。取消原因："
				+ savedOrder.getCancelReason()
				+ "。此訂單將等待後台處理全額退款。";

		// 通知買家
		sendNotification(
				savedOrder.getBuyerMemberId(),
				NOTIFY_BUYER_PLATFORM_CANCELLED,
				notificationContent
		);

		// 通知賣家
		sendNotification(
				savedOrder.getSellerMemberId(),
				NOTIFY_SELLER_PLATFORM_CANCELLED,
				notificationContent
		);

		return savedOrder;
	}

	// =========================================================
	// 後台完成退款
	//
	// 退款前：
	// ORDER_STATUS = 4
	// REFUND_STATUS = 1
	//
	// 退款後：
	// ORDER_STATUS = 4
	// REFUND_STATUS = 2
	// =========================================================

	public ServiceOrderVO completeRefund(
			Integer orderId,
			Integer employeeId) {

		if (employeeId == null) {
			throw new RuntimeException(
					"缺少處理員工資料"
			);
		}

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_CANCELLED)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"只有已取消訂單可以退款"
			);
		}

		if (!Byte.valueOf(REFUND_PENDING)
				.equals(order.getRefundStatus())) {

			throw new RuntimeException(
					"此訂單不是待退款狀態"
			);
		}

		if (Byte.valueOf(PAYOUT_PAID)
				.equals(order.getPayoutStatus())) {

			throw new RuntimeException(
					"此訂單已撥款，不能直接退款"
			);
		}

		if (order.getRefundAmount() == null
				|| order.getRefundAmount() <= 0) {

			throw new RuntimeException(
					"退款金額不正確"
			);
		}

		order.setRefundStatus(
				REFUND_COMPLETED
		);

		order.setEmployeeId(
				employeeId
		);

		order.setHandledAt(
				LocalDateTime.now()
		);

		/*
		 * ORDER_STATUS 維持 4 已取消。
		 * PAYOUT_STATUS 維持 0 未撥款。
		 */

		ServiceOrderVO savedOrder =
		        orderRepo.save(order);

		// 通知買家：退款完成
		sendNotification(
		        savedOrder.getBuyerMemberId(),
		        NOTIFY_BUYER_REFUND_COMPLETED,
		        "您訂購的服務「"
		                + savedOrder.getServiceNameSnapshot()
		                + "」退款已完成，退款金額為 NT$ "
		                + savedOrder.getRefundAmount()
		                + "，原預約時段為 "
		                + formatOrderSlot(savedOrder)
		);

		return savedOrder;
	}

	// =========================================================
	// 服務完成
	// =========================================================

	public ServiceOrderVO completeOrder(
			Integer orderId) {

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_CONFIRMED)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"只有已成立訂單可以完成"
			);
		}

		order.setOrderStatus(
				ORDER_COMPLETED
		);

		order.setServiceCompletedAt(
				LocalDateTime.now()
		);

		/*
		 * 已使用過的時段不重新開放。
		 * SERVICE_SLOT 維持 2 已預約。
		 */

		return orderRepo.save(order);
	}

	// =========================================================
	// 後台撥款
	//
	// 只有已完成且尚未撥款的訂單可以撥款。
	// =========================================================

	public ServiceOrderVO payout(
			Integer orderId,
			Integer employeeId) {

		if (employeeId == null) {
			throw new RuntimeException(
					"缺少處理員工資料"
			);
		}

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_COMPLETED)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"只有已完成訂單可以撥款"
			);
		}

		if (Byte.valueOf(PAYOUT_PAID)
				.equals(order.getPayoutStatus())) {

			throw new RuntimeException(
					"此訂單已撥款"
			);
		}

		if (!Byte.valueOf(REFUND_NONE)
				.equals(order.getRefundStatus())) {

			throw new RuntimeException(
					"有退款狀態的訂單不能撥款"
			);
		}

		order.setPayoutStatus(
				PAYOUT_PAID
		);

		order.setEmployeeId(
				employeeId
		);

		order.setHandledAt(
				LocalDateTime.now()
		);

		return orderRepo.save(order);
	}

	// =========================================================
	// 單筆：賣家確認逾期
	//
	// 尚未付款，因此無退款。
	// =========================================================

	public ServiceOrderVO expireSellerConfirm(
			Integer orderId) {

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_PENDING_SELLER_CONFIRM)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"此訂單不是待賣家確認狀態"
			);
		}

		markSellerConfirmExpired(
				order,
				LocalDateTime.now()
		);

		return orderRepo.save(order);
	}

	// =========================================================
	// 單筆：買家付款逾期
	//
	// 取消訂單、釋放時段並透過 WebSocket 推播。
	// =========================================================

	public ServiceOrderVO expirePayment(
			Integer orderId) {

		ServiceOrderVO order =
				getOrderOrThrow(orderId);

		if (!Byte.valueOf(ORDER_PENDING_PAYMENT)
				.equals(order.getOrderStatus())) {

			throw new RuntimeException(
					"此訂單不是待付款狀態"
			);
		}

		ServiceSlotVO slot =
				getSlotOrThrow(
						order.getServiceSlotId()
				);

		markPaymentExpired(
				order,
				slot,
				LocalDateTime.now()
		);

		slotRepo.save(slot);

		ServiceOrderVO savedOrder =
				orderRepo.save(order);

		publishSlotStatus(
				savedOrder,
				SLOT_AVAILABLE
		);

		return savedOrder;
	}

	// =========================================================
	// 排程：批次處理賣家確認逾期
	//
	// Repository 查詢：
	// ORDER_STATUS = 0
	// SELLER_CONFIRM_EXPIRES_AT <= 現在
	// =========================================================

	public int expireOverdueSellerConfirmOrders() {

		LocalDateTime now =
				LocalDateTime.now();

		List<ServiceOrderVO> expiredOrders =
				orderRepo
					.findByOrderStatusAndSellerConfirmExpiresAtLessThanEqual(
							ORDER_PENDING_SELLER_CONFIRM,
							now
					);

		int count = 0;

		for (ServiceOrderVO order : expiredOrders) {

			/*
			 * 防止查詢後、實際處理前狀態已被改變。
			 */
			if (!Byte.valueOf(ORDER_PENDING_SELLER_CONFIRM)
					.equals(order.getOrderStatus())) {

				continue;
			}

			markSellerConfirmExpired(
					order,
					now
			);

			count++;
		}

		if (!expiredOrders.isEmpty()) {
			orderRepo.saveAll(expiredOrders);
		}

		return count;
	}

	// =========================================================
	// 排程：批次處理買家付款逾期
	//
	// Repository 查詢：
	// ORDER_STATUS = 1
	// PAYMENT_EXPIRES_AT <= 現在
	// =========================================================

	public int expireOverduePaymentOrders() {

		LocalDateTime now =
				LocalDateTime.now();

		List<ServiceOrderVO> expiredOrders =
				orderRepo
					.findByOrderStatusAndPaymentExpiresAtLessThanEqual(
							ORDER_PENDING_PAYMENT,
							now
					);

		int count = 0;

		for (ServiceOrderVO order : expiredOrders) {

			if (!Byte.valueOf(ORDER_PENDING_PAYMENT)
					.equals(order.getOrderStatus())) {

				continue;
			}

			ServiceSlotVO slot =
					getSlotOrThrow(
							order.getServiceSlotId()
					);

			markPaymentExpired(
					order,
					slot,
					now
			);

			slotRepo.save(slot);

			publishSlotStatus(
					order,
					SLOT_AVAILABLE
			);

			count++;
		}

		if (!expiredOrders.isEmpty()) {
			orderRepo.saveAll(expiredOrders);
		}

		return count;
	}

	// =========================================================
	// 賣家確認逾期共用處理
	// =========================================================

	private void markSellerConfirmExpired(
			ServiceOrderVO order,
			LocalDateTime now) {

		order.setOrderStatus(
				ORDER_CANCELLED
		);

		order.setCancelledByRole(
				CANCEL_BY_SYSTEM
		);

		order.setCancelReason(
				"賣家未於 180 秒內確認訂單"
		);

		order.setCancelledAt(now);

		order.setSellerConfirmExpiresAt(null);
		order.setPaymentExpiresAt(null);

		order.setRefundStatus(
				REFUND_NONE
		);

		order.setRefundAmount(0);

		order.setPayoutStatus(
				PAYOUT_UNPAID
		);

		order.setHandledAt(null);

		/*
		 * 買家送出申請時沒有鎖定時段，
		 * 因此不需要釋放 SERVICE_SLOT。
		 */

		sendNotification(
				order.getBuyerMemberId(),
				NOTIFY_BUYER_REQUEST_EXPIRED,
				"賣家未在期限內確認您對服務「"
						+ order.getServiceNameSnapshot()
						+ "」的預約申請，系統已自動取消。預約時段為 "
						+ formatOrderSlot(order)
						+ "。"
		);
	}

	// =========================================================
	// 買家付款逾期共用處理
	// =========================================================

	private void markPaymentExpired(
			ServiceOrderVO order,
			ServiceSlotVO slot,
			LocalDateTime now) {

		order.setOrderStatus(
				ORDER_CANCELLED
		);

		order.setCancelledByRole(
				CANCEL_BY_SYSTEM
		);

		order.setCancelReason(
				"買家未於 60 秒內完成付款"
		);

		order.setCancelledAt(now);

		order.setSellerConfirmExpiresAt(null);
		order.setPaymentExpiresAt(null);

		order.setRefundStatus(
				REFUND_NONE
		);

		order.setRefundAmount(0);

		order.setPayoutStatus(
				PAYOUT_UNPAID
		);

		order.setHandledAt(null);

		releaseSlot(slot);

		sendNotification(
				order.getBuyerMemberId(),
				NOTIFY_BUYER_PAYMENT_EXPIRED,
				"您未在期限內完成服務「"
						+ order.getServiceNameSnapshot()
						+ "」的付款，系統已自動取消訂單。預約時段為 "
						+ formatOrderSlot(order)
						+ "。"
		);

		sendNotification(
				order.getSellerMemberId(),
				NOTIFY_SELLER_BUYER_PAYMENT_EXPIRED,
				"服務「"
						+ order.getServiceNameSnapshot()
						+ "」的買家未在期限內完成付款，系統已自動取消訂單並重新開放時段。預約時段為 "
						+ formatOrderSlot(order)
						+ "。"
		);
	}

	// =========================================================
	// 建立會員通知
	//
	// NotificationService.addNotification() 會自動設定：
	// CREATED_AT = LocalDate.now()
	// IS_READ = 0
	// =========================================================

	private void sendNotification(
			Integer memberId,
			String title,
			String content) {

		if (memberId == null) {
			return;
		}

		MemberVO member = memberRepo
				.findById(memberId)
				.orElseThrow(
						() -> new RuntimeException(
								"找不到通知收件會員：" + memberId
						)
				);

		NotificationVO notification =
				new NotificationVO();

		notification.setMember(member);

		notification.setTitle(title);

		notification.setContent(content);

		// 0：一般會員通知
		notification.setNotificationType(
				(byte) 0
		);

		notificationService.addNotification(
				notification
		);
	}

	// =========================================================
	// 格式化通知中的訂單時段
	// =========================================================

	private String formatOrderSlot(
			ServiceOrderVO order) {

		if (order == null
				|| order.getSlotStartTimeSnapshot() == null
				|| order.getSlotEndTimeSnapshot() == null) {

			return "時間未設定";
		}

		DateTimeFormatter formatter =
				DateTimeFormatter.ofPattern(
						"yyyy-MM-dd HH:mm"
				);

		return order
					.getSlotStartTimeSnapshot()
					.format(formatter)
				+ " ～ "
				+ order
					.getSlotEndTimeSnapshot()
					.format(formatter);
	}

	// =========================================================
	// 計算訂單總金額
	// =========================================================

	private int calculateTotalAmount(
			ServiceVO service,
			ServiceSlotVO slot) {

		if (service == null
				|| service.getHourlyRate() == null) {

			throw new RuntimeException(
					"服務費率不正確"
			);
		}

		if (slot.getStartTime() == null
				|| slot.getEndTime() == null) {

			throw new RuntimeException(
					"服務時段時間不完整"
			);
		}

		long minutes =
				Duration
					.between(
							slot.getStartTime(),
							slot.getEndTime()
					)
					.toMinutes();

		if (minutes <= 0) {
			throw new RuntimeException(
					"服務時段時間不正確"
			);
		}

		/*
		 * 例如：
		 * 每小時 300 元
		 * 90 分鐘
		 * 300 × 90 ÷ 60 = 450 元
		 */
		long totalAmount =
				minutes
				* service.getHourlyRate()
				/ 60;

		return Math.toIntExact(totalAmount);
	}

	// =========================================================
	// 釋放時段
	// =========================================================

	private void releaseSlot(
			ServiceSlotVO slot) {

		slot.setSlotStatus(
				SLOT_AVAILABLE
		);

		slot.setLockExpiresAt(null);
	}

	// =========================================================
	// WebSocket 推播時段狀態
	// =========================================================

	private void publishSlotStatus(
			ServiceOrderVO order,
			Byte slotStatus) {

		slotStatusWebSocketService
				.publishSlotStatus(
						order.getServiceId(),
						order.getServiceSlotId(),
						slotStatus
				);
	}

	// =========================================================
	// 共用查詢
	// =========================================================

	private ServiceOrderVO getOrderOrThrow(
			Integer orderId) {

		if (orderId == null) {
			throw new RuntimeException(
					"訂單編號不可為空"
			);
		}

		return orderRepo
				.findById(orderId)
				.orElseThrow(
						() -> new RuntimeException(
								"找不到訂單"
						)
				);
	}

	private ServiceSlotVO getSlotOrThrow(
			Integer slotId) {

		if (slotId == null) {
			throw new RuntimeException(
					"服務時段編號不可為空"
			);
		}

		return slotRepo
				.findById(slotId)
				.orElseThrow(
						() -> new RuntimeException(
								"找不到服務時段"
						)
				);
	}

	// =========================================================
	// 付款方式驗證
	// =========================================================

	private void validatePaymentMethod(
			Byte paymentMethod) {

		if (paymentMethod == null) {
			throw new RuntimeException(
					"請選擇付款方式"
			);
		}

		if (paymentMethod.byteValue() != 0
				&& paymentMethod.byteValue() != 1) {

			throw new RuntimeException(
					"付款方式不正確"
			);
		}
	}

	// =========================================================
	// 文字處理
	// =========================================================

	private String normalizeNullableText(
			String text) {

		if (text == null) {
			return null;
		}

		String normalized =
				text.trim();

		return normalized.isEmpty()
				? null
				: normalized;
	}

	private String normalizeCancelReason(
			String reason,
			String defaultReason) {

		String normalized =
				normalizeNullableText(reason);

		return normalized != null
				? normalized
				: defaultReason;
	}

	// =========================================================
	// 一般查詢
	// =========================================================

	@Transactional(readOnly = true)
	public ServiceOrderVO getOne(
			Integer orderId) {

		return orderRepo
				.findById(orderId)
				.orElse(null);
	}

	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getAll() {

		return orderRepo.findAll();
	}

	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getByBuyerMemberId(
			Integer buyerMemberId) {

		return orderRepo.findByBuyerMemberId(
				buyerMemberId
		);
	}

	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getBySellerMemberId(
			Integer sellerMemberId) {

		return orderRepo.findBySellerMemberId(
				sellerMemberId
		);
	}

	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getByServiceId(
			Integer serviceId) {

		return orderRepo.findByServiceId(
				serviceId
		);
	}

	/*
	 * 同一時段可能有多筆申請，
	 * 因此現在回傳 List。
	 */
	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getByServiceSlotId(
			Integer serviceSlotId) {

		return orderRepo.findAllByServiceSlotId(
				serviceSlotId
		);
	}

	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getByOrderStatus(
			Byte orderStatus) {

		return orderRepo.findByOrderStatus(
				orderStatus
		);
	}

	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getByPaymentMethod(
			Byte paymentMethod) {

		return orderRepo.findByServicePaymentMethod(
				paymentMethod
		);
	}

	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getByRefundStatus(
			Byte refundStatus) {

		return orderRepo.findByRefundStatus(
				refundStatus
		);
	}

	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getByPayoutStatus(
			Byte payoutStatus) {

		return orderRepo.findByPayoutStatus(
				payoutStatus
		);
	}

	@Transactional(readOnly = true)
	public boolean existsByServiceId(
			Integer serviceId) {

		return orderRepo.existsByServiceId(
				serviceId
		);
	}

	// =========================================================
	// 後台查詢
	// =========================================================

	/*
	 * 已完成、尚未撥款。
	 *
	 * ORDER_STATUS = 3
	 * PAYOUT_STATUS = 0
	 */
	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getCompletedUnpaidOrders() {

		return orderRepo.findByOrderStatusAndPayoutStatus(
				ORDER_COMPLETED,
				PAYOUT_UNPAID
		);
	}

	/*
	 * 已完成、已經撥款。
	 *
	 * ORDER_STATUS = 3
	 * PAYOUT_STATUS = 1
	 */
	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getCompletedPaidOrders() {

		return orderRepo.findByOrderStatusAndPayoutStatus(
				ORDER_COMPLETED,
				PAYOUT_PAID
		);
	}

	/*
	 * 已取消、不需要退款。
	 *
	 * ORDER_STATUS = 4
	 * REFUND_STATUS = 0
	 */
	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getCancelledNoRefundOrders() {

		return orderRepo.findByOrderStatusAndRefundStatus(
				ORDER_CANCELLED,
				REFUND_NONE
		);
	}

	/*
	 * 已取消、等待後台退款。
	 *
	 * ORDER_STATUS = 4
	 * REFUND_STATUS = 1
	 */
	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getCancelledPendingRefundOrders() {

		return orderRepo.findByOrderStatusAndRefundStatus(
				ORDER_CANCELLED,
				REFUND_PENDING
		);
	}

	/*
	 * 已取消、已完成退款。
	 *
	 * ORDER_STATUS = 4
	 * REFUND_STATUS = 2
	 */
	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getCancelledRefundedOrders() {

		return orderRepo.findByOrderStatusAndRefundStatus(
				ORDER_CANCELLED,
				REFUND_COMPLETED
		);
	}

	/*
	 * 保留原本方法名稱，
	 * 避免其他 Controller 已經有呼叫。
	 */
	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getPendingRefundOrders() {

		return getCancelledPendingRefundOrders();
	}

	/*
	 * 保留原本方法名稱，
	 * 避免其他 Controller 已經有呼叫。
	 */
	@Transactional(readOnly = true)
	public List<ServiceOrderVO> getPendingPayoutOrders() {

		return getCompletedUnpaidOrders();
	}

	// =========================================================
	// 後台統計
	// =========================================================

	@Transactional(readOnly = true)
	public Long getCompletedOrderTotalAmount() {

		return orderRepo
				.sumCompletedOrderTotalAmount();
	}

	@Transactional(readOnly = true)
	public Long getCompletedUnpaidTotalAmount() {

		return orderRepo
				.sumCompletedUnpaidTotalAmount();
	}

	@Transactional(readOnly = true)
	public Long getCompletedUnpaidOrderCount() {

		return orderRepo
				.countCompletedUnpaidOrders();
	}

	@Transactional(readOnly = true)
	public Long getPendingRefundAmount() {

		return orderRepo
				.sumPendingRefundAmount();
	}

	@Transactional(readOnly = true)
	public Long getPendingRefundOrderCount() {

		return orderRepo
				.countPendingRefundOrders();
	}
	
	@Transactional(readOnly = true)
	public Long getCompletedOrderCount() {

	    return orderRepo.countCompletedOrders();
	}
}