package com.webond.activity.model;

import java.util.List;

public interface ActivityDAO {
	public void insert(ActivityVO activityVo);

	public void update(ActivityVO activityVo);

	public void delete(Integer activityId);

	public ActivityVO findByPrimaryKey(Integer activityId);

	public List<ActivityVO> getAll();
}