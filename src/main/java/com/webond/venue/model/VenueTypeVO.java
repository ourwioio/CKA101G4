package com.webond.venue.model;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "VENUE_TYPE")
public class VenueTypeVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "VENUE_TYPE_ID", updatable = false)
	private Integer venueTypeId;

	@OneToMany(mappedBy = "venueTypeVO")
	private Set<VenueVO> venues;

	@Column(name = "TYPE_NAME")
	@NotEmpty(message="場地名稱: 請勿空白")
	@Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z)]{2,10}$", message = "場地名稱: 只能是中、英文字母, 且長度必需在2到10之間")
	private String typeName;

	@Column(name = "TYPE_DESC")
//	@NotEmpty(message="場地描述: 請勿空白")
//	@Size(max = 30, message = "場地介紹：請勿超過 30 字")
//	@Pattern(regexp = "^[\u4e00-\u9fa5a-zA-Z0-9，。！？、\\s]{2,100}$", message = "場地描述: 長度必需在2到100之間")
	private String typeDesc;

	@Column(name = "TYPE_MODE")
	private Byte typeMode;

	public VenueTypeVO() {
		super();
	}

	public VenueTypeVO(Integer venueTypeId, Set<VenueVO> venues, String typeName, String typeDesc, Byte typeMode) {
		super();
		this.venueTypeId = venueTypeId;
		this.venues = venues;
		this.typeName = typeName;
		this.typeDesc = typeDesc;
		this.typeMode = typeMode;
	}

	public Integer getVenueTypeId() {
		return venueTypeId;
	}

	public void setVenueTypeId(Integer venueTypeId) {
		this.venueTypeId = venueTypeId;
	}

	public Set<VenueVO> getVenues() {
		return venues;
	}

	public void setVenues(Set<VenueVO> venues) {
		this.venues = venues;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeDesc() {
		return typeDesc;
	}

	public void setTypeDesc(String typeDesc) {
		this.typeDesc = typeDesc;
	}

	public Byte getTypeMode() {
		return typeMode;
	}

	public void setTypeMode(Byte typeMode) {
		this.typeMode = typeMode;
	}

	@Override
	public String toString() {
		return "VenueTypeVO [venueTypeId=" + venueTypeId + ", venues=" + venues + ", typeName=" + typeName
				+ ", typeDesc=" + typeDesc + ", typeMode=" + typeMode + "]";
	}


}
