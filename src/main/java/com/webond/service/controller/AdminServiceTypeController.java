package com.webond.service.controller;

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
import com.webond.service.service.ServiceTypeService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping(AdminServiceTypeController.BASE_PATH)
public class AdminServiceTypeController {

    public static final String BASE_PATH =
            "/admin/service/types";

    private static final String EMPLOYEE_SESSION_KEY =
            "employeeVO";

    private static final String EMPLOYEE_LOGIN_PATH =
            "/admin/login";

    private final ServiceTypeService serviceTypeSvc;

    public AdminServiceTypeController(
            ServiceTypeService serviceTypeSvc) {

        this.serviceTypeSvc = serviceTypeSvc;
    }

    // =========================================================
    // 服務類型管理及查詢
    //
    // GET /admin/service/types
    // GET /admin/service/types?keyword=陪伴
    // =========================================================

    @GetMapping
    public String listServiceTypes(
            @RequestParam(
                    name = "keyword",
                    required = false
            )
            String keyword,
            HttpSession session,
            Model model) {

        if (getLoginEmployeeVO(session) == null) {
            return redirectToEmployeeLogin();
        }

        String normalizedKeyword =
                normalizeNullableText(keyword);

        model.addAttribute(
                "serviceTypeList",
                serviceTypeSvc.search(normalizedKeyword)
        );

        model.addAttribute(
                "keyword",
                normalizedKeyword == null
                        ? ""
                        : normalizedKeyword
        );

        model.addAttribute(
                "searched",
                normalizedKeyword != null
        );

        return "back-end/service/adminServiceTypeList";
    }

    // =========================================================
    // 新增服務類型
    // =========================================================

    @PostMapping("/add")
    public String addServiceType(
            @RequestParam
            String typeName,
            @RequestParam(
                    required = false
            )
            String description,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (getLoginEmployeeVO(session) == null) {
            return redirectToEmployeeLogin();
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

        return redirectToTypeList();
    }

    // =========================================================
    // 修改服務類型
    // =========================================================

    @PostMapping("/{serviceTypeId}/update")
    public String updateServiceType(
            @PathVariable
            Integer serviceTypeId,
            @RequestParam
            String typeName,
            @RequestParam(
                    required = false
            )
            String description,
            @RequestParam(
                    name = "keyword",
                    required = false
            )
            String keyword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (getLoginEmployeeVO(session) == null) {
            return redirectToEmployeeLogin();
        }

        try {

            ServiceTypeVO updatedType =
                    serviceTypeSvc.update(
                            serviceTypeId,
                            typeName,
                            normalizeNullableText(description)
                    );

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "服務類型「"
                            + updatedType.getTypeName()
                            + "」修改成功"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        String normalizedKeyword =
                normalizeNullableText(keyword);

        if (normalizedKeyword != null) {
            redirectAttributes.addAttribute(
                    "keyword",
                    normalizedKeyword
            );
        }

        return redirectToTypeList();
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
    // 文字處理
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

    // =========================================================
    // Redirect
    // =========================================================

    private String redirectToTypeList() {
        return "redirect:" + BASE_PATH;
    }

    private String redirectToEmployeeLogin() {
        return "redirect:" + EMPLOYEE_LOGIN_PATH;
    }
}