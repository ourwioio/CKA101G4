package com.webond.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ChangePasswordDTO {
	
	@NotNull
    private Integer memberId;


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
    
    public ChangePasswordDTO() {}
    
    public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
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
