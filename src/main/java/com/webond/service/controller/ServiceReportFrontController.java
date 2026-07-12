package com.webond.service.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.service.dto.ServiceReportDTO;
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.repository.ServiceOrderRepository;
import com.webond.service.service.ServiceReportService;

@Controller
@RequestMapping("front/serviceReport")
public class ServiceReportFrontController {

    @Autowired
    private ServiceReportService serviceReportService;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    // 訂單完成頁的「檢舉」按鈕會轉跳到這裡，只帶 orderId
    @GetMapping("/create")
    public String showCreateForm(@RequestParam Integer orderId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);
        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            serviceReportService.checkCanReport(orderId, loginMemberId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return backToOrderList(orderId, loginMemberId);
        }

        ServiceOrderVO order = serviceOrderRepository.findById(orderId).orElseThrow();

        model.addAttribute("order", order);
        model.addAttribute("serviceReportDTO", new ServiceReportDTO());
        return "front-end/service/serviceReportCreate";
    }

    @PostMapping("/submit")
    public String submit(@RequestParam Integer orderId,
                          @Valid @ModelAttribute("serviceReportDTO") ServiceReportDTO serviceReportDTO,
                          BindingResult bindingResult,
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);
        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        if (bindingResult.hasErrors()) {
            ServiceOrderVO order = serviceOrderRepository.findById(orderId).orElseThrow();
            model.addAttribute("order", order);
            return "front-end/service/serviceReportCreate";
        }

        try {
            serviceReportService.submitReport(orderId, loginMemberId, serviceReportDTO);
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return backToOrderList(orderId, loginMemberId);
        }

        redirectAttributes.addFlashAttribute("successMessage", "檢舉已送出，等待後台審核");
        return backToOrderList(orderId, loginMemberId);
    }

    // =====================
    // 共用：從 Session 取登入會員 ID（沿用訂單模組的 key 名稱 "memberVO"）
    // =====================
    private Integer getLoginMemberId(HttpSession session) {
        MemberVO memberVO = (MemberVO) session.getAttribute("memberVO");
        if (memberVO == null) {
            return null;
        }
        return memberVO.getMemberId();
    }

    // =====================
    // 共用：依登入者是買家還是賣家，導回對應的訂單列表頁
    // =====================
    private String backToOrderList(Integer orderId, Integer loginMemberId) {
        ServiceOrderVO order = serviceOrderRepository.findById(orderId).orElse(null);

        if (order != null && loginMemberId.equals(order.getBuyerMemberId())) {
            return "redirect:/member/service-orders/buyer";
        }
        return "redirect:/member/service-orders/seller";
    }
}