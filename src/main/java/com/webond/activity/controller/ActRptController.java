package com.webond.activity.controller;


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


	    
	    @GetMapping("/admin/actRptList")
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
	    
	    
	    @GetMapping("/admin/actRpt/img/{id}")
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
	    
	    @GetMapping("/admin/actRpt/appealImg/{id}")
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
	    
	    
	    
	    @PostMapping("/admin/submit")
	    public String submitAudit(
	    		@ModelAttribute("actRptVO") ActRptVO actRptVO, 
	    		HttpSession session) {
	       
	    	EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
	        if (loginEmp == null) { return "redirect:/employee/login"; }
	        
	        //檢舉單
	        ActRptVO originalRpt = actRptSvc.getOneActRpt(actRptVO.getActRptId());
	        if (originalRpt == null) {
	            return "redirect:/admin/actRptList";
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
	        
	        return "redirect:/admin/actRptList";
	    }
	    
	    
}
