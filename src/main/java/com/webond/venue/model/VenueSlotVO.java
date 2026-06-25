package com.webond.venue.model;

import java.time.LocalDate;

import com.webond.venue.model.VenueVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "VENUE_SLOT")
public class VenueSlotVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "VENUE_SLOT_ID", updatable = false)
	private Integer venueSlotId;
	
	@ManyToOne
	@JoinColumn(name="VENUE_ID", referencedColumnName="VENUE_ID")
	private VenueVO venueVO;
	
	@Column(name = "SLOT_DATE")
	private LocalDate slotDate;
	
	@Column(name = "SLOT_STATUS")
	private String slotStatus;

	public VenueSlotVO() {
		super();
	}

	public VenueSlotVO(Integer venueSlotId, VenueVO venueVO, LocalDate slotDate, String slotStatus) {
		super();
		this.venueSlotId = venueSlotId;
		this.venueVO = venueVO;
		this.slotDate = slotDate;
		this.slotStatus = slotStatus;
	}

	public Integer getVenueSlotId() {
		return venueSlotId;
	}

	public void setVenueSlotId(Integer venueSlotId) {
		this.venueSlotId = venueSlotId;
	}

	public VenueVO getVenueVO() {
		return venueVO;
	}

	public void setVenueVO(VenueVO venueVO) {
		this.venueVO = venueVO;
	}

	public LocalDate getSlotDate() {
		return slotDate;
	}

	public void setSlotDate(LocalDate slotDate) {
		this.slotDate = slotDate;
	}

	public String getSlotStatus() {
		return slotStatus;
	}

	public void setSlotStatus(String slotStatus) {
		this.slotStatus = slotStatus;
	}

	@Override
	public String toString() {
		return "VenueSlotVO [venueSlotId=" + venueSlotId + ", venueVO=" + venueVO + ", slotDate=" + slotDate
				+ ", slotStatus=" + slotStatus + "]";
	}

	
	
	
	
}
