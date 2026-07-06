package com.webond.service.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.service.model.ServiceOrderVO;
import com.webond.service.service.ServiceOrderService;

@Controller
@RequestMapping("/admin/service-orders")
public class AdminServiceOrderController {

    private final ServiceOrderService serviceOrderSvc;

    public AdminServiceOrderController(ServiceOrderService serviceOrderSvc) {
        this.serviceOrderSvc = serviceOrderSvc;
    }

    // 後台：查看全部服務訂單 / 依訂單狀態查詢
    // URL:
    // GET /admin/service-orders
    // GET /admin/service-orders?orderStatus=2
    @GetMapping
    public String listOrders(@RequestParam(required = false) Byte orderStatus,
                             Model model) {

        List<ServiceOrderVO> orderList;

        if (orderStatus == null) {
            orderList = serviceOrderSvc.getAll();
        } else {
            orderList = serviceOrderSvc.getByOrderStatus(orderStatus);
        }

        model.addAttribute("orderList", orderList);
        model.addAttribute("selectedOrderStatus", orderStatus);

        return "back-end/service/adminServiceOrderList";
    }
}