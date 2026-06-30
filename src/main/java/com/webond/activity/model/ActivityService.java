package com.webond.activity.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.activity.model.ActivityVO;
import com.webond.activity.repository.ActivityRepository;

import java.util.List;
import java.util.Optional;

@Service // 宣告這是一個 Service 元件
public class ActivityService {

	@Autowired // 讓 Spring 自動幫我們注入 Repository
	private ActivityRepository repository;

	// 查詢全部
	public List<ActivityVO> getAll() {
		return repository.findAll(); // findAll() 是 Spring 內建的方法
	}

	public ActivityVO getOneActivity(Integer id) {
		Optional<ActivityVO> optional = repository.findById(id);
		return optional.orElse(null); // 如果找不到就回傳 null
	}

	// 新增/修改
	public ActivityVO saveActivity(ActivityVO vo) {
		return repository.save(vo);
	}

	// 刪除
	public void deleteActivity(Integer id) {
		repository.deleteById(id);
	}
}