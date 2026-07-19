package com.webond.member.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityOrderVO;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;
import com.webond.member.dto.ChangePasswordDTO;
import com.webond.member.dto.ProfileUpdateDTO;
import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberService;
import com.webond.member.service.MyReviewService;
import com.webond.service.dto.MemberReviewDTO;
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
	ActivityOrderService activityOrderService;

	@Autowired
	VenueService venueService;
	
	@Autowired
	MyReviewService myReviewService;
	
	@Autowired
    private BCryptPasswordEncoder passwordEncoder;
	
	
	private static final Map<String, Integer> STATUS_PRIORITY = Map.of(
	        "即將截止", 0,
	        "報名中", 1,
	        "尚未開放報名", 2,
	        "報名已截止", 3,
	        "活動已結束", 4,
	        "活動不存在", 5
	);


	//會員頁面
	@GetMapping("oneMember")
	public String getOneMember(@RequestParam("memberId") Integer memberId,
	        @RequestParam(value = "venueId", required = false) Integer venueId,
	        ModelMap model) {

	    MemberVO memberVO = memberService.getOneMember(memberId);
	    ServiceVO serviceList = serviceService.getOneService(memberId);
	    List<VenueVO> venueList = venueService.getActiveByMember(memberId);
	    List<MemberReviewDTO> reviews = myReviewService.getReviewsByMemberId(memberId);
	    addHostedActivityData(model, memberId);
	    model.addAttribute("reviews", reviews);

	    List<ActivityVO> activityList = activityService.getAll();
	    Map<Integer, String> registrationStatusMap = memberService.getRegistrationStatusMap(activityList);

	    // 依優先順序排序活動清單
	    List<ActivityVO> sortedActivityList = activityList.stream()
	            .sorted(Comparator.comparingInt(activityVO ->
	                    STATUS_PRIORITY.getOrDefault(
	                            registrationStatusMap.get(activityVO.getActivityId()),
	                            99
	                    )
	            ))
	            .collect(Collectors.toList());
	    List<ServiceVO> serviceListData = serviceService.getAll().stream()
	            .filter(s -> s.getMemberId().equals(memberId))
	            .filter(s -> s.getStatus() == 1)
	            .collect(Collectors.toList());



	    model.addAttribute("registrationStatusMap", registrationStatusMap);
	    model.addAttribute("activityListData", sortedActivityList); // 給 Thymeleaf 用排序後的清單

	    model.addAttribute("serviceListData", serviceListData);
	    model.addAttribute("memberVO", memberVO);
	    model.addAttribute("venueListData", venueList);

	    return "front-end/member/profile";
	}
	

	//會員個人頁面
	@GetMapping("my")
	public String getMyMemberPage(ModelMap model, HttpSession session) {

	    MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
	    if (loginMember == null) {
	        return "redirect:/member/login";
	    }

	    Integer memberId = loginMember.getMemberId();

	    MemberVO memberVO = memberService.getOneMember(memberId);
	    List<VenueVO> venueList = venueService.getActiveByMember(memberId);
	    List<MemberReviewDTO> reviews = myReviewService.getReviewsByMemberId(memberId);
	    addHostedActivityData(model, memberId);
	    model.addAttribute("reviews", reviews);

	    // 只保留這個會員、且上架中的服務
	    List<ServiceVO> serviceListData = serviceService.getAll().stream()
	            .filter(s -> s.getMemberId().equals(memberId))
	            .filter(s -> s.getStatus() == 1)
	            .collect(Collectors.toList());

	    List<ActivityVO> activityList = activityService.getAll();
	    Map<Integer, String> registrationStatusMap = memberService.getRegistrationStatusMap(activityList);

	    // 依優先順序排序活動清單
	    List<ActivityVO> activityListData = activityList.stream()
	            .sorted(Comparator.comparingInt(activityVO ->
	                    STATUS_PRIORITY.getOrDefault(
	                            registrationStatusMap.get(activityVO.getActivityId()),
	                            99
	                    )
	            ))
	            .collect(Collectors.toList());

	    model.addAttribute("registrationStatusMap", registrationStatusMap);
	    model.addAttribute("activityListData", activityListData);

	    model.addAttribute("memberVO", memberVO);
	    model.addAttribute("serviceListData", serviceListData);   // 改成用正確過濾好的清單
	    model.addAttribute("venueListData", venueList);

	    return "front-end/member/myProfile";
	}
	
	

	private void addHostedActivityData(ModelMap model, Integer memberId) {
		LocalDateTime now = LocalDateTime.now();
		List<ActivityVO> hostedActivities = activityService.getActivitiesByMemberId(memberId);

		Map<Integer, List<ActivityOrderVO>> activityReviewMap = new HashMap<>();
		Map<Integer, String> activityDisplayStatusMap = new HashMap<>();
		for (ActivityVO activity : hostedActivities) {
			activityReviewMap.put(activity.getActivityId(),
					activityOrderService.getReviewedOrdersByActivityId(activity.getActivityId()));
			if (activity.getActivityStatus() != null && activity.getActivityStatus() == 2) {
				activityDisplayStatusMap.put(activity.getActivityId(), "已取消");
			} else if (activity.getEndTime() != null && !activity.getEndTime().isAfter(now)) {
				activityDisplayStatusMap.put(activity.getActivityId(), "已完成");
			} else if (activity.getActivityStatus() != null && activity.getActivityStatus() == 1) {
				activityDisplayStatusMap.put(activity.getActivityId(), "延期");
			} else {
				activityDisplayStatusMap.put(activity.getActivityId(), "進行中");
			}
		}

		model.addAttribute("activityListData", hostedActivities);
		model.addAttribute("activityReviewMap", activityReviewMap);
		model.addAttribute("activityDisplayStatusMap", activityDisplayStatusMap);
	}
		
	
	//會員編輯資料
	@GetMapping("edit")
	public String editProfile(@SessionAttribute("memberVO") MemberVO memberVO, ModelMap model, HttpSession session) {
		//只能編輯自己的資料
		if(memberVO == null) {
			return "redirect:/member/login";
		}

	    ProfileUpdateDTO dto = new ProfileUpdateDTO();
	    dto.setMemberId(memberVO.getMemberId());
	    dto.setNickname(memberVO.getNickname());
	    dto.setMemberIntro(memberVO.getMemberIntro());
	    dto.setGender(memberVO.getGender());
	    dto.setEmail(memberVO.getEmail());
	    dto.setPhone(memberVO.getPhone());   
	    
		model.addAttribute("profileUpdateDTO", dto);
	    model.addAttribute("hasProfilePic", memberVO.getMemberPic() != null);
		return "front-end/member/edit";
	}
	
	
	@PostMapping("update")
	public String updateProfile(@Valid ProfileUpdateDTO formData,BindingResult result, 
								@SessionAttribute("memberVO") MemberVO loginMember,
								@RequestParam(value = "picFile", required = false) MultipartFile picFile,
								ModelMap model, HttpSession session) throws IOException {
		if(result.hasErrors()) {
//			result.getAllErrors().forEach(e -> System.out.println(e));
			model.addAttribute("profileUpdateDTO", formData);
		    model.addAttribute("hasProfilePic", loginMember.getMemberPic() != null);
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

        if (picFile != null && !picFile.isEmpty()) {
            memberVO.setMemberPic(picFile.getBytes());
        } 		
		
		memberService.updateMember(memberVO);
	    session.setAttribute("memberVO", memberVO);
	    model.addAttribute("hasProfilePic", memberVO.getMemberPic() != null);
		
		return "front-end/member/edit";		
	}
	
	
	//更新個人圖片
	@GetMapping("pic/{memberId}")
	@ResponseBody
	public ResponseEntity<byte[]> getMemberPic(@PathVariable Integer memberId) throws IOException {
	    MemberVO member = memberService.getOneMember(memberId);	    

	    if (member == null || member.getMemberPic() == null) {
	        return ResponseEntity.notFound().build();
	    }

	    byte[] picData = member.getMemberPic();
	    String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(picData));
	    MediaType mediaType = (contentType != null) ? MediaType.parseMediaType(contentType) : MediaType.IMAGE_JPEG;

	    return ResponseEntity.ok()
	            .contentType(mediaType)
	            .cacheControl(CacheControl.noCache()) 
	            .body(member.getMemberPic());
	}
	

	
	
	//刪除個人圖片
	@PostMapping("deletePic")
	@ResponseBody
	public ResponseEntity<?> getDeleteMemberPic(@SessionAttribute("memberVO") MemberVO loginMember, HttpSession session)  {
		
		if (loginMember == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("請先登入");
	    }
		
	    MemberVO memberVO = memberService.getOneMember(loginMember.getMemberId());

	    if (memberVO == null || memberVO.getMemberPic() == null) {
	        return ResponseEntity.notFound().build();
	    }

	    memberVO.setMemberPic(null);
	    memberService.updateMember(memberVO);

	    // 同步更新 session，避免其他頁面還撈到舊圖
	    session.setAttribute("memberVO", memberVO);

	    return ResponseEntity.ok().build();
	}
	
	
	//刪除個人帳號
	@PostMapping("delete/{memberId}")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> deleteAccount(
	        @PathVariable Integer memberId,
	        @RequestBody Map<String, String> body,
	        HttpSession session) {

	    Map<String, Object> result = new HashMap<>();
	    String rawPassword = body.get("password");

	    MemberVO member = memberService.getOneMember(memberId);
	    if (member == null) {
	        result.put("success", false);
	        result.put("message", "查無此會員");
	        return ResponseEntity.badRequest().body(result);
	    }

	
	    if (!passwordEncoder.matches(rawPassword, member.getPasswordHash())) {
	        result.put("success", false);
	        result.put("message", "密碼錯誤");
	        return ResponseEntity.badRequest().body(result);
	    }

	    memberService.updateAccountStatus(memberId, (byte)2); // 3 = 停權
	    session.invalidate(); // 登出

	    result.put("success", true);
	    return ResponseEntity.ok(result);
	}
	
	
	//密碼更新
	@GetMapping("changePassword")
	public String changePasswordPage(@SessionAttribute("memberVO") MemberVO loginMember, ModelMap model) {			 
		ChangePasswordDTO dto = new ChangePasswordDTO();
		dto.setMemberId(loginMember.getMemberId());
		model.addAttribute("memberVO", dto);
	    return "front-end/member/changePassword";
	}
	
	@PostMapping("changePassword")
	public String changePassword(@Valid @ModelAttribute("memberVO") ChangePasswordDTO formData, BindingResult result, @SessionAttribute("memberVO") MemberVO loginMember, ModelMap model) {
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
			 return "front-end/member/changePassword";
		 }
		 
		 try {
			 memberService.changePassword(formData.getMemberId(), formData.getOldPassword(), formData.getNewPassword());
			 	System.out.print("0");
		 }catch (IllegalArgumentException e){
			 	System.out.print("1");
		        model.addAttribute("error", e.getMessage());
		        model.addAttribute("memberVO", formData);
		        return "front-end/member/changePassword";			 
		 }
		 return "redirect:/member/logout";
	}
	
	

}
