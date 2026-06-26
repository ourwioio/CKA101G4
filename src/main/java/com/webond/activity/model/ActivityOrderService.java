package com.webond.activity.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.model.ActivityOrderRepository;
import com.webond.activity.model.ActivityOrderVO;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ActivityOrderService {

	@Autowired
	private ActivityOrderRepository orderRepo;

	public List<ActivityOrderVO> getAll() {
		return orderRepo.findAll();
	}

	public ActivityOrderVO createOrder(ActivityOrderVO orderVO) {
		return orderRepo.save(orderVO);
	}

	public ActivityOrderVO updatePaymentStatus(Integer orderId, Integer newStatus) {
		return orderRepo.findById(orderId).map(order -> {
			order.setPaymentStatus(newStatus);
			return orderRepo.save(order);
		}).orElseThrow(() -> new IllegalArgumentException("訂單不存在"));
	}

	public void deleteOrder(Integer id) {
		orderRepo.deleteById(id);
	}
}