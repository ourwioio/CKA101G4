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
import com.webond.service.model.ServiceTypeVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceService;
import com.webond.service.service.ServiceTypeService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {

    // Session 裡存放 EmployeeVO 的 key
    private static final String EMPLOYEE_SESSION_KEY =
            "employeeVO";

    // 沒有員工 Session 時使用的測試員工
    private static final int DEFAULT_FAKE_EMPLOYEE_ID =
            1001;

    private final ServiceService serviceSvc;
    private final ServiceTypeService serviceTypeSvc;

    public AdminServiceController(
            ServiceService serviceSvc,
            ServiceTypeService serviceTypeSvc) {

        this.serviceSvc = serviceSvc;
        this.serviceTypeSvc = serviceTypeSvc;
    }

    // =========================================================
    // 後台服務列表
    //
    // GET /admin/services
    // =========================================================

    @GetMapping
    public String listAllServices(
            HttpSession session,
            Model model) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        // 沒有員工 Session，自動執行假登入
        if (loginEmployeeId == null) {
            return "redirect:/admin/services/fake-login";
        }

        List<ServiceVO> serviceList =
                serviceSvc.getAll();

        List<ServiceTypeVO> serviceTypeList =
                serviceTypeSvc.getAll();

        model.addAttribute(
                "serviceList",
                serviceList
        );

        model.addAttribute(
                "serviceTypeList",
                serviceTypeList
        );

        model.addAttribute(
                "loginEmployeeId",
                loginEmployeeId
        );

        model.addAttribute(
                "loginEmployeeVO",
                getLoginEmployeeVO(session)
        );

        return "back-end/service/adminServiceList";
    }

    // =========================================================
    // 後台查看單一服務詳情
    //
    // GET /admin/services/{serviceId}
    // =========================================================

    @GetMapping("/{serviceId}")
    public String getServiceDetail(
            @PathVariable Integer serviceId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        if (loginEmployeeId == null) {
            return "redirect:/admin/services/fake-login";
        }

        ServiceVO serviceVO =
                serviceSvc.getOneService(serviceId);

        if (serviceVO == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "查無此服務"
            );

            return "redirect:/admin/services";
        }

        model.addAttribute(
                "serviceVO",
                serviceVO
        );

        model.addAttribute(
                "loginEmployeeId",
                loginEmployeeId
        );

        model.addAttribute(
                "loginEmployeeVO",
                getLoginEmployeeVO(session)
        );

        return "back-end/service/adminServiceDetail";
    }

    // =========================================================
    // 後台新增服務類型
    //
    // POST /admin/services/types/add
    //
    // TYPE_MODE：
    // 0：動態服務
    // 1：靜態服務
    // =========================================================

    @PostMapping("/types/add")
    public String addServiceType(
            @RequestParam String typeName,
            @RequestParam(required = false)
            String description,
            @RequestParam(defaultValue = "0")
            Integer typeMode,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        if (loginEmployeeId == null) {
            return "redirect:/admin/services/fake-login";
        }

        try {

            ServiceTypeVO serviceTypeVO =
                    serviceTypeSvc.add(
                            typeName,
                            normalizeNullableText(description)
                    );

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "服務類型「"
                            + serviceTypeVO.getTypeName()
                            + "」新增成功"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return "redirect:/admin/services";
    }

    // =========================================================
    // 後台平台停用服務
    //
    // POST /admin/services/{serviceId}/disable
    //
    // STATUS：
    // 0：會員下架
    // 1：上架
    // 2：封存
    // 3：平台停用
    // =========================================================

    @PostMapping("/{serviceId}/disable")
    public String disableService(
            @PathVariable Integer serviceId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        if (loginEmployeeId == null) {
            return "redirect:/admin/services/fake-login";
        }

        try {

            serviceSvc.disableByAdmin(serviceId);

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "服務 #" + serviceId
                            + " 已由平台停用"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return "redirect:/admin/services";
    }

    // =========================================================
    // 後台恢復平台停用的服務
    //
    // POST /admin/services/{serviceId}/restore
    //
    // STATUS：
    // 3 平台停用 → 1 上架
    // =========================================================

    @PostMapping("/{serviceId}/restore")
    public String restoreService(
            @PathVariable Integer serviceId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        if (loginEmployeeId == null) {
            return "redirect:/admin/services/fake-login";
        }

        try {

            serviceSvc.restoreByAdmin(serviceId);

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "服務 #" + serviceId
                            + " 已恢復上架"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        return "redirect:/admin/services";
    }

    // =========================================================
    // 測試用員工假登入
    //
    // GET /admin/services/fake-login
    // GET /admin/services/fake-login?employeeId=1001
    // =========================================================

    @GetMapping("/fake-login")
    public String fakeLogin(
            @RequestParam(required = false)
            Integer employeeId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 未指定員工編號時，預設使用 1001
        if (employeeId == null) {
            employeeId = DEFAULT_FAKE_EMPLOYEE_ID;
        }

        if (employeeId <= 0) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "員工編號不正確"
            );

            return "redirect:/admin/services";
        }

        /*
         * 假登入也存 EmployeeVO，
         * 使用方式與真正員工登入一致。
         */
        EmployeeVO employeeVO =
                new EmployeeVO();

        employeeVO.setEmployeeId(
                employeeId
        );

        session.setAttribute(
                EMPLOYEE_SESSION_KEY,
                employeeVO
        );

        redirectAttributes.addFlashAttribute(
                "successMsg",
                "已假登入員工 #" + employeeId
        );

        return "redirect:/admin/services";
    }

    // =========================================================
    // 測試用員工假登出
    //
    // GET /admin/services/fake-logout
    // =========================================================

    @GetMapping("/fake-logout")
    public String fakeLogout(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        session.removeAttribute(
                EMPLOYEE_SESSION_KEY
        );

        redirectAttributes.addFlashAttribute(
                "successMsg",
                "員工 Session 已清除"
        );

        /*
         * 回到服務管理頁後，
         * 因為沒有 EmployeeVO，會再次執行假登入。
         */
        return "redirect:/admin/services";
    }

    // =========================================================
    // 共用：取得 Session 中的 EmployeeVO
    // =========================================================

    private EmployeeVO getLoginEmployeeVO(
            HttpSession session) {

        Object sessionObject =
                session.getAttribute(
                        EMPLOYEE_SESSION_KEY
                );

        if (sessionObject instanceof EmployeeVO employeeVO) {
            return employeeVO;
        }

        return null;
    }

    // =========================================================
    // 共用：從 EmployeeVO 取得員工 ID
    // =========================================================

    private Integer getLoginEmployeeId(
            HttpSession session) {

        EmployeeVO employeeVO =
                getLoginEmployeeVO(session);

        if (employeeVO == null) {
            return null;
        }

        return employeeVO.getEmployeeId();
    }

    // =========================================================
    // 共用：空白文字轉成 null
    // =========================================================

    private String normalizeNullableText(
            String text) {

        if (text == null) {
            return null;
        }

        String normalized =
                text.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}