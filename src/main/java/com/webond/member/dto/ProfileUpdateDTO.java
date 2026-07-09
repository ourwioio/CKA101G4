package com.webond.member.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ProfileUpdateDTO {
	 
	
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
		
		public ProfileUpdateDTO() {}


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

		
		

		
	    
	    

}
