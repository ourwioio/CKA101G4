package com.webond.venue.model;

import java.time.LocalDateTime;
import java.util.HashSet;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "VENUE")
public class VenueVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "VENUE_ID", updatable = false)
	private Integer venueId;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "venueVO", orphanRemoval = true)
	@OrderBy("imagesId asc")
	private Set<VenueImagesVO> venueImages = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "venueVO")
	@OrderBy("slotDate asc")
	private Set<VenueSlotVO> venueSlots = new HashSet<>();

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
	@NotEmpty(message = "場地名稱：請勿空白")
	private String venueName;

	@Column(name = "ADDRESS")
	@NotEmpty(message = "場地地址：請勿空白")
	private String address;

	@Column(name = "CAPACITY")
	@NotNull(message = "容納人數：請勿空白")
	@Min(value = 1, message = "容納人數：必需大於 0")
	private Integer capacity;

	@Column(name = "HOURLY_RATE")
	@NotNull(message = "每小時費用：請勿空白")
	@Min(value = 1, message = "每小時費用：必需大於 0")
	private Integer hourlyRate;

	@Column(name = "VENUE_STATUS")
	private Byte venueStatus;

	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;

	@Column(name = "DEFAULT_OPEN_DAYS")
	private String openDays;

	@Column(name = "DEFAULT_AVAILABLE_HOURS")
	private String availableHours;

	@Column(name = "TOTAL_RATING_STARS")
	private Integer ratingStars;

	@Column(name = "RATING_COUNT")
	private Integer ratingCount;

	@Column(name = "VENUE_DESCRIPTION")
	@Size(max = 200, message = "場地介紹：請勿超過 200 字")
	private String venueDescription;

	public VenueVO() {
		super();
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

	public VenueImagesVO getCoverImage() {
		for (VenueImagesVO image : venueImages) {
			if (image.getCover() != null && image.getCover() == 1) {
				return image; // 回傳封面
			}
		}
		return null; // 防呆
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Integer getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(Integer ratingCount) {
		this.ratingCount = ratingCount;
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
		return ratingCount;
	}

	public void setRatingcount(Integer ratingcount) {
		this.ratingCount = ratingcount;
	}

	public String getVenueDescription() {
		return venueDescription;
	}

	public void setVenueDescription(String venueDescription) {
		this.venueDescription = venueDescription;
	}

	public Double getAverageRating() {
		if (ratingCount == null || ratingCount == 0) {
			return null; // 尚未評分
		}
		return ratingStars.doubleValue() / ratingCount;
	}

}
