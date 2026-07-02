package com.webond.venue.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberVO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "VENUE_ORDER")
public class VenueOrderVO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "VENUE_ORDER_ID", updatable = false)
	private Integer venueOrderId;

	@ManyToOne
	@JoinColumn(name = "VENUE_ID", referencedColumnName = "VENUE_ID")
	private VenueVO venueVO;

	@ManyToOne
	@JoinColumn(name = "MEMBER_ID", referencedColumnName = "MEMBER_ID")
	private MemberVO member;

	@ManyToOne
	@JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID")
	private EmployeeVO empVO;

//	@Column(name = "EMPLOYEE_ID")
//	private Integer employeId;

	@Column(name = "VENUE_RATING")
	private Integer venueRating;

	@Column(name = "VENUE_COMMENT")
	private String venueComment;

	@Column(name = "PAYOUT_AMOUNT")
	private Byte payoutAmount;

	@Column(name = "REFUND_REASON")
	private String refundReason;

	@Column(name = "REFUND_STATUS")
	private Byte refundStatus;

	@Column(name = "HANDLED_AT")
	private LocalDateTime handledAt;

	@Column(name = "CREATED_AT")
	private LocalDateTime createdAt;

	@Column(name = "START_AT")
	private LocalTime startAt;

	@Column(name = "END_AT")
	private LocalTime endAt;

	@Column(name = "TOTAL_AMOUNT")
	private Integer totalAmount;

	@Column(name = "VENUE_PAYMENT_METHOD")
	private Byte paymentMethod;

	public VenueOrderVO() {
		super();
	}

	public VenueOrderVO(Integer venueOrderId, VenueVO venueVO, MemberVO member, EmployeeVO empVO, Integer venueRating,
			String venueComment, Byte payoutAmount, String refundReason, Byte refundStatus, LocalDateTime handledAt,
			LocalDateTime createdAt, LocalTime startAt, LocalTime endAt, Integer totalAmount, Byte paymentMethod) {
		super();
		this.venueOrderId = venueOrderId;
		this.venueVO = venueVO;
		this.member = member;
		this.empVO = empVO;
		this.venueRating = venueRating;
		this.venueComment = venueComment;
		this.payoutAmount = payoutAmount;
		this.refundReason = refundReason;
		this.refundStatus = refundStatus;
		this.handledAt = handledAt;
		this.createdAt = createdAt;
		this.startAt = startAt;
		this.endAt = endAt;
		this.totalAmount = totalAmount;
		this.paymentMethod = paymentMethod;
	}

	public Integer getVenueOrderId() {
		return venueOrderId;
	}

	public void setVenueOrderId(Integer venueOrderId) {
		this.venueOrderId = venueOrderId;
	}

	public VenueVO getVenueVO() {
		return venueVO;
	}

	public void setVenueVO(VenueVO venueVO) {
		this.venueVO = venueVO;
	}

	public MemberVO getMember() {
		return member;
	}

	public void setMember(MemberVO member) {
		this.member = member;
	}

	public EmployeeVO getEmpVO() {
		return empVO;
	}

	public void setEmpVO(EmployeeVO empVO) {
		this.empVO = empVO;
	}

	public Integer getVenueRating() {
		return venueRating;
	}

	public void setVenueRating(Integer venueRating) {
		this.venueRating = venueRating;
	}

	public String getVenueComment() {
		return venueComment;
	}

	public void setVenueComment(String venueComment) {
		this.venueComment = venueComment;
	}

	public Byte getPayoutAmount() {
		return payoutAmount;
	}

	public void setPayoutAmount(Byte payoutAmount) {
		this.payoutAmount = payoutAmount;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public Byte getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(Byte refundStatus) {
		this.refundStatus = refundStatus;
	}

	public LocalDateTime getHandledAt() {
		return handledAt;
	}

	public void setHandledAt(LocalDateTime handledAt) {
		this.handledAt = handledAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalTime getStartAt() {
		return startAt;
	}

	public void setStartAt(LocalTime startAt) {
		this.startAt = startAt;
	}

	public LocalTime getEndAt() {
		return endAt;
	}

	public void setEndAt(LocalTime endAt) {
		this.endAt = endAt;
	}

	public Integer getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Integer totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Byte getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(Byte paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	

	

}
