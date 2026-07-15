package com.webond.service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webond.service.model.ServiceTypeVO;

public interface ServiceTypeRepository
        extends JpaRepository<ServiceTypeVO, Integer> {

    List<ServiceTypeVO>
            findByTypeNameContainingIgnoreCaseOrDescripContainingIgnoreCase(
                    String typeName,
                    String description
            );
}