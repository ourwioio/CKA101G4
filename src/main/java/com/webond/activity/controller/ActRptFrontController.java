package com.webond.activity.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.webond.activity.model.ActRptService;
import com.webond.activity.model.ActRptVO;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;
import com.webond.member.model.MemberVO;
import com.webond.member.service.MemberService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class ActRptFrontController {
	
	 @Autowired
	 private ActivityService actSvc;
	 
	 @Autowired
	 private ActRptService actRptSvc;
	 
	 @Autowired
	 private MemberService memSvc;
	
// 檢舉按鈕	 
		 @GetMapping("/activity/actButton")
		 public String actButton(Model model) {
			 
			 List<ActivityVO> actList = actSvc.getAll();
			 
			 model.addAttribute("activities", actList);
			 
			 return "front-end/activityReport/reportButton";
			 
		 }
		 
// 顯示檢舉表單頁面（傳入活動 ID）
		    @GetMapping("/activity/report")
		    public String showReportForm(
		    		@RequestParam("activityId") Integer activityId, 
		    		Model model,
		    		HttpSession session) {
		    	
		    	MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		        if (loginMember == null) {
		            return "redirect:/member/login"; 
		        }
		    	
		        ActRptVO actRptVO = new ActRptVO();
		        ActivityVO activity = actSvc.getOneActivity(activityId);  
		        Integer userId = activity.getMemberId();
		        MemberVO mem = memSvc.getOneMember(userId);
		        
		        
		        actRptVO.setActId(activity);
		        actRptVO.setReporterId(loginMember);
		        
		        model.addAttribute("hostUserName", mem.getRealName());
		        model.addAttribute("actRptVO", actRptVO);
		        return "front-end/activityReport/reportForm"; 
		    }
		    
		    
		//  處理表單提交（含圖片上傳）
		    @PostMapping("/activity/rptSubmit")
		    public String submitReport(
		    		@Valid ActRptVO actRptVO,
		    		BindingResult result,
		    		@RequestParam("imageFile") MultipartFile file,
		    		HttpSession session,
		    		Model model) {
		    	

		        try {
		        	
		        	MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
		            if (loginMember == null) {
		                return "redirect:/member/login"; 
		            }
		            
		            if (file != null && !file.isEmpty()) {
		                String contentType = file.getContentType();
		                if (contentType == null || !contentType.startsWith("image/")) {
		                    result.rejectValue("actRptImg", "error.imageType", "佐證檔案必須是圖片格式。");
		                }
		            }

		            
		            if (result.hasErrors()) {
		            	
		            	ActivityVO activity = actSvc.getOneActivity(actRptVO.getActId().getActivityId());  
				        Integer userId = activity.getMemberId();
				        MemberVO mem = memSvc.getOneMember(userId);
				        
				        
				        actRptVO.setActId(activity);
				        actRptVO.setReporterId(loginMember);
				        
				        model.addAttribute("hostUserName", mem.getRealName());
				        model.addAttribute("actRptVO", actRptVO);
		                
		                return "front-end/activityReport/reportForm";
		            }
		            
		            actRptVO.setReporterId(loginMember); 

		            if (file != null && !file.isEmpty()) {
		                actRptVO.setActRptImg(file.getBytes());
		            }

		            actRptSvc.createReport(actRptVO);
		            return "redirect:/activity/success"; 
		            
		        } catch (IOException e) {
		            e.printStackTrace();
		            result.rejectValue("actRptImg", "error.upload", "圖片上傳失敗，請稍後再試。");
		            return "front-end/activityReport/reportForm";
		        }
		    }
		    
	// 成功畫面    
		    @GetMapping("/activity/success")
		    public String showSuccessPage() {
		        // 導向你存放成功頁面的 HTML 路徑
		        return "front-end/activityReport/reportSuccess";
		    }

}
