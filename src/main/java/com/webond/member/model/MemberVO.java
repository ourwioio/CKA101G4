package com.webond.member.model;

import java.util.Set;

import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "MEMBER")
public class MemberVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "MEMBER_ID", updatable = false)
	private Integer memberId;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "member")
	@OrderBy("venueId asc")
	private Set<VenueVO> venues;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "member")
	@OrderBy("venueOrderId asc")
	private Set<VenueOrderVO> venueOrders;

	public MemberVO() {
		super();
	}

//	public MemberVO(Integer memberId, Set<VenueVO> venues, Set<VenueOrderVO> venueOrders) {
//		super();
//		this.memberId = memberId;
//		this.venues = venues;
//		this.venueOrders = venueOrders;
//	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public Set<VenueVO> getVenues() {
		return venues;
	}

	public void setVenues(Set<VenueVO> venues) {
		this.venues = venues;
	}

//	public Set<VenueOrderVO> getVenueOrders() {
//		return venueOrders;
//	}
//
//	public void setVenueOrders(Set<VenueOrderVO> venueOrders) {
//		this.venueOrders = venueOrders;
//	}


	
	

}
