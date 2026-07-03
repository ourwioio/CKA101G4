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

    // =====================
    // 訂單狀態
    // =====================
    private static final byte ORDER_PENDING_SELLER_CONFIRM = 0; // 待賣家確認
    private static final byte ORDER_PENDING_PAYMENT = 1;        // 待買家付款
    private static final byte ORDER_CONFIRMED = 2;              // 已成立
    private static final byte ORDER_COMPLETED = 3;              // 已完成
    private static final byte ORDER_CANCELLED = 4;              // 已取消 / 已拒絕
    private static final byte ORDER_REFUNDED = 5;               // 已退款
    private static final byte ORDER_SELLER_CONFIRM_EXPIRED = 6; // 賣家確認逾期
    private static final byte ORDER_PAYMENT_EXPIRED = 7;        // 買家付款逾期

    // =====================
    // 時段狀態
    // =====================
    private static final byte SLOT_AVAILABLE = 0; // 可預約
    private static final byte SLOT_LOCKED = 1;    // 暫時鎖定
    private static final byte SLOT_BOOKED = 2;    // 已預約

    // =====================
    // 退款狀態
    // =====================
    private static final byte REFUND_NONE = 0;    // 無退款
    private static final byte REFUND_FULL = 2;    // 已退款
    private static final byte REFUND_PARTIAL = 3; // 部分退款

    // =====================
    // 撥款狀態
    // =====================
    private static final byte PAYOUT_UNPAID = 0; // 未撥款
    private static final byte PAYOUT_PAID = 1;   // 已撥款

    // =====================
    // 取消方角色
    // =====================
    private static final byte CANCEL_BY_BUYER = 0;  // 買家取消
    private static final byte CANCEL_BY_SELLER = 1; // 賣家取消

    private final ServiceOrderRepository orderRepo;
    private final ServiceRepository serviceRepo;
    private final ServiceSlotRepository slotRepo;
    private final SlotStatusWebSocketService slotStatusWebSocketService;

    public ServiceOrderService(ServiceOrderRepository orderRepo,
                               ServiceRepository serviceRepo,
                               ServiceSlotRepository slotRepo,
                               SlotStatusWebSocketService slotStatusWebSocketService) {
        this.orderRepo = orderRepo;
        this.serviceRepo = serviceRepo;
        this.slotRepo = slotRepo;
        this.slotStatusWebSocketService = slotStatusWebSocketService;
    }

    // =====================
    // 買家送出預約申請
    // =====================
    public ServiceOrderVO createRequest(Integer slotId,
                                        Integer buyerId,
                                        String buyerRequestNote) {

        if (buyerId == null) {
            throw new RuntimeException("請先登入");
        }

        ServiceSlotVO slot = getSlotOrThrow(slotId);

        // 買家只能申請可預約時段
        if (!Byte.valueOf(SLOT_AVAILABLE).equals(slot.getSlotStatus())) {
            throw new RuntimeException("此時段不可預約");
        }

        ServiceVO service = serviceRepo.findById(slot.getServiceId())
                .orElseThrow(() -> new RuntimeException("找不到服務"));

        // 不能預約自己的服務
        if (service.getMemberId().equals(buyerId)) {
            throw new RuntimeException("不能預約自己的服務");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sellerConfirmExpiresAt = now.plusHours(24);

        ServiceOrderVO order = new ServiceOrderVO();

        // 原始關聯資料
        order.setServiceSlotId(slotId);
        order.setServiceId(service.getServiceId());
        order.setBuyerMemberId(buyerId);

        // 訂單快照資料
        order.setSellerMemberId(service.getMemberId());
        order.setServiceNameSnapshot(service.getServiceName());
        order.setServiceDescriptionSnapshot(service.getDescription());

        if (service.getServiceType() != null) {
            order.setServiceTypeNameSnapshot(service.getServiceType().getTypeName());
        } else {
            order.setServiceTypeNameSnapshot("未分類");
        }

        order.setSlotStartTimeSnapshot(slot.getStartTime());
        order.setSlotEndTimeSnapshot(slot.getEndTime());

        // 金額快照
        order.setOrderHourlyRate(service.getHourlyRate());
        order.setTotalAmount(calculateTotalAmount(service, slot));

        // 訂單狀態
        order.setOrderStatus(ORDER_PENDING_SELLER_CONFIRM);
        order.setBuyerRequestNote(buyerRequestNote);
        order.setCreatedAt(now);

        // 預設退款 / 撥款狀態
        order.setRefundStatus(REFUND_NONE);
        order.setRefundAmount(0);
        order.setPayoutStatus(PAYOUT_UNPAID);

        // 賣家確認期限
        order.setSellerConfirmExpiresAt(sellerConfirmExpiresAt);
        order.setPaymentExpiresAt(null);

        /*
         * 新流程：
         * 買家送出申請時，不鎖定時段。
         * slotStatus 保持 0 可預約。
         * 等賣家 acceptRequest() 同意後，才改成 1 暫時鎖定。
         */

        return orderRepo.save(order);
    }

    // =====================
    // 賣家接受預約
    // =====================
    public ServiceOrderVO acceptRequest(Integer orderId,
                                        String sellerRequirementNote) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_PENDING_SELLER_CONFIRM).equals(order.getOrderStatus())) {
            throw new RuntimeException("此訂單不是待賣家確認狀態");
        }

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());

        // 賣家同意時，才真正鎖定時段
        // 如果已經不是可預約，代表此時段可能已被其他申請鎖定或預約
        if (!Byte.valueOf(SLOT_AVAILABLE).equals(slot.getSlotStatus())) {
            throw new RuntimeException("此時段已被其他申請鎖定或預約");
        }

        LocalDateTime paymentExpiresAt = LocalDateTime.now().plusMinutes(30);

        // 訂單進入待付款
        order.setOrderStatus(ORDER_PENDING_PAYMENT);
        order.setSellerRequirementNote(sellerRequirementNote);
        order.setPaymentExpiresAt(paymentExpiresAt);

        // 時段進入暫時鎖定
        slot.setSlotStatus(SLOT_LOCKED);
        slot.setLockExpiresAt(paymentExpiresAt);

        slotRepo.save(slot);
        ServiceOrderVO savedOrder = orderRepo.save(order);

        // WebSocket 推送：這個時段變成暫時鎖定
        publishSlotStatus(order, SLOT_LOCKED);

        return savedOrder;
    }

    // =====================
    // 賣家拒絕預約
    // =====================
    public ServiceOrderVO rejectRequest(Integer orderId,
                                        String reason) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_PENDING_SELLER_CONFIRM).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有待賣家確認的訂單可以拒絕");
        }

        order.setOrderStatus(ORDER_CANCELLED);
        order.setCancelledByRole(CANCEL_BY_SELLER);
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());

        /*
         * 新流程：
         * 買家申請時不鎖定時段，所以賣家拒絕時不要 releaseSlot。
         * 這裡只改訂單狀態，不動 SERVICE_SLOT。
         */

        return orderRepo.save(order);
    }

    // =====================
    // 買家付款成功
    // =====================
    public ServiceOrderVO paySuccess(Integer orderId,
                                     Byte paymentMethod) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_PENDING_PAYMENT).equals(order.getOrderStatus())) {
            throw new RuntimeException("此訂單不是待付款狀態");
        }

        if (order.getPaymentExpiresAt() != null &&
                LocalDateTime.now().isAfter(order.getPaymentExpiresAt())) {
            throw new RuntimeException("付款期限已過");
        }

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());

        if (!Byte.valueOf(SLOT_LOCKED).equals(slot.getSlotStatus())) {
            throw new RuntimeException("此時段目前不是待付款鎖定狀態");
        }

        order.setOrderStatus(ORDER_CONFIRMED);
        order.setServicePaymentMethod(paymentMethod);

        slot.setSlotStatus(SLOT_BOOKED);
        slot.setLockExpiresAt(null);

        slotRepo.save(slot);
        ServiceOrderVO savedOrder = orderRepo.save(order);

        // WebSocket 推送：這個時段變成已預約
        publishSlotStatus(order, SLOT_BOOKED);

        return savedOrder;
    }

    // =====================
    // 買家取消：三天內不能取消
    // =====================
    public ServiceOrderVO cancelByBuyer(Integer orderId,
                                        String reason) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_CONFIRMED).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有已成立訂單可以由買家取消");
        }

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());

        // 用訂單快照時間判斷，不用 slot 現在的時間
        long daysBeforeStart = Duration.between(
                LocalDateTime.now(),
                order.getSlotStartTimeSnapshot()
        ).toDays();

        if (daysBeforeStart < 3) {
            throw new RuntimeException("距離服務開始未滿 3 天，無法取消訂單");
        }

        order.setCancelledByRole(CANCEL_BY_BUYER);
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());

        applyBuyerRefundRule(order);

        // 已成立訂單取消後，時段重新開放
        releaseSlot(slot);

        slotRepo.save(slot);
        ServiceOrderVO savedOrder = orderRepo.save(order);

        // WebSocket 推送：這個時段重新變成可預約
        publishSlotStatus(order, SLOT_AVAILABLE);

        return savedOrder;
    }

    // =====================
    // 賣家取消：已成立訂單全額退款
    // =====================
    public ServiceOrderVO cancelBySeller(Integer orderId,
                                         String reason) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_CONFIRMED).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有已成立訂單可以由賣家取消");
        }

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());

        order.setOrderStatus(ORDER_REFUNDED);
        order.setCancelledByRole(CANCEL_BY_SELLER);
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());
        order.setRefundStatus(REFUND_FULL);
        order.setRefundAmount(order.getTotalAmount());
        order.setHandledAt(LocalDateTime.now());

        // 已成立訂單取消後，時段重新開放
        releaseSlot(slot);

        slotRepo.save(slot);
        ServiceOrderVO savedOrder = orderRepo.save(order);

        // WebSocket 推送：這個時段重新變成可預約
        publishSlotStatus(order, SLOT_AVAILABLE);

        return savedOrder;
    }

    // =====================
    // 服務完成
    // =====================
    public ServiceOrderVO completeOrder(Integer orderId) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_CONFIRMED).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有已成立訂單可以完成");
        }

        order.setOrderStatus(ORDER_COMPLETED);
        order.setServiceCompletedAt(LocalDateTime.now());

        /*
         * 注意：
         * 服務完成後 slotStatus 不動，維持 2 已預約。
         * 因為這個時段已經被使用過，不應重新開放。
         */

        return orderRepo.save(order);
    }

    // =====================
    // 後台撥款
    // =====================
    public ServiceOrderVO payout(Integer orderId,
                                 Integer employeeId) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_COMPLETED).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有已完成訂單可以撥款");
        }

        if (Byte.valueOf(PAYOUT_PAID).equals(order.getPayoutStatus())) {
            throw new RuntimeException("此訂單已撥款");
        }

        order.setPayoutStatus(PAYOUT_PAID);
        order.setEmployeeId(employeeId);
        order.setHandledAt(LocalDateTime.now());

        return orderRepo.save(order);
    }

    // =====================
    // 賣家確認逾期
    // =====================
    public ServiceOrderVO expireSellerConfirm(Integer orderId) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_PENDING_SELLER_CONFIRM).equals(order.getOrderStatus())) {
            throw new RuntimeException("此訂單不是待賣家確認狀態");
        }

        order.setOrderStatus(ORDER_SELLER_CONFIRM_EXPIRED);
        order.setCancelledAt(LocalDateTime.now());

        /*
         * 新流程：
         * 買家申請時沒有鎖定時段，
         * 所以賣家確認逾期只改訂單狀態，不動 SERVICE_SLOT。
         */

        return orderRepo.save(order);
    }

    // =====================
    // 買家付款逾期
    // =====================
    public ServiceOrderVO expirePayment(Integer orderId) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf(ORDER_PENDING_PAYMENT).equals(order.getOrderStatus())) {
            throw new RuntimeException("此訂單不是待付款狀態");
        }

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());

        if (!Byte.valueOf(SLOT_LOCKED).equals(slot.getSlotStatus())) {
            throw new RuntimeException("此時段目前不是待付款鎖定狀態");
        }

        order.setOrderStatus(ORDER_PAYMENT_EXPIRED);

        // 付款逾期後，釋放時段
        releaseSlot(slot);

        slotRepo.save(slot);
        ServiceOrderVO savedOrder = orderRepo.save(order);

        // WebSocket 推送：這個時段重新變成可預約
        publishSlotStatus(order, SLOT_AVAILABLE);

        return savedOrder;
    }

    // =====================
    // 買方退款規則
    // 7 天以上全退，3~7 天退 50%，3 天內前面已擋掉
    // =====================
    private void applyBuyerRefundRule(ServiceOrderVO order) {

        int refundAmount = calculateRefundAmount(
                order.getTotalAmount(),
                order.getSlotStartTimeSnapshot()
        );

        order.setRefundAmount(refundAmount);
        order.setHandledAt(LocalDateTime.now());
        order.setOrderStatus(ORDER_REFUNDED);

        if (refundAmount == order.getTotalAmount()) {
            order.setRefundStatus(REFUND_FULL);
        } else {
            order.setRefundStatus(REFUND_PARTIAL);
        }
    }

    private int calculateRefundAmount(Integer totalAmount,
                                      LocalDateTime startTime) {

        long daysBeforeStart = Duration.between(
                LocalDateTime.now(),
                startTime
        ).toDays();

        if (daysBeforeStart >= 7) {
            return totalAmount;
        }

        return totalAmount / 2;
    }

    private int calculateTotalAmount(ServiceVO service,
                                     ServiceSlotVO slot) {

        long minutes = Duration.between(
                slot.getStartTime(),
                slot.getEndTime()
        ).toMinutes();

        if (minutes <= 0) {
            throw new RuntimeException("服務時段時間不正確");
        }

        /*
         * 用分鐘算總金額，避免 30 分鐘時段被 toHours() 算成 0。
         * 例如：
         * 300 / 小時，30 分鐘 = 150
         */
        long totalAmount = minutes * service.getHourlyRate() / 60;

        return Math.toIntExact(totalAmount);
    }

    // 釋放時段：把已鎖定或已預約的時段改回可預約
    // 注意：只有在這筆訂單真的有佔用時段時才能呼叫
    private void releaseSlot(ServiceSlotVO slot) {
        slot.setSlotStatus(SLOT_AVAILABLE);
        slot.setLockExpiresAt(null);
    }

    private void publishSlotStatus(ServiceOrderVO order,
                                   Byte slotStatus) {

        slotStatusWebSocketService.publishSlotStatus(
                order.getServiceId(),
                order.getServiceSlotId(),
                slotStatus
        );
    }

    private ServiceOrderVO getOrderOrThrow(Integer orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));
    }

    private ServiceSlotVO getSlotOrThrow(Integer slotId) {
        return slotRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("找不到服務時段"));
    }

    // =====================
    // 查詢方法
    // =====================

    @Transactional(readOnly = true)
    public ServiceOrderVO getOne(Integer orderId) {
        return orderRepo.findById(orderId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderVO> getAll() {
        return orderRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderVO> getByBuyerMemberId(Integer buyerMemberId) {
        return orderRepo.findByBuyerMemberId(buyerMemberId);
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderVO> getBySellerMemberId(Integer sellerMemberId) {
        return orderRepo.findBySellerMemberId(sellerMemberId);
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderVO> getByServiceId(Integer serviceId) {
        return orderRepo.findByServiceId(serviceId);
    }

    @Transactional(readOnly = true)
    public ServiceOrderVO getByServiceSlotId(Integer serviceSlotId) {
        return orderRepo.findByServiceSlotId(serviceSlotId);
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderVO> getByOrderStatus(Byte orderStatus) {
        return orderRepo.findByOrderStatus(orderStatus);
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderVO> getByRefundStatus(Byte refundStatus) {
        return orderRepo.findByRefundStatus(refundStatus);
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderVO> getByPayoutStatus(Byte payoutStatus) {
        return orderRepo.findByPayoutStatus(payoutStatus);
    }
}