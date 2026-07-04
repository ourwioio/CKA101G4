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
	public ActivityOrderVO getOneOrder(Integer activityOrderId) {
		Optional<ActivityOrderVO> optional = orderRepo.findById(activityOrderId);
		return optional.orElse(null);
	}

	// 新增
	public ActivityOrderVO addOrder(ActivityOrderVO orderVO) {
		return orderRepo.save(orderVO);
	}

	// 修改
	public ActivityOrderVO updateOrder(ActivityOrderVO orderVO) {
		return orderRepo.save(orderVO);
	}

	// 新增/修改共用
	public ActivityOrderVO saveOrder(ActivityOrderVO orderVO) {
		return orderRepo.save(orderVO);
	}

	// 查詢會員自己的訂單
	public List<ActivityOrderVO> getOrdersByBuyerMemberId(Integer buyerMemberId) {
		return orderRepo.findByBuyerMemberId(buyerMemberId);
	}

	// 修改訂單狀態(API使用)
	public ActivityOrderVO updateOrderStatus(Integer activityOrderId, Byte orderStatus) {

		ActivityOrderVO orderVO = getOneOrder(activityOrderId);

		if (orderVO == null) {
			throw new RuntimeException("查無此訂單");
		}

		orderVO.setOrderStatus(orderStatus);

		return orderRepo.save(orderVO);
	}

	// 刪除
	public void deleteOrder(Integer activityOrderId) {
		orderRepo.deleteById(activityOrderId);
	}

	// 查詢某活動的所有報名訂單
	public List<ActivityOrderVO> getOrdersByActivityId(Integer activityId) {
		return orderRepo.findByActivityId(activityId);
	}

}