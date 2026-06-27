package com.webond.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.service.model.ServiceTypeVO;

public interface ServiceTypeRepository extends JpaRepository<ServiceTypeVO, Integer> {

}