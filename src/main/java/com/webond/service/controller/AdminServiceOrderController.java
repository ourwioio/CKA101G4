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
import com.webond.service.model.ServiceOrderVO;
import com.webond.service.service.ServiceOrderService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping(AdminServiceOrderController.BASE_PATH)
public class AdminServiceOrderController {

    // =========================================================
    // 路徑設定
    // =========================================================

    public static final String BASE_PATH =
            "/admin/services/orders";

    // =========================================================
    // Session 設定
    // =========================================================

    /*
     * 真正員工登入成功後，也要使用相同的 Session key：
     *
     * session.setAttribute("employeeVO", employeeVO);
     */
    private static final String EMPLOYEE_SESSION_KEY =
            "employeeVO";

    // 測試用預設員工 ID
    private static final int DEFAULT_FAKE_EMPLOYEE_ID =
            1001;

    // =========================================================
    // 訂單分類
    // =========================================================

    private static final String VIEW_ALL =
            "all";

    private static final String VIEW_COMPLETED_UNPAID =
            "completed-unpaid";

    private static final String VIEW_COMPLETED_PAID =
            "completed-paid";

    private static final String VIEW_CANCELLED_NO_REFUND =
            "cancelled-no-refund";

    private static final String VIEW_CANCELLED_PENDING_REFUND =
            "cancelled-pending-refund";

    private static final String VIEW_CANCELLED_REFUNDED =
            "cancelled-refunded";

    // 已完成訂單狀態
    private static final byte ORDER_COMPLETED =
            3;

    private final ServiceOrderService serviceOrderSvc;

    public AdminServiceOrderController(
            ServiceOrderService serviceOrderSvc) {

        this.serviceOrderSvc = serviceOrderSvc;
    }

    // =========================================================
    // 後台服務訂單列表
    //
    // GET /admin/services/orders
    // GET /admin/services/orders?view=completed-unpaid
    // GET /admin/services/orders?view=completed-paid
    // GET /admin/services/orders?view=cancelled-no-refund
    // GET /admin/services/orders?view=cancelled-pending-refund
    // GET /admin/services/orders?view=cancelled-refunded
    // =========================================================

    @GetMapping
    public String listOrders(
            @RequestParam(
                    name = "view",
                    defaultValue = VIEW_ALL
            )
            String view,
            HttpSession session,
            Model model) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        /*
         * Session 中沒有 EmployeeVO，
         * 就執行測試用假登入。
         */
        if (loginEmployeeId == null) {
            return "redirect:"
                    + BASE_PATH
                    + "/fake-login";
        }

        view = normalizeView(view);

        List<ServiceOrderVO> orderList;

        switch (view) {

            // 已完成、尚未撥款
            case VIEW_COMPLETED_UNPAID:

                orderList =
                        serviceOrderSvc
                                .getCompletedUnpaidOrders();

                break;

            // 已完成、已撥款
            case VIEW_COMPLETED_PAID:

                orderList =
                        serviceOrderSvc
                                .getCompletedPaidOrders();

                break;

            // 已取消、不需退款
            case VIEW_CANCELLED_NO_REFUND:

                orderList =
                        serviceOrderSvc
                                .getCancelledNoRefundOrders();

                break;

            // 已取消、等待退款
            case VIEW_CANCELLED_PENDING_REFUND:

                orderList =
                        serviceOrderSvc
                                .getCancelledPendingRefundOrders();

                break;

            // 已取消、已退款
            case VIEW_CANCELLED_REFUNDED:

                orderList =
                        serviceOrderSvc
                                .getCancelledRefundedOrders();

                break;

            // 全部訂單
            case VIEW_ALL:
            default:

                orderList =
                        serviceOrderSvc.getAll();

                break;
        }

        // =====================================================
        // 後台統計
        // =====================================================

        // 已完成訂單總金額
        long completedTotalAmount =
                safeLong(
                        serviceOrderSvc
                                .getCompletedOrderTotalAmount()
                );

        // 已完成訂單筆數
        long completedOrderCount =
                serviceOrderSvc
                        .getByOrderStatus(ORDER_COMPLETED)
                        .size();

        // 已完成但尚未撥款總金額
        long completedUnpaidTotalAmount =
                safeLong(
                        serviceOrderSvc
                                .getCompletedUnpaidTotalAmount()
                );

        // 已完成但尚未撥款筆數
        long completedUnpaidOrderCount =
                safeLong(
                        serviceOrderSvc
                                .getCompletedUnpaidOrderCount()
                );

        // 等待退款總金額
        long pendingRefundAmount =
                safeLong(
                        serviceOrderSvc
                                .getPendingRefundAmount()
                );

        // 等待退款筆數
        long pendingRefundOrderCount =
                safeLong(
                        serviceOrderSvc
                                .getPendingRefundOrderCount()
                );

        // =====================================================
        // 傳給 Thymeleaf
        // =====================================================

        model.addAttribute(
                "orderList",
                orderList
        );

        model.addAttribute(
                "selectedView",
                view
        );

        model.addAttribute(
                "loginEmployeeId",
                loginEmployeeId
        );

        model.addAttribute(
                "loginEmployeeVO",
                getLoginEmployeeVO(session)
        );

        model.addAttribute(
                "completedTotalAmount",
                completedTotalAmount
        );

        model.addAttribute(
                "completedOrderCount",
                completedOrderCount
        );

        model.addAttribute(
                "completedUnpaidTotalAmount",
                completedUnpaidTotalAmount
        );

        model.addAttribute(
                "completedUnpaidOrderCount",
                completedUnpaidOrderCount
        );

        model.addAttribute(
                "pendingRefundAmount",
                pendingRefundAmount
        );

        model.addAttribute(
                "pendingRefundOrderCount",
                pendingRefundOrderCount
        );

        return "back-end/service/adminServiceOrderList";
    }

    // =========================================================
    // 完成撥款
    //
    // POST /admin/services/orders/{orderId}/payout
    //
    // 執行後：
    // PAYOUT_STATUS = 1
    // EMPLOYEE_ID = EmployeeVO.getEmployeeId()
    // HANDLED_AT = 現在時間
    // =========================================================

    @PostMapping("/{orderId}/payout")
    public String completePayout(
            @PathVariable Integer orderId,
            @RequestParam(
                    name = "view",
                    defaultValue = VIEW_COMPLETED_UNPAID
            )
            String view,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        if (loginEmployeeId == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "請先登入員工帳號"
            );

            return "redirect:"
                    + BASE_PATH
                    + "/fake-login";
        }

        try {

            serviceOrderSvc.payout(
                    orderId,
                    loginEmployeeId
            );

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "訂單 #" + orderId
                            + " 已完成撥款，處理員工 #"
                            + loginEmployeeId
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        redirectAttributes.addAttribute(
                "view",
                normalizeView(view)
        );

        return "redirect:" + BASE_PATH;
    }

    // =========================================================
    // 完成退款
    //
    // POST /admin/services/orders/{orderId}/refund
    //
    // 執行後：
    // REFUND_STATUS = 2
    // EMPLOYEE_ID = EmployeeVO.getEmployeeId()
    // HANDLED_AT = 現在時間
    // =========================================================

    @PostMapping("/{orderId}/refund")
    public String completeRefund(
            @PathVariable Integer orderId,
            @RequestParam(
                    name = "view",
                    defaultValue =
                            VIEW_CANCELLED_PENDING_REFUND
            )
            String view,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer loginEmployeeId =
                getLoginEmployeeId(session);

        if (loginEmployeeId == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "請先登入員工帳號"
            );

            return "redirect:"
                    + BASE_PATH
                    + "/fake-login";
        }

        try {

            serviceOrderSvc.completeRefund(
                    orderId,
                    loginEmployeeId
            );

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "訂單 #" + orderId
                            + " 已完成退款，處理員工 #"
                            + loginEmployeeId
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        redirectAttributes.addAttribute(
                "view",
                normalizeView(view)
        );

        return "redirect:" + BASE_PATH;
    }

    // =========================================================
    // 測試用員工假登入
    //
    // GET /admin/services/orders/fake-login
    // GET /admin/services/orders/fake-login?employeeId=1001
    //
    // Session：
    // employeeVO -> EmployeeVO
    // =========================================================

    @GetMapping("/fake-login")
    public String fakeLogin(
            @RequestParam(
                    name = "employeeId",
                    required = false
            )
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
    // GET /admin/services/orders/fake-logout
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
         * 回到訂單頁後，因為沒有 EmployeeVO，
         * 會再次自動執行假登入。
         */
        return "redirect:" + BASE_PATH;
    }

    // =========================================================
    // 共用：從 Session 取得 EmployeeVO
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
    // 共用：統計結果避免 null
    // =========================================================

    private long safeLong(
            Number value) {

        return value == null
                ? 0L
                : value.longValue();
    }

    // =========================================================
    // 共用：確認查詢分類是否合法
    // =========================================================

    private String normalizeView(
            String view) {

        if (VIEW_ALL.equals(view)
                || VIEW_COMPLETED_UNPAID.equals(view)
                || VIEW_COMPLETED_PAID.equals(view)
                || VIEW_CANCELLED_NO_REFUND.equals(view)
                || VIEW_CANCELLED_PENDING_REFUND.equals(view)
                || VIEW_CANCELLED_REFUNDED.equals(view)) {

            return view;
        }

        return VIEW_ALL;
    }
}