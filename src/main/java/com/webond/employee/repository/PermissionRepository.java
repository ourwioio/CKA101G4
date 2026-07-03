package com.webond.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.employee.model.PermissionVO;

public interface PermissionRepository extends JpaRepository<PermissionVO, Integer>{

}
