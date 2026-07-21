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
import com.webond.service.service.ServiceTypeService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping(AdminServiceController.BASE_PATH)
public class AdminServiceController {

    public static final String BASE_PATH =
            "/admin/services";

    private static final String EMPLOYEE_LOGIN_PATH =
            "/admin/login";

    private static final String EMPLOYEE_SESSION_KEY =
            "employeeVO";

    private final ServiceService serviceSvc;
    private final ServiceTypeService serviceTypeSvc;
    private final ServiceImageImportService serviceImageImportService;

    public AdminServiceController(
            ServiceService serviceSvc,
            ServiceTypeService serviceTypeSvc,
            ServiceImageImportService serviceImageImportService) {

        this.serviceSvc = serviceSvc;
        this.serviceTypeSvc = serviceTypeSvc;
        this.serviceImageImportService =
                serviceImageImportService;
    }

    // =========================================================
    // 服務管理首頁
    // =========================================================

    @GetMapping("/home")
    public String showServiceAdminHome(
            HttpSession session) {

        if (getLoginEmployeeVO(session) == null) {
            return redirectToEmployeeLogin();
        }

        return "back-end/service/adminServiceHome";
    }

    // =========================================================
    // 後台服務列表與複合查詢
    //
    // GET /admin/services
    // GET /admin/services?serviceId=1
    // GET /admin/services?memberId=3
    // GET /admin/services?serviceTypeId=2
    //
    // 三個條件可以單獨或同時使用。
    // =========================================================

    @GetMapping
    public String listAllServices(
            @RequestParam(
                    name = "serviceId",
                    required = false
            )
            Integer serviceId,

            @RequestParam(
                    name = "memberId",
                    required = false
            )
            Integer memberId,

            @RequestParam(
                    name = "serviceTypeId",
                    required = false
            )
            Integer serviceTypeId,

            HttpSession session,
            Model model) {

        if (getLoginEmployeeVO(session) == null) {
            return redirectToEmployeeLogin();
        }

        boolean searched =
                serviceId != null
                        || memberId != null
                        || serviceTypeId != null;

        List<ServiceVO> serviceList;

        if (serviceId != null && serviceId <= 0) {

            serviceList = List.of();

            model.addAttribute(
                    "errorMsg",
                    "服務編號必須大於 0"
            );

        } else if (memberId != null && memberId <= 0) {

            serviceList = List.of();

            model.addAttribute(
                    "errorMsg",
                    "會員編號必須大於 0"
            );

        } else if (serviceTypeId != null
                && serviceTypeId <= 0) {

            serviceList = List.of();

            model.addAttribute(
                    "errorMsg",
                    "服務類型編號不正確"
            );

        } else {

            /*
             * 先取得後台全部服務，再依使用者有填寫的條件篩選。
             *
             * 這樣可以同時支援：
             * 1. 服務編號
             * 2. 會員編號
             * 3. 服務類型
             * 4. 任意條件組合
             */
            serviceList =
                    serviceSvc.getAll()
                            .stream()
                            .filter(service ->
                                    serviceId == null
                                            || serviceId.equals(
                                                    service.getServiceId()
                                            )
                            )
                            .filter(service ->
                                    memberId == null
                                            || memberId.equals(
                                                    service.getMemberId()
                                            )
                            )
                            .filter(service ->
                                    serviceTypeId == null
                                            || serviceTypeId.equals(
                                                    service.getServiceTypeId()
                                            )
                            )
                            .toList();
        }

        model.addAttribute(
                "serviceList",
                serviceList
        );

        model.addAttribute(
                "serviceTypeList",
                serviceTypeSvc.getAll()
        );

        model.addAttribute(
                "searchServiceId",
                serviceId
        );

        model.addAttribute(
                "searchMemberId",
                memberId
        );

        model.addAttribute(
                "searchServiceTypeId",
                serviceTypeId
        );

        model.addAttribute(
                "searched",
                searched
        );

        return "back-end/service/adminServiceList";
    }

    // =========================================================
    // 後台查看單一服務詳情
    // =========================================================

    @GetMapping("/{serviceId:\\d+}")
    public String getServiceDetail(
            @PathVariable
            Integer serviceId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (getLoginEmployeeVO(session) == null) {
            return redirectToEmployeeLogin();
        }

        ServiceVO serviceVO =
                serviceSvc.getOneService(serviceId);

        if (serviceVO == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "查無此服務"
            );

            return redirectToServiceList();
        }

        model.addAttribute(
                "serviceVO",
                serviceVO
        );

        return "back-end/service/adminServiceDetail";
    }

    // =========================================================
    // 後台平台停用服務
    // =========================================================

    @PostMapping("/{serviceId:\\d+}/disable")
    public String disableService(
            @PathVariable
            Integer serviceId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (getLoginEmployeeVO(session) == null) {
            return redirectToEmployeeLogin();
        }

        try {

            serviceSvc.disableByAdmin(serviceId);

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "服務 #"
                            + serviceId
                            + " 已由平台停用"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return redirectToServiceList();
    }

    // =========================================================
    // 後台恢復平台停用服務
    // =========================================================

    @PostMapping("/{serviceId:\\d+}/restore")
    public String restoreService(
            @PathVariable
            Integer serviceId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (getLoginEmployeeVO(session) == null) {
            return redirectToEmployeeLogin();
        }

        try {

            serviceSvc.restoreByAdmin(serviceId);

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "服務 #"
                            + serviceId
                            + " 已恢復上架"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return redirectToServiceList();
    }

    // =========================================================
    // 後台匯入服務圖片
    // =========================================================

    @PostMapping("/import-images")
    public String importServiceImages(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (getLoginEmployeeVO(session) == null) {
            return redirectToEmployeeLogin();
        }

        try {

            ImportResult result =
                    serviceImageImportService
                            .importServiceImages();

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
                    "圖片匯入失敗："
                            + e.getMessage()
            );
        }

        return "redirect:"
                + BASE_PATH
                + "/home";
    }

    // =========================================================
    // Session
    // =========================================================

    private EmployeeVO getLoginEmployeeVO(
            HttpSession session) {

        Object sessionObject =
                session.getAttribute(
                        EMPLOYEE_SESSION_KEY
                );

        if (sessionObject
                instanceof EmployeeVO employeeVO) {

            return employeeVO;
        }

        return null;
    }

    // =========================================================
    // Redirect
    // =========================================================

    private String redirectToServiceList() {
        return "redirect:" + BASE_PATH;
    }

    private String redirectToEmployeeLogin() {
        return "redirect:" + EMPLOYEE_LOGIN_PATH;
    }
}