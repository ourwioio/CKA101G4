package com.webond.service.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.webond.employee.model.EmployeeVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceImageImportService;
import com.webond.service.service.ServiceImageImportService.ImportResult;
import com.webond.service.service.ServiceService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {

    public static final String BASE_PATH = "/admin/services";

    private static final String EMPLOYEE_SESSION_KEY = "employeeVO";
    private static final int DEFAULT_FAKE_EMPLOYEE_ID = 1001;

    private final ServiceService serviceSvc;
    private final ServiceImageImportService serviceImageImportService;

    public AdminServiceController(
            ServiceService serviceSvc,
            ServiceImageImportService serviceImageImportService) {

        this.serviceSvc = serviceSvc;
        this.serviceImageImportService = serviceImageImportService;
    }

    @GetMapping("/home")
    public String showServiceAdminHome(HttpSession session) {

        if (getLoginEmployeeId(session) == null) {
            return redirectToFakeLogin();
        }

        return "back-end/service/adminServiceHome";
    }

    @GetMapping
    public String listAllServices(
            HttpSession session,
            Model model) {

        EmployeeVO employeeVO = getLoginEmployeeVO(session);

        if (employeeVO == null) {
            return redirectToFakeLogin();
        }

        List<ServiceVO> serviceList = serviceSvc.getAll();

        model.addAttribute("serviceList", serviceList);
        model.addAttribute("loginEmployeeId", employeeVO.getEmployeeId());
        model.addAttribute("loginEmployeeVO", employeeVO);

        return "back-end/service/adminServiceList";
    }

    @GetMapping("/{serviceId:\\d+}")
    public String getServiceDetail(
            @PathVariable Integer serviceId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        EmployeeVO employeeVO = getLoginEmployeeVO(session);

        if (employeeVO == null) {
            return redirectToFakeLogin();
        }

        ServiceVO serviceVO = serviceSvc.getOneService(serviceId);

        if (serviceVO == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "查無此服務");
            return redirectToServiceList();
        }

        model.addAttribute("serviceVO", serviceVO);
        model.addAttribute("loginEmployeeId", employeeVO.getEmployeeId());
        model.addAttribute("loginEmployeeVO", employeeVO);

        return "back-end/service/adminServiceDetail";
    }

    @PostMapping("/{serviceId:\\d+}/disable")
    public String disableService(
            @PathVariable Integer serviceId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (getLoginEmployeeId(session) == null) {
            return redirectToFakeLogin();
        }

        try {
            serviceSvc.disableByAdmin(serviceId);
            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "服務 #" + serviceId + " 已由平台停用"
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return redirectToServiceList();
    }

    @PostMapping("/{serviceId:\\d+}/restore")
    public String restoreService(
            @PathVariable Integer serviceId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (getLoginEmployeeId(session) == null) {
            return redirectToFakeLogin();
        }

        try {
            serviceSvc.restoreByAdmin(serviceId);
            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "服務 #" + serviceId + " 已恢復上架"
            );

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return redirectToServiceList();
    }

    @PostMapping("/import-images")
    public String importServiceImages(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (getLoginEmployeeId(session) == null) {
            return redirectToFakeLogin();
        }

        try {
            ImportResult result =
                    serviceImageImportService.importServiceImages();

            if (result.getFailureCount() == 0) {
                redirectAttributes.addFlashAttribute(
                        "successMsg",
                        "服務圖片匯入完成，共成功匯入 "
                                + result.getSuccessCount()
                                + " 張圖片"
                );
            } else {
                redirectAttributes.addFlashAttribute(
                        "errorMsg",
                        "圖片匯入完成：成功 "
                                + result.getSuccessCount()
                                + " 張，失敗 "
                                + result.getFailureCount()
                                + " 張。"
                                + result.getFailureMessage()
                );
            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "圖片匯入失敗：" + e.getMessage()
            );
        }

        return "redirect:" + "/admin/services" + "/home";
    }

    // 以下是假登入功能，正式登入整合完成後可以移除

    @GetMapping("/fake-login")
    public String fakeLogin(
            @RequestParam(required = false) Integer employeeId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        int fakeEmployeeId =
                employeeId == null
                        ? DEFAULT_FAKE_EMPLOYEE_ID
                        : employeeId;

        if (fakeEmployeeId <= 0) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "員工編號不正確"
            );

            return redirectToServiceList();
        }

        EmployeeVO employeeVO = new EmployeeVO();
        employeeVO.setEmployeeId(fakeEmployeeId);

        session.setAttribute(
                EMPLOYEE_SESSION_KEY,
                employeeVO
        );

        redirectAttributes.addFlashAttribute(
                "successMsg",
                "已假登入員工 #" + fakeEmployeeId
        );

        return redirectToServiceList();
    }

    @GetMapping("/fake-logout")
    public String fakeLogout(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        session.removeAttribute(EMPLOYEE_SESSION_KEY);

        redirectAttributes.addFlashAttribute(
                "successMsg",
                "員工 Session 已清除"
        );

        return redirectToServiceList();
    }

    private EmployeeVO getLoginEmployeeVO(HttpSession session) {

        Object sessionObject =
                session.getAttribute(EMPLOYEE_SESSION_KEY);

        if (sessionObject instanceof EmployeeVO employeeVO) {
            return employeeVO;
        }

        return null;
    }

    private Integer getLoginEmployeeId(HttpSession session) {

        EmployeeVO employeeVO = getLoginEmployeeVO(session);

        return employeeVO == null
                ? null
                : employeeVO.getEmployeeId();
    }

    private String redirectToServiceList() {
        return "redirect:" + "/admin/services";
    }

    private String redirectToFakeLogin() {
        return "redirect:" + "/admin/services" + "/fake-login";
    }
}