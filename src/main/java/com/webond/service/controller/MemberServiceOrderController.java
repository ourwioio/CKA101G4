package com.webond.service.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.service.ServiceOrderService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member/service-orders")
public class MemberServiceOrderController {

    // 退款狀態：1 待退款
    private static final byte REFUND_PENDING = 1;

    private final ServiceOrderService serviceOrderSvc;

    public MemberServiceOrderController(
            ServiceOrderService serviceOrderSvc) {

        this.serviceOrderSvc = serviceOrderSvc;
    }

    // =========================================================
    // 買家：我的預約／訂單列表
    //
    // GET /member/service-orders/buyer
    // =========================================================

    @GetMapping("/buyer")
    public String buyerOrderList(
            HttpSession session,
            Model model) {

        Integer loginMemberId =
                getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        List<ServiceOrderVO> orderList =
                serviceOrderSvc.getByBuyerMemberId(
                        loginMemberId
                );

        model.addAttribute(
                "orderList",
                orderList
        );

        model.addAttribute(
                "loginMemberId",
                loginMemberId
        );

        return "front-end/service/memberBuyerOrderList";
    }

    // =========================================================
    // 賣家：收到的預約申請／訂單列表
    //
    // GET /member/service-orders/seller
    // =========================================================

    @GetMapping("/seller")
    public String sellerOrderList(
            HttpSession session,
            Model model) {

        Integer loginMemberId =
                getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        List<ServiceOrderVO> orderList =
                serviceOrderSvc.getBySellerMemberId(
                        loginMemberId
                );

        model.addAttribute(
                "orderList",
                orderList
        );

        model.addAttribute(
                "loginMemberId",
                loginMemberId
        );

        return "front-end/service/memberSellerOrderList";
    }

    // =========================================================
    // 賣家同意預約
    //
    // 執行後：
    // 1. 該訂單變成待買家付款
    // 2. 時段鎖定30秒
    // 3. WebSocket推播時段鎖定
    // 4. 其他同時段申請由系統取消
    //
    // POST /member/service-orders/{orderId}/accept
    // =========================================================

    @PostMapping("/{orderId}/accept")
    public String acceptOrder(
            @PathVariable Integer orderId,
            @RequestParam(required = false)
            String sellerRequirementNote,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginMemberId =
                getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            getSellerOrderOrThrow(
                    orderId,
                    loginMemberId
            );

            serviceOrderSvc.acceptRequest(
                    orderId,
                    sellerRequirementNote
            );

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "已接受預約，時段已鎖定30秒，等待買家付款"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return "redirect:/member/service-orders/seller";
    }

    // =========================================================
    // 賣家拒絕預約
    //
    // 只有待賣家確認的訂單可以拒絕。
    // 買家尚未付款，所以不需要退款。
    //
    // POST /member/service-orders/{orderId}/reject
    // =========================================================

    @PostMapping("/{orderId}/reject")
    public String rejectOrder(
            @PathVariable Integer orderId,
            @RequestParam(required = false)
            String reason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginMemberId =
                getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            getSellerOrderOrThrow(
                    orderId,
                    loginMemberId
            );

            serviceOrderSvc.rejectRequest(
                    orderId,
                    reason
            );

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "已拒絕預約申請"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return "redirect:/member/service-orders/seller";
    }

    // =========================================================
    // 買家付款成功
    //
    // 目前為測試付款流程。
    // 正式版本再接信用卡或ATM金流。
    //
    // POST /member/service-orders/{orderId}/pay
    // =========================================================

    @PostMapping("/{orderId}/pay")
    public String payOrder(
            @PathVariable Integer orderId,
            @RequestParam(defaultValue = "0")
            Byte paymentMethod,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginMemberId =
                getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            getBuyerOrderOrThrow(
                    orderId,
                    loginMemberId
            );

            serviceOrderSvc.paySuccess(
                    orderId,
                    paymentMethod
            );

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "付款成功，訂單已成立"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return "redirect:/member/service-orders/buyer";
    }

    // =========================================================
    // 買家取消已成立訂單
    //
    // 超過服務開始時間3天：
    // 已取消＋待退款。
    //
    // 服務開始時間3天內：
    // 已取消＋無退款。
    //
    // POST /member/service-orders/{orderId}/cancel-by-buyer
    // =========================================================

    @PostMapping("/{orderId}/cancel-by-buyer")
    public String cancelByBuyer(
            @PathVariable Integer orderId,
            @RequestParam(required = false)
            String reason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginMemberId =
                getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            getBuyerOrderOrThrow(
                    orderId,
                    loginMemberId
            );

            ServiceOrderVO cancelledOrder =
                    serviceOrderSvc.cancelByBuyer(
                            orderId,
                            reason
                    );

            if (Byte.valueOf(REFUND_PENDING)
                    .equals(
                            cancelledOrder.getRefundStatus()
                    )) {

                redirectAttributes.addFlashAttribute(
                        "successMsg",
                        "訂單已取消，等待後台處理退款"
                );

            } else {

                redirectAttributes.addFlashAttribute(
                        "successMsg",
                        "訂單已取消，本次取消不符合退款條件"
                );
            }

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return "redirect:/member/service-orders/buyer";
    }

    // =========================================================
    // 賣家取消訂單
    //
    // 賣家可以取消：
    // 0 待賣家確認
    // 1 待買家付款
    // 2 已成立
    //
    // 已成立訂單取消：
    // 已取消＋待退款。
    //
    // 尚未付款訂單取消：
    // 已取消＋無退款。
    //
    // POST /member/service-orders/{orderId}/cancel-by-seller
    // =========================================================

    @PostMapping("/{orderId}/cancel-by-seller")
    public String cancelBySeller(
            @PathVariable Integer orderId,
            @RequestParam(required = false)
            String reason,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginMemberId =
                getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            getSellerOrderOrThrow(
                    orderId,
                    loginMemberId
            );

            ServiceOrderVO cancelledOrder =
                    serviceOrderSvc.cancelBySeller(
                            orderId,
                            reason
                    );

            if (Byte.valueOf(REFUND_PENDING)
                    .equals(
                            cancelledOrder.getRefundStatus()
                    )) {

                redirectAttributes.addFlashAttribute(
                        "successMsg",
                        "訂單已取消，將由後台辦理全額退款"
                );

            } else {

                redirectAttributes.addFlashAttribute(
                        "successMsg",
                        "訂單已取消"
                );
            }

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return "redirect:/member/service-orders/seller";
    }

    // =========================================================
    // 共用：取得登入會員ID
    //
    // 同時支援：
    // 1. loginMemberId
    // 2. memberVO
    // =========================================================

    private Integer getLoginMemberId(
            HttpSession session) {

        Object loginMemberId =
                session.getAttribute(
                        "loginMemberId"
                );

        if (loginMemberId instanceof Integer memberId) {
            return memberId;
        }

        Object memberObject =
                session.getAttribute(
                        "memberVO"
                );

        if (memberObject instanceof MemberVO memberVO) {
            return memberVO.getMemberId();
        }

        return null;
    }

    // =========================================================
    // 共用：確認訂單屬於目前買家
    // =========================================================

    private ServiceOrderVO getBuyerOrderOrThrow(
            Integer orderId,
            Integer loginMemberId) {

        ServiceOrderVO order =
                getOrderOrThrow(orderId);

        if (!loginMemberId.equals(
                order.getBuyerMemberId()
        )) {

            throw new IllegalArgumentException(
                    "你沒有權限操作這筆訂單"
            );
        }

        return order;
    }

    // =========================================================
    // 共用：確認訂單屬於目前賣家
    // =========================================================

    private ServiceOrderVO getSellerOrderOrThrow(
            Integer orderId,
            Integer loginMemberId) {

        ServiceOrderVO order =
                getOrderOrThrow(orderId);

        if (!loginMemberId.equals(
                order.getSellerMemberId()
        )) {

            throw new IllegalArgumentException(
                    "你沒有權限操作這筆訂單"
            );
        }

        return order;
    }

    // =========================================================
    // 共用：查詢訂單
    // =========================================================

    private ServiceOrderVO getOrderOrThrow(
            Integer orderId) {

        ServiceOrderVO order =
                serviceOrderSvc.getOne(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "查無此訂單"
            );
        }

		return order;
	}
}