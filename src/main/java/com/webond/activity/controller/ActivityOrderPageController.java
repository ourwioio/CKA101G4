package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityService;

@Controller
@RequestMapping("/activityOrder")
public class ActivityOrderPageController {

    @Autowired
    private ActivityOrderService orderSvc;

    @Autowired
    private ActivityService activitySvc;

    // 查詢全部
    @GetMapping("/listAllActivityOrder")
    public String listAllActivityOrder(Model model) {

        model.addAttribute("orderListData", orderSvc.getAll());

        // 活動下拉選單
        model.addAttribute("activityListData", activitySvc.getAll());

        return "front-end/activityorder/listAllActivityOrder";
    }

    // 新增頁面
    @GetMapping("/addActivityOrder")
    public String addActivityOrder(Model model) {

        model.addAttribute("activityOrderVO", new ActivityOrderVO());

        model.addAttribute("activityListData", activitySvc.getAll());

        return "front-end/activityorder/addActivityOrder";
    }

    // 新增
    @PostMapping("/insert")
    public String insert(
            @Valid @ModelAttribute("activityOrderVO") ActivityOrderVO orderVO,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {

            model.addAttribute("activityListData", activitySvc.getAll());

            return "front-end/activityorder/addActivityOrder";
        }

        orderSvc.addOrder(orderVO);

        return "redirect:/activityOrder/listAllActivityOrder";
    }

    // 修改頁面
    @GetMapping("/updateActivityOrder")
    public String updateActivityOrder(
            @RequestParam("id") Integer activityOrderId,
            Model model) {

        ActivityOrderVO orderVO = orderSvc.getOneOrder(activityOrderId);

        if (orderVO == null) {
            return "redirect:/activityOrder/listAllActivityOrder";
        }

        model.addAttribute("activityOrderVO", orderVO);

        model.addAttribute("activityListData", activitySvc.getAll());

        return "front-end/activityorder/updateActivityOrder";
    }

    // 修改
    @PostMapping("/update")
    public String update(
            @Valid @ModelAttribute("activityOrderVO") ActivityOrderVO formVO,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {

            model.addAttribute("activityListData", activitySvc.getAll());

            return "front-end/activityorder/updateActivityOrder";
        }

        ActivityOrderVO orderVO = orderSvc.getOneOrder(formVO.getActivityOrderId());

        if (orderVO == null) {
            return "redirect:/activityOrder/listAllActivityOrder";
        }

        orderVO.setActivityId(formVO.getActivityId());
        orderVO.setBuyerMemberId(formVO.getBuyerMemberId());
        orderVO.setEmployeeId(formVO.getEmployeeId());

        orderVO.setOrderStatus(formVO.getOrderStatus());

        orderVO.setBookingCount(formVO.getBookingCount());

        orderVO.setActivityPrice(formVO.getActivityPrice());

        orderVO.setTotalAmount(formVO.getTotalAmount());

        orderVO.setOrderNote(formVO.getOrderNote());

        orderVO.setActivityPaymentMethod(formVO.getActivityPaymentMethod());

        orderVO.setPaidAt(formVO.getPaidAt());

        orderVO.setActivityCompletedAt(formVO.getActivityCompletedAt());

        orderVO.setBuyerRateSeller(formVO.getBuyerRateSeller());

        orderVO.setBuyerReviewComment(formVO.getBuyerReviewComment());

        orderVO.setBuyerReviewedAt(formVO.getBuyerReviewedAt());

        orderVO.setSellerRateBuyer(formVO.getSellerRateBuyer());

        orderVO.setSellerReviewComment(formVO.getSellerReviewComment());

        orderVO.setSellerReviewedAt(formVO.getSellerReviewedAt());

        orderVO.setPayoutAmount(formVO.getPayoutAmount());

        orderVO.setRefundReason(formVO.getRefundReason());

        orderVO.setRefundStatus(formVO.getRefundStatus());

        orderSvc.updateOrder(orderVO);

        return "redirect:/activityOrder/listAllActivityOrder";
    }

    // 刪除
    @PostMapping("/deleteActivityOrder")
    public String deleteActivityOrder(
            @RequestParam("activityOrderId") Integer activityOrderId) {

        orderSvc.deleteOrder(activityOrderId);

        return "redirect:/activityOrder/listAllActivityOrder";
    }

}