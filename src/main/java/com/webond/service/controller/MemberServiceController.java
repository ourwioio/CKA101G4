package com.webond.service.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.member.model.MemberVO;
import com.webond.service.dto.ServiceRequest;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceService;
import com.webond.service.service.ServiceTypeService;

@Controller
@RequestMapping("/member/services")
public class MemberServiceController {

	private final ServiceService serviceSvc;
	private final ServiceTypeService serviceTypeSvc;

	public MemberServiceController(ServiceService serviceSvc,
	                               ServiceTypeService serviceTypeSvc) {
	    this.serviceSvc = serviceSvc;
	    this.serviceTypeSvc = serviceTypeSvc;
	}
	
	// 前往會員新增服務頁面
	// URL: GET /member/services/add
	@GetMapping("/add")
	public String showAddForm(HttpSession session, Model model) {

	    Integer loginMemberId = getLoginMemberId(session);

	    if (loginMemberId == null) {
	        return "redirect:/member/services/fakelogin";
	    }

	    model.addAttribute("serviceRequest", new ServiceRequest());
	    model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
	    model.addAttribute("mode", "add");

	    return "front-end/service/memberServiceForm";
	}
	
	// 會員新增服務
	// URL: POST /member/services/add
	@PostMapping("/add")
	public String addService(@ModelAttribute ServiceRequest request,
	                         HttpSession session,
	                         Model model,
	                         RedirectAttributes redirectAttributes) {

	    Integer loginMemberId = getLoginMemberId(session);

	    if (loginMemberId == null) {
	        return "redirect:/member/services/fakelogin";
	    }

	    try {
	        serviceSvc.addBySeller(
	                loginMemberId,
	                request.getServiceTypeId(),
	                request.getServiceName(),
	                request.getDescription(),
	                request.getHourlyRate()
	        );

	        redirectAttributes.addFlashAttribute("successMsg", "新增服務成功");

	        return "redirect:/member/services";

	    } catch (IllegalArgumentException e) {
	        model.addAttribute("errorMsg", e.getMessage());
	        model.addAttribute("serviceRequest", request);
	        model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
	        model.addAttribute("mode", "add");

	        return "front-end/service/memberServiceForm";
	    }
	}
	
	// 前往會員修改服務頁面
	// URL: GET /member/services/{serviceId}/edit
	@GetMapping("/{serviceId}/edit")
	public String showEditForm(@PathVariable Integer serviceId,
	                           HttpSession session,
	                           Model model,
	                           RedirectAttributes redirectAttributes) {

	    Integer loginMemberId = getLoginMemberId(session);

	    if (loginMemberId == null) {
	        return "redirect:/member/services/fakelogin";
	    }

	    try {
	        ServiceVO serviceVO = serviceSvc.getOwnServiceForEdit(serviceId, loginMemberId);

	        ServiceRequest request = new ServiceRequest();
	        request.setServiceTypeId(serviceVO.getServiceTypeId());
	        request.setServiceName(serviceVO.getServiceName());
	        request.setDescription(serviceVO.getDescription());
	        request.setHourlyRate(serviceVO.getHourlyRate());

	        model.addAttribute("serviceId", serviceId);
	        model.addAttribute("serviceRequest", request);
	        model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
	        model.addAttribute("mode", "edit");

	        return "front-end/service/memberServiceForm";

	    } catch (IllegalArgumentException e) {
	        redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
	        return "redirect:/member/services";
	    }
	}
	
	// 會員修改服務
	// URL: POST /member/services/{serviceId}/edit
	@PostMapping("/{serviceId}/edit")
	public String updateService(@PathVariable Integer serviceId,
	                            @ModelAttribute ServiceRequest request,
	                            HttpSession session,
	                            Model model,
	                            RedirectAttributes redirectAttributes) {

	    Integer loginMemberId = getLoginMemberId(session);

	    if (loginMemberId == null) {
	        return "redirect:/member/services/fakelogin";
	    }

	    try {
	        serviceSvc.updateBySeller(
	                serviceId,
	                loginMemberId,
	                request.getServiceTypeId(),
	                request.getServiceName(),
	                request.getDescription(),
	                request.getHourlyRate()
	        );

	        redirectAttributes.addFlashAttribute("successMsg", "修改服務成功");

	        return "redirect:/member/services";

	    } catch (IllegalArgumentException e) {
	        model.addAttribute("errorMsg", e.getMessage());
	        model.addAttribute("serviceId", serviceId);
	        model.addAttribute("serviceRequest", request);
	        model.addAttribute("serviceTypeList", serviceTypeSvc.getAll());
	        model.addAttribute("mode", "edit");

	        return "front-end/service/memberServiceForm";
	    }
	}
	
	
    // 會員中心：我的服務列表
    // URL: GET /member/services
    @GetMapping
    public String myServiceList(HttpSession session, Model model) {

        MemberVO memberVO = (MemberVO) session.getAttribute("memberVO");

        if (memberVO == null) {
            return "redirect:/member/services/fakelogin";
        }

        Integer loginMemberId = memberVO.getMemberId();

        List<ServiceVO> serviceList = serviceSvc.getManageableServicesByMemberId(loginMemberId);

        model.addAttribute("serviceList", serviceList);
        model.addAttribute("loginMemberId", loginMemberId);

        return "front-end/service/memberServiceList";
    }

    

    // 下架自己的服務
    // URL: POST /member/services/{serviceId}/deactivate
    @PostMapping("/{serviceId}/deactivate")
    public String deactivateService(@PathVariable Integer serviceId,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            serviceSvc.deactivateBySeller(serviceId, loginMemberId);
            redirectAttributes.addFlashAttribute("successMsg", "服務已下架");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/services";
    }

    // 重新上架自己的服務
    // URL: POST /member/services/{serviceId}/activate
    @PostMapping("/{serviceId}/activate")
    public String activateService(@PathVariable Integer serviceId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            serviceSvc.activateBySeller(serviceId, loginMemberId);
            redirectAttributes.addFlashAttribute("successMsg", "服務已重新上架");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/services";
    }

    // 刪除自己的服務
    // 無訂單：真刪除
    // 有訂單：封存 status = 2
    // URL: POST /member/services/{serviceId}/delete
    @PostMapping("/{serviceId}/delete")
    public String deleteService(@PathVariable Integer serviceId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Integer loginMemberId = getLoginMemberId(session);

        if (loginMemberId == null) {
            return "redirect:/member/services/fakelogin";
        }

        try {
            serviceSvc.deleteBySeller(serviceId, loginMemberId);
            redirectAttributes.addFlashAttribute("successMsg", "服務已刪除或封存");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }

        return "redirect:/member/services";
    }

    // 共用：從 Session 取登入會員 ID
    private Integer getLoginMemberId(HttpSession session) {

        MemberVO memberVO = (MemberVO) session.getAttribute("memberVO");

        if (memberVO == null) {
            return null;
        }

        return memberVO.getMemberId();
    }
    
    
    //fake login
 // 顯示服務模組假登入頁
    // URL: GET /member/services/fakelogin
    @GetMapping("/fakelogin")
    public String fakeLoginPage() {
        return "front-end/service/fakelogin";
    }

    // 處理服務模組假登入
    // URL: POST /member/services/fakelogin
    @PostMapping("/fakelogin")
    public String fakeLogin(@RequestParam(required = false) Integer memberId,
                            HttpSession session,
                            Model model) {

        if (memberId == null) {
            model.addAttribute("errorMsg", "請輸入會員編號");
            return "front-end/service/fakelogin";
        }

        MemberVO memberVO = new MemberVO();
        memberVO.setMemberId(memberId);

        session.setAttribute("memberVO", memberVO);

        return "redirect:/member/services";
    }

    // 服務模組測試用登出
    // URL: GET /member/services/fakelogout
    @GetMapping("/fakelogout")
    public String fakeLogout(HttpSession session) {

        session.removeAttribute("memberVO");

        return "redirect:/front/services";
    }
}