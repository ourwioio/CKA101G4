package com.webond.activity.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webond.activity.repository.ActivityOrderRepository;

@Service
@Transactional
public class ActivityOrderService {

	@Autowired
	private ActivityOrderRepository orderRepo;

	// 查詢全部
	public List<ActivityOrderVO> getAll() {
		return orderRepo.findAll();
	}

	// 查詢單筆
	public ActivityOrderVO getOneOrder(Integer id) {
		Optional<ActivityOrderVO> optional = orderRepo.findById(id);
		return optional.orElse(null);
	}

	// 新增
	public ActivityOrderVO createOrder(ActivityOrderVO orderVO) {
		return orderRepo.save(orderVO);
	}

	// 修改（新增這個方法）
	public ActivityOrderVO saveOrder(ActivityOrderVO orderVO) {
		return orderRepo.save(orderVO);
	}

	// 修改付款狀態(API使用)
	public ActivityOrderVO updatePaymentStatus(Integer actOrderId, Integer newStatus) {

		return orderRepo.findById(actOrderId).map(order -> {
			order.setPaymentStatus(newStatus);
			return orderRepo.save(order);
		}).orElseThrow(() -> new IllegalArgumentException("訂單不存在"));
	}

	// 刪除
	public void deleteOrder(Integer id) {
		orderRepo.deleteById(id);
	}
}