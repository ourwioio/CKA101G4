package com.webond.activity.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.activity.model.ActRptService;
import com.webond.activity.model.ActRptVO;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;
import com.webond.employee.model.EmpService;
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
	 
	 @Autowired
	 private EmpService empSvc;


	    
	    @GetMapping({ "/admin/actRptList", "/admin/activity/actRptList" })
	    public String actRptList(
	            @RequestParam(value = "status", required = false) Integer status,
	            @RequestParam(value = "empId",required = false) Integer empId,
	            @RequestParam(value = "rptType", required = false) Integer rptType,
	            @RequestParam(value = "page", defaultValue = "0") int page,
	    		Model model, 
	    		HttpSession session) {
	        
	    	
	    	EmployeeVO loginEmp = (EmployeeVO) session.getAttribute("employeeVO");
	        if (loginEmp == null) {
	            return "redirect:/admin/login"; 
	        }
	        
	        int pageSize = 5;
	        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("actRptId").descending());

	        Page<ActRptVO> reportPage = actRptSvc.getCompositeSearch(status, empId, rptType, pageable);
	        
	        List<EmployeeVO> empList = empSvc.getEmployeesWithActRptPermission(); 
	        model.addAttribute("empList", empList); 
	        
	        model.addAttribute("actRptListData", reportPage.getContent());
	        model.addAttribute("currentStatus", status);
	        model.addAttribute("currentEmpId", empId);
	        model.addAttribute("currentRptType", rptType);
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
	        	String rptTypeChinese = actRptSvc.getRptTypeChinese(originalRpt.getRptType());
	        	notificationVO.setContent("活動已違反：" + rptTypeChinese);
	        	notificationVO.setNotificationType((byte) 2);
	        	
	        	notificationSvc.addNotification(notificationVO);
	        	
	        // 檢舉駁回(通知檢舉人)
	        }else if(Integer.valueOf(2).equals(actRptVO.getActRptStatus())) {
	        	NotificationVO notificationVO = new NotificationVO();
	        	notificationVO.setMember(originalRpt.getReporterId());
	        	notificationVO.setTitle("您的「" + actVO.getActivityTitle() + "」檢舉已被駁回");
	        	notificationVO.setContent("查無違規");
	        	notificationVO.setNotificationType((byte) 2);
	        	
	        	notificationSvc.addNotification(notificationVO);
	        
	        }
	        	
	        	// 申訴成功(通知被檢舉人)	
//	        }else if(Integer.valueOf(4).equals(actRptVO.getActRptStatus())) {
//	        	NotificationVO notificationVO = new NotificationVO();
//	        	notificationVO.setMember(memVO);
//	        	notificationVO.setTitle("您的「" + actVO.getActivityTitle() + "」申訴成功");
//	        	notificationVO.setContent("您的懲處已取消");
//	        	notificationVO.setNotificationType((byte) 2);
//	        	
//	        	notificationSvc.addNotification(notificationVO);
//	        	
//	        // 申訴失敗(通知被檢舉人)
//	        }else if(Integer.valueOf(5).equals(actRptVO.getActRptStatus())) {
//	        	NotificationVO notificationVO = new NotificationVO();
//	        	notificationVO.setMember(memVO);
//	        	notificationVO.setTitle("您的「" + actVO.getActivityTitle() + "」申訴失敗");
//	        	notificationVO.setContent("維持原處分");
//	        	notificationVO.setNotificationType((byte) 2);
//	        	
//	        	notificationSvc.addNotification(notificationVO);
//	        	
//	        }

	        
	        actRptVO.setEmpId(loginEmp);
	        actRptSvc.reviewRpt(actRptVO);
	        
	        return "redirect:/admin/activity/actRptList";
	    }
	    
	    
	    @GetMapping({ "/admin/actRpt/review/{id}", "/admin/activity/actRpt/review/{id}" })
	    public String showReviewPage(
	    		@PathVariable("id") Integer actRptId, 
	    		Model model, 
	    		RedirectAttributes redirectAttributes) {
	        
	        ActRptVO actRptVO = actRptSvc.getOneActRpt(actRptId); 
	        
	        if (actRptVO == null) {
	            redirectAttributes.addFlashAttribute("errorMessage", "❌ 找不到該筆檢舉案件，可能已被處理或刪除。");
	            return "redirect:/admin/activity/actRptList";
	        }
	        
	        if (actRptVO.getActRptStatus() != 0) {
	            redirectAttributes.addFlashAttribute("errorMessage", "⚠️ 該案件已經審核完畢，無法重複審核。");
	            return "redirect:/admin/activity/actRptList";
	        }
	        
	        model.addAttribute("actRptVO", actRptVO);
	        
	        return "back-end/activity/actRpt"; 
	    }
	    
	    
}
