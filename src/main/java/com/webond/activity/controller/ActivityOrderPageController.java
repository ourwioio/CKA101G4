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

        // 下拉式選單使用
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

        orderSvc.createOrder(orderVO);

        return "redirect:/activityOrder/listAllActivityOrder";
    }

    // 修改頁面
    @GetMapping("/updateActivityOrder")
    public String updateActivityOrder(
            @RequestParam("id") Integer id,
            Model model) {

        ActivityOrderVO orderVO = orderSvc.getOneOrder(id);

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

        orderVO.setActivityId(formVO.getActivityId());
        orderVO.setMemberId(formVO.getMemberId());
        orderVO.setOrderTotal(formVO.getOrderTotal());
        orderVO.setPaymentStatus(formVO.getPaymentStatus());

        orderSvc.createOrder(orderVO);

        return "redirect:/activityOrder/listAllActivityOrder";
    }

    // 刪除
    @PostMapping("/deleteActivityOrder")
    public String deleteActivityOrder(
            @RequestParam("activityOrderId") Integer id) {

        orderSvc.deleteOrder(id);

        return "redirect:/activityOrder/listAllActivityOrder";
    }

}