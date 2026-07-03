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

    private final ServiceOrderRepository orderRepo;
    private final ServiceRepository serviceRepo;
    private final ServiceSlotRepository slotRepo;
    private final SlotStatusWebSocketService slotStatusWebSocketService;

    public ServiceOrderService(
            ServiceOrderRepository orderRepo,
            ServiceRepository serviceRepo,
            ServiceSlotRepository slotRepo,
            SlotStatusWebSocketService slotStatusWebSocketService
            ) {
        this.orderRepo = orderRepo;
        this.serviceRepo = serviceRepo;
        this.slotRepo = slotRepo;
        this.slotStatusWebSocketService = slotStatusWebSocketService;
    }

    // 買家送出預約申請
    public ServiceOrderVO createRequest(Integer slotId, Integer buyerId, String buyerRequestNote) {

        ServiceSlotVO slot = getSlotOrThrow(slotId);

        if (!Byte.valueOf((byte) 0).equals(slot.getSlotStatus())) {
            throw new RuntimeException("此時段不可預約");
        }

        ServiceVO service = serviceRepo.findById(slot.getServiceId())
                .orElseThrow(() -> new RuntimeException("找不到服務"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sellerConfirmExpiresAt = now.plusHours(24);

        ServiceOrderVO order = new ServiceOrderVO();

        order.setServiceSlotId(slotId);
        order.setServiceId(service.getServiceId());
        order.setBuyerMemberId(buyerId);
        order.setOrderHourlyRate(service.getHourlyRate());
        order.setTotalAmount(calculateTotalAmount(service, slot));
        order.setOrderStatus((byte) 0); // 待賣家確認
        order.setBuyerRequestNote(buyerRequestNote);
        order.setCreatedAt(now);

        order.setRefundStatus((byte) 0); // 無退款
        order.setRefundAmount(0);
        order.setPayoutStatus((byte) 0); // 未撥款

        order.setSellerConfirmExpiresAt(sellerConfirmExpiresAt);
        order.setPaymentExpiresAt(null);

        slot.setSlotStatus((byte) 1); // 暫時鎖定
        slot.setLockExpiresAt(sellerConfirmExpiresAt);

        slotRepo.save(slot);
        return orderRepo.save(order);
    }

    // 賣家接受預約
    public ServiceOrderVO acceptRequest(Integer orderId, String sellerRequirementNote) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf((byte) 0).equals(order.getOrderStatus())) {
            throw new RuntimeException("此訂單不是待賣家確認狀態");
        }

        LocalDateTime paymentExpiresAt = LocalDateTime.now().plusMinutes(30);

        order.setOrderStatus((byte) 1); // 待買家付款
        order.setSellerRequirementNote(sellerRequirementNote);
        order.setPaymentExpiresAt(paymentExpiresAt);

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());
        slot.setLockExpiresAt(paymentExpiresAt);

        slotRepo.save(slot);
        return orderRepo.save(order);
    }

    // 賣家拒絕預約
    public ServiceOrderVO rejectRequest(Integer orderId, String reason) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf((byte) 0).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有待賣家確認的訂單可以拒絕");
        }

        order.setOrderStatus((byte) 4); // 已取消
        order.setCancelledByRole((byte) 1); // 賣家取消
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());
        releaseSlot(slot);

        slotRepo.save(slot);
        return orderRepo.save(order);
    }

    // 買家付款成功
    public ServiceOrderVO paySuccess(Integer orderId, Byte paymentMethod) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf((byte) 1).equals(order.getOrderStatus())) {
            throw new RuntimeException("此訂單不是待付款狀態");
        }

        if (order.getPaymentExpiresAt() != null &&
                LocalDateTime.now().isAfter(order.getPaymentExpiresAt())) {
            throw new RuntimeException("付款期限已過");
        }

        order.setOrderStatus((byte) 2); // 已成立
        order.setServicePaymentMethod(paymentMethod);

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());
        slot.setSlotStatus((byte) 2); // 已預約
        slot.setLockExpiresAt(null);

        slotRepo.save(slot);
        return orderRepo.save(order);
    }

    // 買家取消：三天內不能取消
    public ServiceOrderVO cancelByBuyer(Integer orderId, String reason) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf((byte) 2).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有已成立訂單可以由買家取消");
        }

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());

        long daysBeforeStart = Duration.between(
                LocalDateTime.now(),
                slot.getStartTime()
        ).toDays();

        if (daysBeforeStart < 3) {
            throw new RuntimeException("距離服務開始未滿 3 天，無法取消訂單");
        }

        order.setCancelledByRole((byte) 0); // 買家取消
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());

        applyBuyerRefundRule(order, slot);
        releaseSlot(slot);

        slotRepo.save(slot);
        return orderRepo.save(order);
    }

    // 賣家取消：已成立訂單全額退款
    public ServiceOrderVO cancelBySeller(Integer orderId, String reason) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf((byte) 2).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有已成立訂單可以由賣家取消");
        }

        order.setOrderStatus((byte) 5); // 已退款
        order.setCancelledByRole((byte) 1); // 賣家取消
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());
        order.setRefundStatus((byte) 2); // 已退款
        order.setRefundAmount(order.getTotalAmount());
        order.setHandledAt(LocalDateTime.now());

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());
        releaseSlot(slot);

        slotRepo.save(slot);
        return orderRepo.save(order);
    }

    // 服務完成
    public ServiceOrderVO completeOrder(Integer orderId) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf((byte) 2).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有已成立訂單可以完成");
        }

        order.setOrderStatus((byte) 3); // 已完成
        order.setServiceCompletedAt(LocalDateTime.now());

        return orderRepo.save(order);
    }

    // 後台撥款
    public ServiceOrderVO payout(Integer orderId, Integer employeeId) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf((byte) 3).equals(order.getOrderStatus())) {
            throw new RuntimeException("只有已完成訂單可以撥款");
        }

        if (Byte.valueOf((byte) 1).equals(order.getPayoutStatus())) {
            throw new RuntimeException("此訂單已撥款");
        }

        order.setPayoutStatus((byte) 1); // 已撥款
        order.setEmployeeId(employeeId);
        order.setHandledAt(LocalDateTime.now());

        return orderRepo.save(order);
    }

    // 賣家確認逾期
    public ServiceOrderVO expireSellerConfirm(Integer orderId) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf((byte) 0).equals(order.getOrderStatus())) {
            throw new RuntimeException("此訂單不是待賣家確認狀態");
        }

        order.setOrderStatus((byte) 6); // 賣家確認逾期

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());
        releaseSlot(slot);

        slotRepo.save(slot);
        return orderRepo.save(order);
    }

    // 買家付款逾期
    public ServiceOrderVO expirePayment(Integer orderId) {

        ServiceOrderVO order = getOrderOrThrow(orderId);

        if (!Byte.valueOf((byte) 1).equals(order.getOrderStatus())) {
            throw new RuntimeException("此訂單不是待付款狀態");
        }

        order.setOrderStatus((byte) 7); // 買家付款逾期

        ServiceSlotVO slot = getSlotOrThrow(order.getServiceSlotId());
        releaseSlot(slot);

        slotRepo.save(slot);
        return orderRepo.save(order);
    }

    // 買方退款規則：7天以上全退，3~7天退50%，3天內前面已擋掉
    private void applyBuyerRefundRule(ServiceOrderVO order, ServiceSlotVO slot) {

        int refundAmount = calculateRefundAmount(order.getTotalAmount(), slot.getStartTime());

        order.setRefundAmount(refundAmount);
        order.setHandledAt(LocalDateTime.now());

        if (refundAmount == order.getTotalAmount()) {
            order.setOrderStatus((byte) 5);  // 已退款
            order.setRefundStatus((byte) 2); // 已退款
        } else {
            order.setOrderStatus((byte) 5);  // 已退款
            order.setRefundStatus((byte) 3); // 部分退款
        }
    }

    private int calculateRefundAmount(Integer totalAmount, LocalDateTime startTime) {

        long daysBeforeStart = Duration.between(
                LocalDateTime.now(),
                startTime
        ).toDays();

        if (daysBeforeStart >= 7) {
            return totalAmount;
        }

        return totalAmount / 2;
    }

    private int calculateTotalAmount(ServiceVO service, ServiceSlotVO slot) {

        long hours = Duration.between(
                slot.getStartTime(),
                slot.getEndTime()
        ).toHours();

        if (hours <= 0) {
            throw new RuntimeException("服務時段時間不正確");
        }

        return Math.toIntExact(hours * service.getHourlyRate());
    }

    private void releaseSlot(ServiceSlotVO slot) {
        slot.setSlotStatus((byte) 0); // 可預約
        slot.setLockExpiresAt(null);
    }

    private ServiceOrderVO getOrderOrThrow(Integer orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));
    }

    private ServiceSlotVO getSlotOrThrow(Integer slotId) {
        return slotRepo.findById(slotId)
                .orElseThrow(() -> new RuntimeException("找不到服務時段"));
    }

    public ServiceOrderVO getOne(Integer orderId) {
        return orderRepo.findById(orderId).orElse(null);
    }

    public List<ServiceOrderVO> getAll() {
        return orderRepo.findAll();
    }

    public List<ServiceOrderVO> getByBuyerMemberId(Integer buyerMemberId) {
        return orderRepo.findByBuyerMemberId(buyerMemberId);
    }

    public List<ServiceOrderVO> getBySellerMemberId(Integer sellerMemberId) {
        return orderRepo.findBySellerMemberId(sellerMemberId);
    }

    public List<ServiceOrderVO> getByServiceId(Integer serviceId) {
        return orderRepo.findByServiceId(serviceId);
    }

    public ServiceOrderVO getByServiceSlotId(Integer serviceSlotId) {
        return orderRepo.findByServiceSlotId(serviceSlotId);
    }

    public List<ServiceOrderVO> getByOrderStatus(Byte orderStatus) {
        return orderRepo.findByOrderStatus(orderStatus);
    }

    public List<ServiceOrderVO> getByRefundStatus(Byte refundStatus) {
        return orderRepo.findByRefundStatus(refundStatus);
    }

    public List<ServiceOrderVO> getByPayoutStatus(Byte payoutStatus) {
        return orderRepo.findByPayoutStatus(payoutStatus);
    }
}