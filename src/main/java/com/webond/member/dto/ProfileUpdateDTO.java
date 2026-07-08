package com.webond.member.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ProfileUpdateDTO {
	 
		@Lob
		@Column(name = "MEMBER_PIC")
		private byte[] memberPic;
	
		@NotNull
	    private Integer memberId;

	    @NotEmpty(message = "暱稱請勿空白")
	    private String nickname;

	    private String memberIntro;

	    private Byte gender;

		@NotEmpty(message = "登入帳號（Email）請勿空白")
		@Email(message = "Email格式不正確")
		private String email;

		@Pattern(regexp = "^09\\d{8}$", message = "手機號碼需為09開頭的10碼數字")
		private String phone;
		

	    @NotBlank(message = "請輸入原密碼")
	    private String oldPassword;

	    @NotBlank(message = "請輸入新密碼")
	    @Pattern(
	        regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
	        message = "新密碼至少需8碼，並包含英文字母與數字"
	    )
	    private String newPassword;

	    @NotBlank(message = "請再次輸入新密碼")
	    private String confirmPassword;
		
		public ProfileUpdateDTO() {}

		public byte[] getMemberPic() {
			return memberPic;
		}

		public void setMemberPic(byte[] memberPic) {
			this.memberPic = memberPic;
		}

		public Integer getMemberId() {
			return memberId;
		}

		public void setMemberId(Integer memberId) {
			this.memberId = memberId;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public String getMemberIntro() {
			return memberIntro;
		}

		public void setMemberIntro(String memberIntro) {
			this.memberIntro = memberIntro;
		}

		public Byte getGender() {
			return gender;
		}

		public void setGender(Byte gender) {
			this.gender = gender;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getOldPassword() {
			return oldPassword;
		}

		public void setOldPassword(String oldPassword) {
			this.oldPassword = oldPassword;
		}

		public String getNewPassword() {
			return newPassword;
		}

		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}

		public String getConfirmPassword() {
			return confirmPassword;
		}

		public void setConfirmPassword(String confirmPassword) {
			this.confirmPassword = confirmPassword;
		}

		
		

		
	    
	    

}
