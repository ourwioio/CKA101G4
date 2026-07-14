package com.webond.orderManagement.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.repository.ActivityOrderRepository;
import com.webond.orderManagement.dto.RevenueQueryDTO;
import com.webond.orderManagement.dto.RevenueSummaryDTO;
import com.webond.orderManagement.dto.TransactionDetailDTO;
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.repository.ServiceOrderRepository;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.repository.VenueOrderRepository;

@Service
public class TransactionRevenueService {

    @Autowired private ServiceOrderRepository serviceOrderRepository;
    @Autowired private ActivityOrderRepository activityOrderRepository;
    @Autowired private VenueOrderRepository venueOrderRepository;

    public List<TransactionDetailDTO> listAllTransactions(RevenueQueryDTO query) {
        query.resolveRange();
        LocalDateTime start = query.getStartDate();
        LocalDateTime end = query.getEndDate();

        List<TransactionDetailDTO> result = new ArrayList<>();

        serviceOrderRepository.findAll().stream()
            .filter(o -> o.getOrderStatus() == 3 || o.getOrderStatus() == 5) // 已完成、已退款
            .filter(o -> isInRange(o.getCreatedAt(), start, end))
            .forEach(o -> result.add(mapServiceOrder(o)));

        activityOrderRepository.findAll().stream()
            .filter(o -> o.getOrderStatus() == 0 || o.getOrderStatus() == 2) // 已完成、已退款
            .filter(o -> isInRange(o.getPaidAt(), start, end))
            .forEach(o -> result.add(mapActivityOrder(o)));

        venueOrderRepository.findAll().stream()
            .filter(o -> o.getOrderStatus() == 3 || o.getRefundStatus() == 1) // 已完成、已付款取消
            .filter(o -> isInRange(o.getCreatedAt(), start, end))
            .forEach(o -> result.add(mapVenueOrder(o)));

        return result.stream()
                .filter(t -> query.getOrderType() == null || query.getOrderType().isEmpty() || t.getOrderType().equals(query.getOrderType()))
                .filter(t -> query.getOrderStatus() == null || t.getOrderStatus() == query.getOrderStatus())
                .sorted(Comparator.comparing(TransactionDetailDTO::getTransactionTime).reversed())
                .toList(); // Java 16+ 寫法，舊版用 .collect(Collectors.toList())
    }

    public RevenueSummaryDTO calculateRevenue(RevenueQueryDTO query) {
        List<TransactionDetailDTO> all = listAllTransactions(query);

        BigDecimal serviceRevenue = sumByType(all, "服務");
        BigDecimal activityRevenue = sumByType(all, "活動");
        BigDecimal venueRevenue = sumByType(all, "場地");
        BigDecimal total = serviceRevenue.add(activityRevenue).add(venueRevenue);

        RevenueSummaryDTO summary = new RevenueSummaryDTO();
        summary.setServiceRevenue(serviceRevenue);
        summary.setActivityRevenue(activityRevenue);
        summary.setVenueRevenue(venueRevenue);
        summary.setTotalRevenue(total);
        summary.setRangeStart(query.getStartDate());
        summary.setRangeEnd(query.getEndDate());
        return summary;
    }

    private BigDecimal sumByType(List<TransactionDetailDTO> list, String type) {
        return list.stream()
            .filter(d -> d.getOrderType().equals(type) && Boolean.TRUE.equals(d.getIsRevenue()))
            .map(TransactionDetailDTO::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private TransactionDetailDTO mapServiceOrder(ServiceOrderVO o) {
        TransactionDetailDTO dto = new TransactionDetailDTO();
        dto.setOrderType("服務");
        dto.setOrderId(o.getServiceOrderId());
        dto.setTotalAmount(BigDecimal.valueOf(o.getTotalAmount()));
        dto.setOrderStatus(o.getOrderStatus());
        dto.setStatusLabel(resolveServiceStatusLabel(o.getOrderStatus()));
        dto.setTransactionTime(o.getCreatedAt());
        dto.setIsRevenue(o.getOrderStatus() == 2 || o.getOrderStatus() == 3 || o.getCancelledByRole() == 1);
        dto.setCategory(o.getOrderStatus() == 3 ? "已完成" : "已退款");
        return dto;
    }

    private TransactionDetailDTO mapActivityOrder(ActivityOrderVO o) {
        TransactionDetailDTO dto = new TransactionDetailDTO();
        dto.setOrderType("活動");
        dto.setOrderId(o.getActivityOrderId());
        dto.setTotalAmount(BigDecimal.valueOf(o.getTotalAmount()));
        dto.setOrderStatus(o.getOrderStatus());
        dto.setStatusLabel(resolveActivityStatusLabel(o.getOrderStatus()));
        dto.setTransactionTime(o.getPaidAt());
        dto.setIsRevenue(o.getOrderStatus() == 0);
        dto.setCategory(o.getOrderStatus() == 0 ? "已完成" : "已退款");
        return dto;
    }

    private TransactionDetailDTO mapVenueOrder(VenueOrderVO o) {
        TransactionDetailDTO dto = new TransactionDetailDTO();
        dto.setOrderType("場地");
        dto.setOrderId(o.getVenueOrderId());
        dto.setTotalAmount(BigDecimal.valueOf(o.getTotalAmount()));
        dto.setOrderStatus(o.getOrderStatus());
        dto.setStatusLabel(resolveVenueStatusLabel(o.getOrderStatus()));
        dto.setTransactionTime(o.getCreatedAt());
        dto.setIsRevenue(o.getOrderStatus() == 1 || o.getOrderStatus() == 3);
        dto.setCategory(o.getOrderStatus() == 3 ? "已完成" : "已退款");
        return dto;
    }

    private String resolveServiceStatusLabel(int status) {
        return switch (status) {
            case 3 -> "已完成";
            case 5 -> "已退款";
		default -> throw new IllegalArgumentException("Unexpected value: " + status);
        };
    }

    private String resolveActivityStatusLabel(int status) {
        return switch (status) {
            case 0 -> "已完成";
            case 2 -> "已退款";
		default -> throw new IllegalArgumentException("Unexpected value: " + status);
        };
    }

    private String resolveVenueStatusLabel(int status) {
        return switch (status) {
            case 3 -> "已完成";
            case 4 -> "已退款";
		default -> throw new IllegalArgumentException("Unexpected value: " + status);
        };
    }

    private boolean isInRange(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        if (target == null) return false;
        if (start != null && target.isBefore(start)) return false;
        if (end != null && target.isAfter(end)) return false;
        return true;
    }
}