package com.webond.venue.model;

import java.util.Set;

import com.webond.venue.model.VenueVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "VENUE_TYPE")
public class VenueTypeVO {

	@Id
	@Column(name = "VENUE_TYPE_ID")
	private Integer venueTypeId;

	@OneToMany(mappedBy = "venueTypeVO")
	private Set<VenueVO> venues;

	@Column(name = "TYPE_NAME")
	private String typeName;

	@Column(name = "TYPE_DESC")
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
