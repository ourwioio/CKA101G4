package com.webond.member.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import com.webond.employee.model.EmployeeVO;
import com.webond.notification.model.NotificationVO;
import com.webond.servicereport.model.ServiceReportVO;
import com.webond.venue.model.VenueOrderVO;
import com.webond.venue.model.VenueVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "MEMBER")
public class MemberVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "MEMBER_ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer memberId;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "member")
	@OrderBy("venueId asc")
	private Set<VenueVO> venues;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "member")
	@OrderBy("venueOrderId asc")
	private Set<VenueOrderVO> venueOrders;
	
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "member")
	@OrderBy("notificationId asc")
	private Set<NotificationVO> notifications;	

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "reporterMember")
	@OrderBy("serviceReportId asc")
	private Set<ServiceReportVO> servicereports;

	@Column(name = "PASSWORD_HASH")
	private String passwordHash;

	@Column(name = "NICKNAME")
	@NotEmpty(message = "請勿空白")
	private String nickname;

	@Lob
	@Column(name = "MEMBER_PIC")
	private byte[] memberPic;

	@Column(name = "GENDER")
	private Byte gender;

	@Column(name = "PHONE")
	@Pattern(regexp = "^09\\d{8}$", message = "手機號碼需為09開頭的10碼數字")
	private String phone;

	@Column(name = "EMAIL", unique = true)
	@NotEmpty(message = "登入帳號，請勿空白")
	@Email(message = "Email格式不正確")
	private String email;

	@Column(name = "MEMBER_INTRO")
	private String memberIntro;

	@Column(name = "ACCOUNT_STATUS")
	private Byte accountStatus;

	@Column(name = "CREATED_AT")
	private LocalDate createdAt;

	@Column(name = "SERVICE_RATE_SUM")
	private BigDecimal serviceRateSum;

	@Column(name = "SERVICE_RATE_COUNT")
	private Integer serviceRateCount;

	@Column(name = "SERVICER_RATE_SUM")
	private BigDecimal servicerRateSum;

	@Column(name = "SERVICER_RATE_COUNT")
	private Integer servicerRateCount;

	@Column(name = "ACT_RATE_SUM")
	private BigDecimal actRateSum;

	@Column(name = "ACT_RATE_COUNT")
	private Integer actRateCount;

	@Column(name = "HOLDACT_RATE_SUM")
	private BigDecimal holdactRateSum;

	@Column(name = "HOLDACT_RATE_COUNT")
	private Integer holdactRateCount;

	@Column(name = "REPORT_POINTS")
	private Integer reportPoints;

	@Column(name = "KYC_ID")
	private Integer kycId;

	@Column(name = "REAL_NAME")
	@NotEmpty(message = "請輸入姓名")
	private String realName;

	@ManyToOne
	@JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "EMPLOYEE_ID")
	private EmployeeVO employee;

	@Lob
	@Column(name = "ID_IMAGE")
	@NotNull(message = "請上傳身分證件")
	private byte[] idImage;

	@Lob
	@Column(name = "FACE_IMAGE")
	@NotNull(message = "請上傳臉部圖片")
	private byte[] faceImage;

	@Column(name = "ID_NUMBER")
	@NotEmpty(message = "請輸入身分證字號")
	private String idNumber;

	@Column(name = "KYC_STATUS")
	private Byte kycStatus;

	@Column(name = "SUBMITTED_AT")
	private LocalDateTime submittedAt;

	@Column(name = "REVIEWED_AT")
	private LocalDateTime reviewedAt;

	@Column(name = "BANK_CODE")
	@Pattern(regexp = "^\\d{3}$", message = "銀行代號必須為3位數字")
	private String bankCode;

	@Column(name = "BANK_ACCOUNT")
	@Pattern(regexp = "^\\d{10,14}$", message = "銀行帳號格式錯誤")
	private String bankAccount;

	public MemberVO() {
		super();
	}

	public Set<VenueVO> getVenues() {
		return venues;
	}

	public void setVenues(Set<VenueVO> venues) {
		this.venues = venues;
	}

	public Set<VenueOrderVO> getVenueOrders() {
		return venueOrders;
	}

	public void setVenueOrders(Set<VenueOrderVO> venueOrders) {
		this.venueOrders = venueOrders;
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public byte[] getMemberPic() {
		return memberPic;
	}

	public void setMemberPic(byte[] memberPic) {
		this.memberPic = memberPic;
	}

	public Byte getGender() {
		return gender;
	}

	public void setGender(Byte gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMemberIntro() {
		return memberIntro;
	}

	public void setMemberIntro(String memberIntro) {
		this.memberIntro = memberIntro;
	}

	public Byte getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(Byte accountStatus) {
		this.accountStatus = accountStatus;
	}

	public LocalDate getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDate createdAt) {
		this.createdAt = createdAt;
	}

	public BigDecimal getServiceRateSum() {
		return serviceRateSum;
	}

	public void setServiceRateSum(BigDecimal serviceRateSum) {
		this.serviceRateSum = serviceRateSum;
	}

	public Integer getServiceRateCount() {
		return serviceRateCount;
	}

	public void setServiceRateCount(Integer serviceRateCount) {
		this.serviceRateCount = serviceRateCount;
	}

	public BigDecimal getServicerRateSum() {
		return servicerRateSum;
	}

	public void setServicerRateSum(BigDecimal servicerRateSum) {
		this.servicerRateSum = servicerRateSum;
	}

	public Integer getServicerRateCount() {
		return servicerRateCount;
	}

	public void setServicerRateCount(Integer servicerRateCount) {
		this.servicerRateCount = servicerRateCount;
	}

	public BigDecimal getActRateSum() {
		return actRateSum;
	}

	public void setActRateSum(BigDecimal actRateSum) {
		this.actRateSum = actRateSum;
	}

	public Integer getActRateCount() {
		return actRateCount;
	}

	public void setActRateCount(Integer actRateCount) {
		this.actRateCount = actRateCount;
	}

	public BigDecimal getHoldactRateSum() {
		return holdactRateSum;
	}

	public void setHoldactRateSum(BigDecimal holdactRateSum) {
		this.holdactRateSum = holdactRateSum;
	}

	public Integer getHoldactRateCount() {
		return holdactRateCount;
	}

	public void setHoldactRateCount(Integer holdactRateCount) {
		this.holdactRateCount = holdactRateCount;
	}

	public Integer getReportPoints() {
		return reportPoints;
	}

	public void setReportPoints(Integer reportPoints) {
		this.reportPoints = reportPoints;
	}

	public Integer getKycId() {
		return kycId;
	}

	public void setKycId(Integer kycId) {
		this.kycId = kycId;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public EmployeeVO getEmployee() {
		return employee;
	}

	public void setEmployee(EmployeeVO employee) {
		this.employee = employee;
	}

	public byte[] getIdImage() {
		return idImage;
	}

	public void setIdImage(byte[] idImage) {
		this.idImage = idImage;
	}

	public byte[] getFaceImage() {
		return faceImage;
	}

	public void setFaceImage(byte[] faceImage) {
		this.faceImage = faceImage;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public Byte getKycStatus() {
		return kycStatus;
	}

	public void setKycStatus(Byte kycStatus) {
		this.kycStatus = kycStatus;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
	}

	public LocalDateTime getReviewedAt() {
		return reviewedAt;
	}

	public void setReviewedAt(LocalDateTime reviewedAt) {
		this.reviewedAt = reviewedAt;
	}

	public String getBankCode() {
		return bankCode;
	}

	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}

	public String getBankAccount() {
		return bankAccount;
	}

	public void setBankAccount(String bankAccount) {
		this.bankAccount = bankAccount;
	}

}
