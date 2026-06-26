package com.webond.service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SERVICE_ORDER")
public class ServiceOrderVO implements java.io.Serializable   {

	private static final long serialVersionUID = 1L;

   	@Id
    @Column(name = "SERVICE_ORDER_ID")
    private Integer serviceOrderId;
}
