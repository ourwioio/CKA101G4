package com.webond.venue.model;

import java.time.LocalDate;
import java.util.Set;

import com.webond.member.model.MemberVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "VENUE")
public class VenueVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "VENUE_ID", updatable = false)
	private Integer venueId;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "venueVO")
	@OrderBy("imagesId asc")
	private Set<VenueImagesVO> venueImages;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "venueVO")
	@OrderBy("venueSlotId asc")
	private Set<VenueSlotVO> venueSlots;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "venueVO")
	@OrderBy("venueOrderId asc")
	private Set<VenueOrderVO> venueOrders;

	@ManyToOne
	@JoinColumn(name = "MEMBER_ID", referencedColumnName = "MEMBER_ID")
	private MemberVO member;

	@ManyToOne
	@JoinColumn(name = "VENUE_TYPE_ID")
	private VenueTypeVO venueTypeVO;

	@Column(name = "VENUE_NAME")
	private String venueName;

	@Column(name = "ADDRESS")
	private String address;

	@Column(name = "CAPACITY")
	private Integer capacity;

	@Column(name = "HOURLY_RATE")
	private Integer hourlyRate;

	@Column(name = "VENUE_STATUS")
	private Byte venueStatus;

	@Column(name = "CREATED_AT")
	private LocalDate createdAt;

	@Column(name = "DEFAULT_OPEN_DAYS")
	private String openDays;

	@Column(name = "DEFAULT_AVAILABLE_HOURS")
	private String availableHours;

	@Column(name = "TOTAL_RATING_STARS")
	private Integer ratingStars;

	@Column(name = "RATING_COUNT")
	private Integer ratingcount;

	public VenueVO() {
		super();
	}

	public VenueVO(Integer venueId, Set<VenueImagesVO> venueImages, Set<VenueSlotVO> venueSlots,
			Set<VenueOrderVO> venueOrders, MemberVO member, VenueTypeVO venueTypeVO, String venueName, String address,
			Integer capacity, Integer hourlyRate, Byte venueStatus, LocalDate createdAt, String openDays,
			String availableHours, Integer ratingStars, Integer ratingcount) {
		super();
		this.venueId = venueId;
		this.venueImages = venueImages;
		this.venueSlots = venueSlots;
		this.venueOrders = venueOrders;
		this.member = member;
		this.venueTypeVO = venueTypeVO;
		this.venueName = venueName;
		this.address = address;
		this.capacity = capacity;
		this.hourlyRate = hourlyRate;
		this.venueStatus = venueStatus;
		this.createdAt = createdAt;
		this.openDays = openDays;
		this.availableHours = availableHours;
		this.ratingStars = ratingStars;
		this.ratingcount = ratingcount;
	}

	public Integer getVenueId() {
		return venueId;
	}

	public void setVenueId(Integer venueId) {
		this.venueId = venueId;
	}

	public Set<VenueImagesVO> getVenueImages() {
		return venueImages;
	}

	public void setVenueImages(Set<VenueImagesVO> venueImages) {
		this.venueImages = venueImages;
	}

	public Set<VenueSlotVO> getVenueSlots() {
		return venueSlots;
	}

	public void setVenueSlots(Set<VenueSlotVO> venueSlots) {
		this.venueSlots = venueSlots;
	}

	public Set<VenueOrderVO> getVenueOrders() {
		return venueOrders;
	}

	public void setVenueOrders(Set<VenueOrderVO> venueOrders) {
		this.venueOrders = venueOrders;
	}

	public MemberVO getMember() {
		return member;
	}

	public void setMember(MemberVO member) {
		this.member = member;
	}

	public VenueTypeVO getVenueTypeVO() {
		return venueTypeVO;
	}

	public void setVenueTypeVO(VenueTypeVO venueTypeVO) {
		this.venueTypeVO = venueTypeVO;
	}

	public String getVenueName() {
		return venueName;
	}

	public void setVenueName(String venueName) {
		this.venueName = venueName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public Integer getHourlyRate() {
		return hourlyRate;
	}

	public void setHourlyRate(Integer hourlyRate) {
		this.hourlyRate = hourlyRate;
	}

	public Byte getVenueStatus() {
		return venueStatus;
	}

	public void setVenueStatus(Byte venueStatus) {
		this.venueStatus = venueStatus;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public String getOpenDays() {
		return openDays;
	}

	public void setOpenDays(String openDays) {
		this.openDays = openDays;
	}

	public String getAvailableHours() {
		return availableHours;
	}

	public void setAvailableHours(String availableHours) {
		this.availableHours = availableHours;
	}

	public Integer getRatingStars() {
		return ratingStars;
	}

	public void setRatingStars(Integer ratingStars) {
		this.ratingStars = ratingStars;
	}

	public Integer getRatingcount() {
		return ratingcount;
	}

	public void setRatingcount(Integer ratingcount) {
		this.ratingcount = ratingcount;
	}

	@Override
	public String toString() {
		return "VenueVO [venueId=" + venueId + ", venueImages=" + venueImages + ", venueSlots=" + venueSlots
				+ ", venueOrders=" + venueOrders + ", member=" + member + ", venueTypeVO=" + venueTypeVO
				+ ", venueName=" + venueName + ", address=" + address + ", capacity=" + capacity + ", hourlyRate="
				+ hourlyRate + ", venueStatus=" + venueStatus + ", createdAt=" + createdAt + ", openDays=" + openDays
				+ ", availableHours=" + availableHours + ", ratingStars=" + ratingStars + ", ratingcount=" + ratingcount
				+ "]";
	}

	

}
