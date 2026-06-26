package com.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.activity.model.ActivityOrderVO;
import com.activity.model.ActivityOrderService;
import java.util.List;

@RestController
@RequestMapping("/api/activity-orders")
@CrossOrigin(origins = "*")
public class ActivityOrderController {

    @Autowired
    private ActivityOrderService orderService;

    @GetMapping
    public ResponseEntity<List<ActivityOrderVO>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @PostMapping
    public ResponseEntity<ActivityOrderVO> createOrder(@RequestBody ActivityOrderVO orderVO) {
        return ResponseEntity.ok(orderService.createOrder(orderVO));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            return ResponseEntity.ok(orderService.updatePaymentStatus(id, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Integer id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok("訂單已刪除");
    }
}