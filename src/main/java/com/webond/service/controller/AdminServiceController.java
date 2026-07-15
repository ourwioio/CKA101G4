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
import com.webond.service.service.ServiceService;

import jakarta.servlet.http.HttpSession;

import com.webond.service.service.ServiceImageImportService;
import com.webond.service.service.ServiceImageImportService.ImportResult;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {

    // =========================================================
    // 路徑設定
    // =========================================================

    public static final String BASE_PATH =
            "/admin/services";

    // =========================================================
    // Session 設定
    // =========================================================

    private static final String EMPLOYEE_SESSION_KEY =
            "employeeVO";

    private static final int DEFAULT_FAKE_EMPLOYEE_ID =
            1001;

    // =========================================================
    // Service
    // =========================================================

    private final ServiceService serviceSvc;
    private final ServiceImageImportService
    serviceImageImportService;

    public AdminServiceController(
            ServiceService serviceSvc,
            ServiceImageImportService serviceImageImportService) {

        this.serviceSvc = serviceSvc;

        this.serviceImageImportService =
                serviceImageImportService;
    }

    // =========================================================
    // 後台服務列表
    //
    // GET /admin/services
    // =========================================================
    @GetMapping("/home")
    public String showServiceAdminHome() {

        return "back-end/service/adminServiceHome";
    }
    @GetMapping
    public String listAllServices(
            HttpSession session,
            Model model) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        // 測試階段：沒有員工 Session 時，自動導向假登入
        if (loginEmployeeId == null) {
            return "redirect:"
                    + BASE_PATH
                    + "/fake-login";
        }

        List<ServiceVO> serviceList =
                serviceSvc.getAll();

        model.addAttribute(
                "serviceList",
                serviceList
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
    //
    // 使用 \d+ 限制 serviceId 必須是數字，
    // 避免與 /orders、/fake-login 等固定路徑衝突。
    // =========================================================

    @GetMapping("/{serviceId:\\d+}")
    public String getServiceDetail(
            @PathVariable Integer serviceId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        if (loginEmployeeId == null) {
            return "redirect:"
                    + BASE_PATH
                    + "/fake-login";
        }

        ServiceVO serviceVO =
                serviceSvc.getOneService(serviceId);

        if (serviceVO == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "查無此服務"
            );

            return "redirect:" + BASE_PATH;
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
    // 後台平台停用服務
    //
    // POST /admin/services/{serviceId}/disable
    //
    // 服務狀態：
    // 0：會員下架
    // 1：上架中
    // 2：已封存
    // 3：平台停用
    // =========================================================

    @PostMapping("/{serviceId:\\d+}/disable")
    public String disableService(
            @PathVariable Integer serviceId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        if (loginEmployeeId == null) {
            return "redirect:"
                    + BASE_PATH
                    + "/fake-login";
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

        return "redirect:" + BASE_PATH;
    }

    // =========================================================
    // 後台恢復平台停用的服務
    //
    // POST /admin/services/{serviceId}/restore
    //
    // 狀態變更：
    // 3 平台停用 → 1 上架中
    // =========================================================

    @PostMapping("/{serviceId:\\d+}/restore")
    public String restoreService(
            @PathVariable Integer serviceId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        if (loginEmployeeId == null) {
            return "redirect:"
                    + BASE_PATH
                    + "/fake-login";
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

        return "redirect:" + BASE_PATH;
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

        if (employeeId == null) {
            employeeId = DEFAULT_FAKE_EMPLOYEE_ID;
        }

        if (employeeId <= 0) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "員工編號不正確"
            );

            return "redirect:" + BASE_PATH;
        }

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

        return "redirect:" + BASE_PATH;
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
         * 回到服務列表後，因為 Session 已清除，
         * listAllServices() 會再次導向假登入。
         */
        return "redirect:" + BASE_PATH;
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
    // 共用：取得登入員工編號
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
 // 後台手動匯入服務圖片
 //
 // POST /admin/services/import-images
 // =========================================================

 @PostMapping("/import-images")
 public String importServiceImages(
         HttpSession session,
         RedirectAttributes redirectAttributes) {

     Integer loginEmployeeId =
             getLoginEmployeeId(session);

     if (loginEmployeeId == null) {
         return "redirect:"
                 + BASE_PATH
                 + "/fake-login";
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

     return "redirect:" + BASE_PATH + "/home";
 }
}