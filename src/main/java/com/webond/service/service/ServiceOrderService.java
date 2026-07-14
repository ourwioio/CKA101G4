package com.webond.service.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.service.model.ServiceOrderVO;
import com.webond.service.model.ServiceSlotVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.repository.ServiceOrderRepository;
import com.webond.service.repository.ServiceRepository;
import com.webond.service.repository.ServiceSlotRepository;

@Service
@Transactional
public class ServiceOrderService {

    // =========================================================
    // 訂單狀態
    // 0：待賣家確認
    // 1：待買家付款
    // 2：已成立
    // 3：已完成
    // 4：已取消
    // 5：已退款
    // =========================================================

    private static final byte ORDER_PENDING_SELLER_CONFIRM = 0;
    private static final byte ORDER_PENDING_PAYMENT = 1;
    private static final byte ORDER_CONFIRMED = 2;
    private static final byte ORDER_COMPLETED = 3;
    private static final byte ORDER_CANCELLED = 4;
    private static final byte ORDER_REFUNDED = 5;

    // =========================================================
    // 時段狀態
    // 0：可預約
    // 1：暫時鎖定
    // 2：已預約
    // =========================================================

    private static final byte SLOT_AVAILABLE = 0;
    private static final byte SLOT_LOCKED = 1;
    private static final byte SLOT_BOOKED = 2;

    // =========================================================
    // 退款狀態
    //
    // 目前沿用你原本程式的定義：
    // 0：無退款
    // 2：全額退款
    // 3：部分退款
    // =========================================================

    private static final byte REFUND_NONE = 0;
    private static final byte REFUND_FULL = 2;
    private static final byte REFUND_PARTIAL = 3;

    // =========================================================
    // 撥款狀態
    // 0：未撥款
    // 1：已撥款
    // =========================================================

    private static final byte PAYOUT_UNPAID = 0;
    private static final byte PAYOUT_PAID = 1;

    // =========================================================
    // 取消方角色
    // 0：買家
    // 1：賣家
    // 2：後台
    // 3：系統
    // =========================================================

    private static final byte CANCEL_BY_BUYER = 0;
    private static final byte CANCEL_BY_SELLER = 1;
    private static final byte CANCEL_BY_ADMIN = 2;
    private static final byte CANCEL_BY_SYSTEM = 3;

    private final ServiceOrderRepository orderRepo;
    private final ServiceRepository serviceRepo;
    private final ServiceSlotRepository slotRepo;
    private final SlotStatusWebSocketService slotStatusWebSocketService;

    public ServiceOrderService(
            ServiceOrderRepository orderRepo,
            ServiceRepository serviceRepo,
            ServiceSlotRepository slotRepo,
            SlotStatusWebSocketService slotStatusWebSocketService) {

        this.orderRepo = orderRepo;
        this.serviceRepo = serviceRepo;
        this.slotRepo = slotRepo;
        this.slotStatusWebSocketService = slotStatusWebSocketService;
    }

    // =========================================================
    // 買家送出預約申請
    // =========================================================

    public ServiceOrderVO createRequest(
            Integer slotId,
            Integer buyerId,
            String buyerRequestNote) {

        if (buyerId == null) {
            throw new RuntimeException("請先登入");
        }

        ServiceSlotVO slot = getSlotOrThrow(slotId);

        // 只有可預約時段才能提出申請
        if (!Byte.valueOf(SLOT_AVAILABLE)
                .equals(slot.getSlotStatus())) {

            throw new RuntimeException("此時段不可預約");
        }

        ServiceVO service = serviceRepo.findById(slot.getServiceId())
                .orElseThrow(
                        () -> new RuntimeException("找不到服務")
                );

        // 不能預約自己的服務
        if (service.getMemberId().equals(buyerId)) {
            throw new RuntimeException("不能預約自己的服務");
        }

        LocalDateTime now = LocalDateTime.now();

        // 賣家必須在 24 小時內確認
        LocalDateTime sellerConfirmExpiresAt =
                now.plusHours(24);

        ServiceOrderVO order = new ServiceOrderVO();

        // -----------------------------------------------------
        // 原始關聯資料
        // -----------------------------------------------------

        order.setServiceSlotId(slotId);
        order.setServiceId(service.getServiceId());
        order.setBuyerMemberId(buyerId);

        // -----------------------------------------------------
        // 訂單快照資料
        // -----------------------------------------------------

        order.setSellerMemberId(service.getMemberId());

        order.setServiceNameSnapshot(
                service.getServiceName()
        );

        order.setServiceDescriptionSnapshot(
                service.getDescription()
        );

        if (service.getServiceType() != null) {

            order.setServiceTypeNameSnapshot(
                    service.getServiceType().getTypeName()
            );

        } else {

            order.setServiceTypeNameSnapshot("未分類");
        }

        order.setServiceCitySnapshot(
                service.getServiceCity()
        );

        order.setServiceDistrictSnapshot(
                service.getServiceDistrict()
        );

        order.setServiceLocationSnapshot(
                service.getServiceLocation()
        );

        order.setSlotStartTimeSnapshot(
                slot.getStartTime()
        );

        order.setSlotEndTimeSnapshot(
                slot.getEndTime()
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
        // 訂單初始狀態
        // -----------------------------------------------------

        order.setOrderStatus(
                ORDER_PENDING_SELLER_CONFIRM
        );

        order.setBuyerRequestNote(
                normalizeNullableText(buyerRequestNote)
        );

        order.setCreatedAt(now);

        // -----------------------------------------------------
        // 預設退款、撥款狀態
        // -----------------------------------------------------

        order.setRefundStatus(REFUND_NONE);
        order.setRefundAmount(0);
        order.setPayoutStatus(PAYOUT_UNPAID);

        // -----------------------------------------------------
        // 期限
        // -----------------------------------------------------

        order.setSellerConfirmExpiresAt(
                sellerConfirmExpiresAt
        );

        order.setPaymentExpiresAt(null);

        /*
         * 買家剛提出申請時不鎖時段。
         *
         * SERVICE_SLOT.SLOT_STATUS 仍保持 0。
         * 等賣家接受申請後，才改為 1 暫時鎖定。
         */

        return orderRepo.save(order);
    }

    // =========================================================
    // 賣家接受預約申請
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

        // 防止賣家在確認期限過後仍接受
        if (order.getSellerConfirmExpiresAt() != null
                && now.isAfter(
                        order.getSellerConfirmExpiresAt()
                )) {

            throw new RuntimeException(
                    "賣家確認期限已過"
            );
        }

        ServiceSlotVO slot =
                getSlotOrThrow(
                        order.getServiceSlotId()
                );

        /*
         * 賣家同意時才真正鎖定時段。
         *
         * 若時段已不是可預約，代表其他訂單已經先取得此時段。
         */
        if (!Byte.valueOf(SLOT_AVAILABLE)
                .equals(slot.getSlotStatus())) {

            throw new RuntimeException(
                    "此時段已被其他申請鎖定或預約"
            );
        }

        // 買家付款期限為 30 分鐘
        LocalDateTime paymentExpiresAt =
                now.plusMinutes(30);

        // 訂單進入待付款
        order.setOrderStatus(
                ORDER_PENDING_PAYMENT
        );

        order.setSellerRequirementNote(
                normalizeNullableText(
                        sellerRequirementNote
                )
        );

        order.setPaymentExpiresAt(
                paymentExpiresAt
        );

        // 時段暫時鎖定
        slot.setSlotStatus(SLOT_LOCKED);

        slot.setLockExpiresAt(
                paymentExpiresAt
        );

        slotRepo.save(slot);

        ServiceOrderVO savedOrder =
                orderRepo.save(order);

        // WebSocket：通知前端時段已暫時鎖定
        publishSlotStatus(
                order,
                SLOT_LOCKED
        );

        return savedOrder;
    }

    // =========================================================
    // 賣家拒絕預約申請
    // =========================================================

    public ServiceOrderVO rejectRequest(
            Integer orderId,
            String reason) {

        ServiceOrderVO order =
                getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_PENDING_SELLER_CONFIRM)
                .equals(order.getOrderStatus())) {

            throw new RuntimeException(
                    "只有待賣家確認的訂單可以拒絕"
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

        /*
         * 買家提出申請時沒有鎖定時段，
         * 所以賣家拒絕時不需要釋放時段。
         */

        return orderRepo.save(order);
    }

    // =========================================================
    // 買家付款成功
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

        LocalDateTime now =
                LocalDateTime.now();

        if (order.getPaymentExpiresAt() != null
                && now.isAfter(
                        order.getPaymentExpiresAt()
                )) {

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

        order.setOrderStatus(
                ORDER_CONFIRMED
        );

        order.setServicePaymentMethod(
                paymentMethod
        );

        // 付款成功後不再需要付款期限
        order.setPaymentExpiresAt(null);

        slot.setSlotStatus(
                SLOT_BOOKED
        );

        slot.setLockExpiresAt(null);

        slotRepo.save(slot);

        ServiceOrderVO savedOrder =
                orderRepo.save(order);

        // WebSocket：通知前端時段已被預約
        publishSlotStatus(
                order,
                SLOT_BOOKED
        );

        return savedOrder;
    }

    // =========================================================
    // 買家取消已成立訂單
    //
    // 規則：
    // 距離服務開始 7 天以上：全額退款
    // 距離服務開始 3～7 天：退 50%
    // 距離服務開始未滿 3 天：不能取消
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

        ServiceSlotVO slot =
                getSlotOrThrow(
                        order.getServiceSlotId()
                );

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

        long daysBeforeStart =
                Duration.between(
                        now,
                        serviceStartTime
                ).toDays();

        if (daysBeforeStart < 3) {
            throw new RuntimeException(
                    "距離服務開始未滿 3 天，無法取消訂單"
            );
        }

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

        // 依照取消時間套用退款規則
        applyBuyerRefundRule(order);

        // 取消後重新釋放時段
        releaseSlot(slot);

        slotRepo.save(slot);

        ServiceOrderVO savedOrder =
                orderRepo.save(order);

        // WebSocket：通知前端時段重新開放
        publishSlotStatus(
                order,
                SLOT_AVAILABLE
        );

        return savedOrder;
    }

    // =========================================================
    // 賣家取消已成立訂單
    //
    // 賣家取消時，買家全額退款。
    // =========================================================

    public ServiceOrderVO cancelBySeller(
            Integer orderId,
            String reason) {

        ServiceOrderVO order =
                getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_CONFIRMED)
                .equals(order.getOrderStatus())) {

            throw new RuntimeException(
                    "只有已成立訂單可以由賣家取消"
            );
        }

        ServiceSlotVO slot =
                getSlotOrThrow(
                        order.getServiceSlotId()
                );

        LocalDateTime now =
                LocalDateTime.now();

        order.setOrderStatus(
                ORDER_REFUNDED
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

        order.setRefundStatus(
                REFUND_FULL
        );

        order.setRefundAmount(
                order.getTotalAmount()
        );

        order.setHandledAt(now);

        // 取消後重新釋放時段
        releaseSlot(slot);

        slotRepo.save(slot);

        ServiceOrderVO savedOrder =
                orderRepo.save(order);

        // WebSocket：通知前端時段重新開放
        publishSlotStatus(
                order,
                SLOT_AVAILABLE
        );

        return savedOrder;
    }

    // =========================================================
    // 後台取消已成立訂單
    //
    // 目前設定為全額退款。
    // =========================================================

    public ServiceOrderVO cancelByAdmin(
            Integer orderId,
            Integer employeeId,
            String reason) {

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
                ORDER_REFUNDED
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

        order.setRefundStatus(
                REFUND_FULL
        );

        order.setRefundAmount(
                order.getTotalAmount()
        );

        order.setEmployeeId(employeeId);
        order.setHandledAt(now);

        releaseSlot(slot);

        slotRepo.save(slot);

        ServiceOrderVO savedOrder =
                orderRepo.save(order);

        publishSlotStatus(
                order,
                SLOT_AVAILABLE
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

        LocalDateTime now =
                LocalDateTime.now();

        order.setOrderStatus(
                ORDER_COMPLETED
        );

        order.setServiceCompletedAt(now);

        /*
         * 完成後 SERVICE_SLOT 維持 2 已預約。
         * 已經使用過的時段不能重新開放。
         */

        return orderRepo.save(order);
    }

    // =========================================================
    // 後台撥款
    // =========================================================

    public ServiceOrderVO payout(
            Integer orderId,
            Integer employeeId) {

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
    // 賣家確認逾期
    //
    // 原本狀態 6 改為：
    // ORDER_STATUS = 4 已取消
    // CANCELLED_BY_ROLE = 3 系統
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

        LocalDateTime now =
                LocalDateTime.now();

        order.setOrderStatus(
                ORDER_CANCELLED
        );

        order.setCancelledByRole(
                CANCEL_BY_SYSTEM
        );

        order.setCancelReason(
                "賣家未於期限內確認訂單"
        );

        order.setCancelledAt(now);

        /*
         * 買家提出申請時沒有鎖定時段，
         * 所以賣家確認逾期只取消訂單，
         * 不需要修改 SERVICE_SLOT。
         */

        return orderRepo.save(order);
    }

    // =========================================================
    // 買家付款逾期
    //
    // 原本狀態 7 改為：
    // ORDER_STATUS = 4 已取消
    // CANCELLED_BY_ROLE = 3 系統
    // 並釋放暫時鎖定的時段
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

        if (!Byte.valueOf(SLOT_LOCKED)
                .equals(slot.getSlotStatus())) {

            throw new RuntimeException(
                    "此時段目前不是待付款鎖定狀態"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        order.setOrderStatus(
                ORDER_CANCELLED
        );

        order.setCancelledByRole(
                CANCEL_BY_SYSTEM
        );

        order.setCancelReason(
                "買家未於期限內完成付款"
        );

        order.setCancelledAt(now);

        // 清除付款期限
        order.setPaymentExpiresAt(null);

        // 付款逾期後重新釋放時段
        releaseSlot(slot);

        slotRepo.save(slot);

        ServiceOrderVO savedOrder =
                orderRepo.save(order);

        // WebSocket：通知前端時段重新開放
        publishSlotStatus(
                order,
                SLOT_AVAILABLE
        );

        return savedOrder;
    }

    // =========================================================
    // 買家取消退款規則
    //
    // 7 天以上：全額退款
    // 3～7 天：退款 50%
    // 未滿 3 天：前面已阻擋
    // =========================================================

    private void applyBuyerRefundRule(
            ServiceOrderVO order) {

        int refundAmount =
                calculateRefundAmount(
                        order.getTotalAmount(),
                        order.getSlotStartTimeSnapshot()
                );

        order.setRefundAmount(
                refundAmount
        );

        order.setHandledAt(
                LocalDateTime.now()
        );

        /*
         * 因為退款已完成，
         * 訂單最終狀態設為 5 已退款。
         */
        order.setOrderStatus(
                ORDER_REFUNDED
        );

        if (refundAmount
                == order.getTotalAmount()) {

            order.setRefundStatus(
                    REFUND_FULL
            );

        } else {

            order.setRefundStatus(
                    REFUND_PARTIAL
            );
        }
    }

    private int calculateRefundAmount(
            Integer totalAmount,
            LocalDateTime startTime) {

        if (totalAmount == null
                || totalAmount < 0) {

            throw new RuntimeException(
                    "訂單金額不正確"
            );
        }

        if (startTime == null) {
            throw new RuntimeException(
                    "訂單缺少服務開始時間"
            );
        }

        long daysBeforeStart =
                Duration.between(
                        LocalDateTime.now(),
                        startTime
                ).toDays();

        if (daysBeforeStart >= 7) {
            return totalAmount;
        }

        // 3～7 天退款 50%
        return totalAmount / 2;
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
                Duration.between(
                        slot.getStartTime(),
                        slot.getEndTime()
                ).toMinutes();

        if (minutes <= 0) {
            throw new RuntimeException(
                    "服務時段時間不正確"
            );
        }

        /*
         * 使用分鐘計算，避免 30 分鐘被 toHours() 算成 0。
         *
         * 例如：
         * 每小時 300 元
         * 30 分鐘 = 300 × 30 ÷ 60 = 150 元
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
    // WebSocket 推送時段狀態
    // =========================================================

    private void publishSlotStatus(
            ServiceOrderVO order,
            Byte slotStatus) {

        slotStatusWebSocketService.publishSlotStatus(
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

        return orderRepo.findById(orderId)
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

        return slotRepo.findById(slotId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "找不到服務時段"
                        )
                );
    }

    // =========================================================
    // 共用文字處理
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
    // 查詢方法
    // =========================================================

    @Transactional(readOnly = true)
    public ServiceOrderVO getOne(
            Integer orderId) {

        return orderRepo.findById(orderId)
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

    @Transactional(readOnly = true)
    public ServiceOrderVO getByServiceSlotId(
            Integer serviceSlotId) {

        return orderRepo.findByServiceSlotId(
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
}