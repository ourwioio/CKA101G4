package com.webond.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;
import com.webond.member.dto.ProfileUpdateDTO;
import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberService;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceService;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/front/memberPage")
public class MemberFrontControllerAyaka {
	@Autowired
	MemberService memberService;
	
	@Autowired
	ServiceService serviceService;
	
	@Autowired
	ActivityService activityService;
	
	@Autowired
	VenueService venueService;
	
	
	
	
	
	//會員頁面
	@GetMapping("oneMember")
	public String getOneMember(@RequestParam("memberId") Integer memberId,
	        @RequestParam(value = "venueId", required = false) Integer venueId,
	        ModelMap model) {
	    
	    MemberVO memberVO = memberService.getOneMember(memberId);
	    ServiceVO serviceList = serviceService.getOneService(memberId);
	    ActivityVO activityList = activityService.getOneActivity(memberId);
	    List<VenueVO> venueList = venueService.getVenuesByMember(memberId);
	    
	    // 為每個 venue 載入圖片
//	    if (venueList != null) {
//	        for (VenueVO v : venueList) {
//	            Set<VenueImagesVO> imgs = venueService.getImagesByVenue(v.getVenueId());
//	            v.setVenueImages(imgs);
//	        }
//	    }
	    
	    model.addAttribute("memberVO", memberVO);
	    model.addAttribute("serviceListData", serviceList);
	    model.addAttribute("activityListData", activityList);
	    model.addAttribute("venueListData", venueList);

//	    if (venueId != null) {
//	        Set<VenueImagesVO> venueImgList = venueService.getImagesByVenue(venueId);
//	        model.addAttribute("venueImagesData", venueImgList);
//	    }

	    return "front-end/member/profile";
	}
	

	
	
	//會員個人頁面
	@GetMapping("my")
	public String getMyMemberPage(@RequestParam("memberId") Integer memberId,
	        @RequestParam(value = "venueId", required = false) Integer venueId,
	        ModelMap model, HttpSession session) {	
		
		MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		if(loginMember == null) {
			return "redirect:/member/login";
		}
		
		MemberVO memberVO = memberService.getOneMember(memberId);
	    ServiceVO serviceList = serviceService.getOneService(memberId);
	    ActivityVO activityList = activityService.getOneActivity(memberId);
	    List<VenueVO> venueList = venueService.getVenuesByMember(memberId);
	    
	    model.addAttribute("memberVO", memberVO);
	    model.addAttribute("serviceListData", serviceList);
	    model.addAttribute("activityListData", activityList);
	    model.addAttribute("venueListData", venueList);
	    
		return "front-end/member/myProfile";
	}
		
	
	//會員編輯資料
	@GetMapping("edit")
	public String editProfile(@SessionAttribute("memberVO") MemberVO loginMember, ModelMap model) {
		//只能編輯自己的資料
		MemberVO memberVO = memberService.getOneMember(loginMember.getMemberId());

	    ProfileUpdateDTO dto = new ProfileUpdateDTO();
	    dto.setMemberId(memberVO.getMemberId());
	    dto.setNickname(memberVO.getNickname());
	    dto.setMemberIntro(memberVO.getMemberIntro());
	    dto.setGender(memberVO.getGender());
	    dto.setEmail(memberVO.getEmail());
	    dto.setPhone(memberVO.getPhone());
	    
		model.addAttribute("memberVO", dto);
		return "front-end/member/edit";
	}
	
	@PostMapping("update")
	public String updateProfile(@Valid ProfileUpdateDTO formData,BindingResult result, @SessionAttribute("memberVO") MemberVO loginMember,ModelMap model) {
		if(result.hasErrors()) {
			 result.getAllErrors().forEach(e -> System.out.println(e));
			model.addAttribute("memberVO", formData);
			return "front-end/member/edit";
		}
		
		
		
		if(!loginMember.getMemberId().equals(formData.getMemberId())) {
			model.addAttribute("error", "無修改權限");
			return "front-end/member/edit";			
		}
		
		MemberVO memberVO = memberService.getOneMember(formData.getMemberId());

	    // 只覆蓋表單有出現的欄位
	    memberVO.setNickname(formData.getNickname());
	    memberVO.setMemberIntro(formData.getMemberIntro());
	    memberVO.setGender(formData.getGender());
	    memberVO.setEmail(formData.getEmail());
	    memberVO.setPhone(formData.getPhone());

		
		
		memberService.updateMember(memberVO);
		model.addAttribute("memberVO", memberVO);
		
		return "front-end/member/edit";		
	}
	
	//密碼更新
	@GetMapping("changePassword")
	public String changePasswordPage(@SessionAttribute("memberVO") MemberVO loginMember, ModelMap model) {
		 
		ProfileUpdateDTO dto = new ProfileUpdateDTO();
		dto.setMemberId(loginMember.getMemberId());
		model.addAttribute("memberVO", dto);
	    return "front-end/member/changePassword";
	}
	
	@PostMapping("changePassword")
	public String changePassword(@Valid ProfileUpdateDTO formData, BindingResult result, @SessionAttribute("memberVO") MemberVO loginMember, ModelMap model) {
		 if (result.hasErrors()) {
		        model.addAttribute("memberVO", formData);
		        return "front-end/member/changePassword";
		    }
		 
		 if(!loginMember.getMemberId().equals(formData.getMemberId())) {
			model.addAttribute("error", "無修改權限");
	        return "front-end/member/changePassword";			
		 }
		 
		 if(!formData.getNewPassword().equals(formData.getConfirmPassword())) {
			 model.addAttribute("error", "兩次輸入新密碼不一致");
			 return "fornt-end/member/changePassword";
		 }
		 
		 try {
			 memberService.changePassword(formData.getMemberId(), formData.getOldPassword(), formData.getNewPassword());
		 }catch (IllegalArgumentException e){
		        model.addAttribute("error", e.getMessage());
		        model.addAttribute("changePasswordDTO", formData);
		        return "front-end/member/changePassword";			 
		 }
		 return "redirect:/member/logout";
	}
	
	

}
