package com.webond.activity.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.webond.activity.model.ActRptService;
import com.webond.activity.model.ActRptVO;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;
import com.webond.employee.model.EmployeeVO;
import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.service.MemberService;
import com.webond.member.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ActRptController {

	 @Autowired
	 private ActRptService actRptSvc;
	 
	 
	 @Autowired
	 private ActivityService actSvc;
	 
	 @Autowired
	 private NotificationService notificationSvc;
	 
	 @Autowired
	 private MemberService memSvc;

// ============== 前台 ================= //
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
	        
	        ActivityVO activity = new ActivityVO();
	        activity.setActivityId(activityId); 
	        actRptVO.setActId(activity);
	        
	        actRptVO.setReporterId(loginMember);
	        
	        model.addAttribute("actRptVO", actRptVO);
	        return "front-end/activityReport/reportForm"; 
	    }

	    
//  處理表單提交（含圖片上傳）
	    @PostMapping("/activity/rptSubmit")
	    public String submitReport(
	    		@ModelAttribute("actRptVO") ActRptVO actRptVO,
	    		@RequestParam("imageFile") MultipartFile file,
	    		HttpSession session,
	    		Model model) {
	        try {
	        	
	        	MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
	            if (loginMember == null) {
	                return "redirect:/member/login"; 
	            }
	        	
	            actRptVO.setReporterId(loginMember);

	            if (file != null && !file.isEmpty()) {
	                actRptVO.setActRptImg(file.getBytes());
	            }

	            actRptSvc.createReport(actRptVO);
	            
	            return "redirect:/activity/success"; 
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            model.addAttribute("errorMessage", "圖片上傳失敗，請稍後再試。");
	            return "activity/report_form";
	        }
	    }
	    
// 成功畫面    
	    @GetMapping("/activity/success")
	    public String showSuccessPage() {
	        // 導向你存放成功頁面的 HTML 路徑
	        return "front-end/activityReport/reportSuccess";
	    }
	    
	    
//===================後台===============// 
	    
	    @GetMapping({ "/admin/actRptList", "/admin/activity/actRptList" })
	    public String actRptList(
	            @RequestParam(value = "status", required = false) Integer status, // 預設看 0:待處理
	            @RequestParam(value = "page", defaultValue = "0") int page,
	    		Model model, 
	    		HttpSession session) {
	        
	    	EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
	        if (loginEmp == null) {
	            return "redirect:/admin/login"; 
	        }
	        
	        int pageSize = 10; 
	        Page<ActRptVO> reportPage;

	        if (status == null) {
	        	reportPage = actRptSvc.getAllRpts(page, pageSize);
	        } else {
	            reportPage = actRptSvc.getRptsByStatusWithPage(status, page, pageSize);
	        }
	        
	        model.addAttribute("actRptListData", reportPage.getContent());
	        model.addAttribute("currentStatus", status);
	        model.addAttribute("currentPage", page);
	        model.addAttribute("totalPages", reportPage.getTotalPages());
	    	
	        return "back-end/activity/listAllActRpt";
	    }
	    
	    
	    @GetMapping({ "/admin/actRpt/img/{id}", "/admin/activity/actRpt/img/{id}" })
	    @ResponseBody
	    public ResponseEntity<byte[]> getReportImg(@PathVariable("id") Integer id) {
	        ActRptVO vo = actRptSvc.getOneActRpt(id); 
	        
	        if (vo != null && vo.getActRptImg() != null) {
	            byte[] imgBytes = vo.getActRptImg();
	            return ResponseEntity.ok()
	                    .contentType(MediaType.IMAGE_JPEG)
	                    .body(imgBytes);
	        }
	        return null;
	    }
	    
	    @GetMapping({ "/admin/actRpt/appealImg/{id}", "/admin/activity/actRpt/appealImg/{id}" })
	    @ResponseBody
	    public ResponseEntity<byte[]> getAppealImg(@PathVariable("id") Integer id) {
	    	ActRptVO vo = actRptSvc.getOneActRpt(id); 
	        
	        if (vo != null && vo.getAppealImg() != null) {
	            byte[] imgBytes = vo.getAppealImg();
	            return ResponseEntity.ok()
	                    .contentType(MediaType.IMAGE_JPEG)
	                    .body(imgBytes);
	        }
	        return null;
	    }
	    
	    
	    
	    @PostMapping({ "/admin/submit", "/admin/activity/submit" })
	    public String submitAudit(
	    		@ModelAttribute("actRptVO") ActRptVO actRptVO, 
	    		HttpSession session) {
	       
	    	EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
	        if (loginEmp == null) { return "redirect:/employee/login"; }
	        
	        //檢舉單
	        ActRptVO originalRpt = actRptSvc.getOneActRpt(actRptVO.getActRptId());
	        if (originalRpt == null) {
	            return "redirect:/admin/activity/actRptList";
	        }
	        
	        //被檢舉活動的主辦人
	        MemberVO memVO = memSvc.getOneMember(originalRpt.getActId().getMemberId());
	        //被檢舉活動
	        ActivityVO actVO = actSvc.getOneActivity(originalRpt.getActId().getActivityId());

	        
	        //申訴網址
	        Integer rptId = originalRpt.getActRptId();
	        String appealPath = "/member/activity/appeal?actRptId=" + rptId;
	        
	        // 檢舉成功(通知被檢舉人)
	        if(Integer.valueOf(1).equals(actRptVO.getActRptStatus())){
	        	NotificationVO notificationVO = new NotificationVO();
	        	notificationVO.setMember(memVO);
	        	notificationVO.setTitle("您的「" + actVO.getActivityTitle() + "」已被檢舉");
	        	notificationVO.setContent("活動已違反：" + originalRpt.getRptType() + "<br>" +
	        							  "懲處：會員記點 " + originalRpt.getPenaltyValue() + "<br>" +
	        							  "申訴管道(請在三天內完成)：<a href='" + appealPath + "' class='alert-link'>點此填寫申訴單</a>");
	        	notificationVO.setNotificationType((byte) 2);
	        	
	        	notificationSvc.addNotification(notificationVO);
	        	
	        // 檢舉駁回(通知檢舉人)
	        }else if(Integer.valueOf(2).equals(actRptVO.getActRptStatus())) {
	        	NotificationVO notificationVO = new NotificationVO();
	        	notificationVO.setMember(actRptVO.getReporterId());
	        	notificationVO.setTitle("您的「" + actVO.getActivityTitle() + "」檢舉已被駁回");
	        	notificationVO.setContent("查無違規");
	        	notificationVO.setNotificationType((byte) 2);
	        	
	        	notificationSvc.addNotification(notificationVO);
	        
	        // 申訴成功(通知被檢舉人)	
	        }else if(Integer.valueOf(4).equals(actRptVO.getActRptStatus())) {
	        	NotificationVO notificationVO = new NotificationVO();
	        	notificationVO.setMember(memVO);
	        	notificationVO.setTitle("您的「" + actVO.getActivityTitle() + "」申訴成功");
	        	notificationVO.setContent("您的懲處已取消");
	        	notificationVO.setNotificationType((byte) 2);
	        	
	        	notificationSvc.addNotification(notificationVO);
	        	
	        // 申訴失敗(通知被檢舉人)
	        }else if(Integer.valueOf(5).equals(actRptVO.getActRptStatus())) {
	        	NotificationVO notificationVO = new NotificationVO();
	        	notificationVO.setMember(memVO);
	        	notificationVO.setTitle("您的「" + actVO.getActivityTitle() + "」申訴失敗");
	        	notificationVO.setContent("維持原處分");
	        	notificationVO.setNotificationType((byte) 2);
	        	
	        	notificationSvc.addNotification(notificationVO);
	        	
	        }

	        
	        actRptVO.setEmpId(loginEmp);
	        actRptSvc.reviewRpt(actRptVO);
	        
	        return "redirect:/admin/activity/actRptList";
	    }
	    
	    
}
