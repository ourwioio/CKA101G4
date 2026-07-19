package com.webond.orderManagement.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.orderManagement.dto.RevenueQueryDTO;
import com.webond.orderManagement.dto.RevenueSummaryDTO;
import com.webond.orderManagement.dto.TransactionDetailDTO;
import com.webond.orderManagement.service.TransactionRevenueService;

@Controller
@RequestMapping("/admin/transaction")
public class TransactionRevenueController {

    @Autowired private TransactionRevenueService revenueService;

    @GetMapping("/revenue")
    public String showRevenue(
            @RequestParam(defaultValue = "thisMonth") String quickRange,
            @RequestParam(required = false) String orderType,   
            @RequestParam(required = false) Byte orderStatus, 
            @RequestParam(required = false) String category, 
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        RevenueQueryDTO query = new RevenueQueryDTO();
        query.setQuickRange(quickRange); 
        
        if ("custom".equals(quickRange)) {
            query.setStartDate(startDate != null ? startDate.atStartOfDay() : null);
            query.setEndDate(endDate != null ? endDate.atTime(23, 59, 59) : null);
        }
        

        List<TransactionDetailDTO> transactions = revenueService.listAllTransactions(query);

        if (orderType != null && !orderType.isEmpty()) {
            transactions = transactions.stream()
                .filter(t -> t.getOrderType().equals(orderType))
                .collect(Collectors.toList());
        }

        if (category != null && !category.isEmpty()) {
            transactions = transactions.stream()
                .filter(t -> t.getCategory().equals(category))
                .collect(Collectors.toList());
        }

        RevenueSummaryDTO summary = revenueService.calculateRevenue(query);

        model.addAttribute("revenueSummary", summary);
        model.addAttribute("selectedRange", quickRange);
        model.addAttribute("selectedType", orderType);     
        model.addAttribute("selectedCategory", category); 
        model.addAttribute("transactions", transactions);
        
        return "back-end/orderManagement/transactionRevenue";
    }
}
