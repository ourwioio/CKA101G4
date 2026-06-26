package com.webond.service.model;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "SERVICE_TYPE")
public class ServiceTypeVO {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="SERVICE_TYPE_ID")
	private Integer svcTypeID;
	@Column(name="TYPE_NAME")
	private String typeName;
	@Column(name="DESCRIPTION")
	private String descrip;
	@Column(name="TYPE_MODE")
	@JdbcTypeCode(SqlTypes.TINYINT)
	private Integer typeMode;
	@Column(name="DEFAULT_IMAGE_URL")
	private String imgURL;
	
	// ServiceTypeVO
	@OneToMany(mappedBy = "serviceType")
	private List<ServiceVO> serviceList;
	
	public ServiceTypeVO(){
		super();
	}
	public Integer getSvcTypeID() {
		return svcTypeID;
	}
	
	public void setSvcTypeID(Integer serTypeID) {
		this.svcTypeID = serTypeID;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getDescrip() {
		return descrip;
	}
	public void setDescrip(String descrip) {
		this.descrip = descrip;
	}
	public Integer getTypeMode() {
		return typeMode;
	}
	public void setTypeMode(Integer typeMode) {
		this.typeMode = typeMode;
	}
	public String getImgURL() {
		return imgURL;
	}
	public void setImgURL(String serImgURL) {
		this.imgURL = serImgURL;
	}
	
	
}
