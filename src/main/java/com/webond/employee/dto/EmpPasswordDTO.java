package com.webond.employee.dto;



import com.webond.employee.dto.EmpPasswordDTO.ValidGroup;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@GroupSequence({ValidGroup.First.class, ValidGroup.Second.class, EmpPasswordDTO.class})
public class EmpPasswordDTO {
	
	@NotBlank(message = "目前的初始密碼不能為空", groups = ValidGroup.First.class)
	private String currentPassword;
	
	@NotBlank(message = "新密碼不能為空", groups = ValidGroup.First.class)
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$", 
    message = "新密碼長度為 8 到 20 的大小寫英文與數字", groups = ValidGroup.Second.class)
	private String newPassword;
	
	@NotBlank(message = "確認新密碼不能為空", groups = ValidGroup.First.class)
    private String confirmPassword;

	
	public EmpPasswordDTO() {
	}

	
	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
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
	
	
	
	public interface ValidGroup {
	    interface First {}  
	    interface Second {}
	}
	
	

}
