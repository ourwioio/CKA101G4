package com.webond.service.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

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

@Controller
@RequestMapping("/member/service-orders")
public class MemberServiceOrderController {

    private final ServiceOrderService serviceOrderSvc;

    public MemberServiceOrderController(ServiceOrderService serviceOrderSvc) {
        this.serviceOrderSvc = serviceOrderSvc;
    }

    // =====================
    // 買家：我的預約 / 訂單列表
    // URL: GET /member/service-orders/buyer
    // =====================
    @GetMapping("/buyer")
    public String buyerOrderList(HttpSession session,
                                 Model model) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        List<ServiceOrderVO> orderList = serviceOrderSvc.getByBuyerMemberId(loginMemberId);

        model.addAttribute("orderList", orderList);
        model.addAttribute("loginMemberId", loginMemberId);

        return "front-end/service/memberBuyerOrderList";
    }

    // =====================
    // 賣家：收到的預約申請 / 訂單列表
    // URL: GET /member/service-orders/seller
    // =====================
    @GetMapping("/seller")
    public String sellerOrderList(HttpSession session,
                                  Model model) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        List<ServiceOrderVO> orderList = serviceOrderSvc.getBySellerMemberId(loginMemberId);

        model.addAttribute("orderList", orderList);
        model.addAttribute("loginMemberId", loginMemberId);

        return "front-end/service/memberSellerOrderList";
    }

    // =====================
    // 賣家同意預約
    // URL: POST /member/service-orders/{orderId}/accept
    // =====================
    @PostMapping("/{orderId}/accept")
    public String acceptOrder(@PathVariable Integer orderId,
                              @RequestParam(required = false) String sellerRequirementNote,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            ServiceOrderVO order = getOrderOrThrow(orderId);

            if (!loginMemberId.equals(order.getSellerMemberId())) {
                throw new IllegalArgumentException("你沒有權限同意這筆預約");
            }

            serviceOrderSvc.acceptRequest(orderId, sellerRequirementNote);

            redirectAttributes.addFlashAttribute("successMsg", "已同意預約，等待買家付款");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/service-orders/seller";
    }

    // =====================
    // 賣家拒絕預約
    // URL: POST /member/service-orders/{orderId}/reject
    // =====================
    @PostMapping("/{orderId}/reject")
    public String rejectOrder(@PathVariable Integer orderId,
                              @RequestParam(required = false) String reason,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            ServiceOrderVO order = getOrderOrThrow(orderId);

            if (!loginMemberId.equals(order.getSellerMemberId())) {
                throw new IllegalArgumentException("你沒有權限拒絕這筆預約");
            }

            serviceOrderSvc.rejectRequest(orderId, reason);

            redirectAttributes.addFlashAttribute("successMsg", "已拒絕預約申請");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/service-orders/seller";
    }

    // =====================
    // 買家付款成功
    // 目前先做測試用，之後可以接金流
    // URL: POST /member/service-orders/{orderId}/pay
    // =====================
    @PostMapping("/{orderId}/pay")
    public String payOrder(@PathVariable Integer orderId,
                           @RequestParam(defaultValue = "0") Byte paymentMethod,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            ServiceOrderVO order = getOrderOrThrow(orderId);

            if (!loginMemberId.equals(order.getBuyerMemberId())) {
                throw new IllegalArgumentException("你沒有權限付款這筆訂單");
            }

            serviceOrderSvc.paySuccess(orderId, paymentMethod);

            redirectAttributes.addFlashAttribute("successMsg", "付款成功，訂單已成立");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/service-orders/buyer";
    }

    // =====================
    // 買家取消已成立訂單
    // URL: POST /member/service-orders/{orderId}/cancel-by-buyer
    // =====================
    @PostMapping("/{orderId}/cancel-by-buyer")
    public String cancelByBuyer(@PathVariable Integer orderId,
                                @RequestParam(required = false) String reason,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            ServiceOrderVO order = getOrderOrThrow(orderId);

            if (!loginMemberId.equals(order.getBuyerMemberId())) {
                throw new IllegalArgumentException("你沒有權限取消這筆訂單");
            }

            serviceOrderSvc.cancelByBuyer(orderId, reason);

            redirectAttributes.addFlashAttribute("successMsg", "訂單已取消，退款規則已套用");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/service-orders/buyer";
    }

    // =====================
    // 賣家取消已成立訂單
    // URL: POST /member/service-orders/{orderId}/cancel-by-seller
    // =====================
    @PostMapping("/{orderId}/cancel-by-seller")
    public String cancelBySeller(@PathVariable Integer orderId,
                                 @RequestParam(required = false) String reason,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            ServiceOrderVO order = getOrderOrThrow(orderId);

            if (!loginMemberId.equals(order.getSellerMemberId())) {
                throw new IllegalArgumentException("你沒有權限取消這筆訂單");
            }

            serviceOrderSvc.cancelBySeller(orderId, reason);

            redirectAttributes.addFlashAttribute("successMsg", "訂單已取消，將全額退款給買家");

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/service-orders/seller";
    }

    // =====================
    // 共用：從 Session 取登入會員 ID
    // =====================
    private Integer getLoginMemberId(HttpSession session) {

        MemberVO memberVO = (MemberVO) session.getAttribute("memberVO");

        if (memberVO == null) {
            return null;
        }

        return memberVO.getMemberId();
    }

    // =====================
    // 共用：查訂單
    // =====================
    private ServiceOrderVO getOrderOrThrow(Integer orderId) {

        ServiceOrderVO order = serviceOrderSvc.getOne(orderId);

        if (order == null) {
            throw new IllegalArgumentException("查無此訂單");
        }

        return order;
    }
}