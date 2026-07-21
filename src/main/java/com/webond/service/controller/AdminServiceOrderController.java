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

    public static final String BASE_PATH =
            "/admin/services/orders";

    private static final String EMPLOYEE_SESSION_KEY =
            "employeeVO";

    private static final String EMPLOYEE_LOGIN_PATH =
            "/admin/login";

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

    private final ServiceOrderService serviceOrderSvc;

    public AdminServiceOrderController(
            ServiceOrderService serviceOrderSvc) {

        this.serviceOrderSvc = serviceOrderSvc;
    }

    // =========================================================
    // 後台服務訂單列表與訂單編號搜尋
    //
    // GET /admin/services/orders
    // GET /admin/services/orders?view=completed-unpaid
    // GET /admin/services/orders?orderId=1
    // =========================================================

    @GetMapping
    public String listOrders(

            @RequestParam(
                    name = "view",
                    defaultValue = VIEW_ALL
            )
            String view,

            @RequestParam(
                    name = "orderId",
                    required = false
            )
            Integer orderId,

            HttpSession session,
            Model model) {

        EmployeeVO employeeVO =
                getLoginEmployeeVO(session);

        if (employeeVO == null) {
            return redirectToEmployeeLogin();
        }

        String selectedView =
                normalizeView(view);

        List<ServiceOrderVO> orderList;

        // =====================================================
        // 有輸入訂單編號時，優先搜尋單筆訂單
        // =====================================================

        if (orderId != null) {

            if (orderId <= 0) {

                orderList = List.of();

                model.addAttribute(
                        "errorMsg",
                        "訂單編號必須大於 0"
                );

            } else {

                ServiceOrderVO order =
                        serviceOrderSvc.getOne(orderId);

                if (order == null) {

                    orderList = List.of();

                    model.addAttribute(
                            "errorMsg",
                            "查無訂單 #" + orderId
                    );

                } else {

                    orderList =
                            List.of(order);
                }
            }

        } else {

            // 沒有輸入訂單編號時，依分類查詢
            orderList =
                    getOrderList(selectedView);
        }

        // =====================================================
        // 訂單資料
        // =====================================================

        model.addAttribute(
                "orderList",
                orderList
        );

        model.addAttribute(
                "selectedView",
                selectedView
        );

        /*
         * HTML 會用這個值判斷：
         * 1. 是否顯示搜尋條件
         * 2. 是否自動捲動到訂單列表
         */
        model.addAttribute(
                "searchOrderId",
                orderId
        );

        // =====================================================
        // 登入員工
        // =====================================================

        model.addAttribute(
                "loginEmployeeId",
                employeeVO.getEmployeeId()
        );

        model.addAttribute(
                "loginEmployeeVO",
                employeeVO
        );

        // =====================================================
        // 後台統計
        // =====================================================

        model.addAttribute(
                "completedTotalAmount",
                safeLong(
                        serviceOrderSvc
                                .getCompletedOrderTotalAmount()
                )
        );

        model.addAttribute(
                "completedOrderCount",
                safeLong(
                        serviceOrderSvc
                                .getCompletedOrderCount()
                )
        );

        model.addAttribute(
                "completedUnpaidTotalAmount",
                safeLong(
                        serviceOrderSvc
                                .getCompletedUnpaidTotalAmount()
                )
        );

        model.addAttribute(
                "completedUnpaidOrderCount",
                safeLong(
                        serviceOrderSvc
                                .getCompletedUnpaidOrderCount()
                )
        );

        model.addAttribute(
                "pendingRefundAmount",
                safeLong(
                        serviceOrderSvc
                                .getPendingRefundAmount()
                )
        );

        model.addAttribute(
                "pendingRefundOrderCount",
                safeLong(
                        serviceOrderSvc
                                .getPendingRefundOrderCount()
                )
        );

        return "back-end/service/adminServiceOrderList";
    }

    // =========================================================
    // 完成撥款
    // =========================================================

    @PostMapping("/{orderId}/payout")
    public String completePayout(

            @PathVariable
            Integer orderId,

            @RequestParam(
                    name = "view",
                    defaultValue = VIEW_COMPLETED_UNPAID
            )
            String view,

            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer employeeId =
                getLoginEmployeeId(session);

        if (employeeId == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "請先登入員工帳號"
            );

            return redirectToEmployeeLogin();
        }

        try {

            serviceOrderSvc.payout(
                    orderId,
                    employeeId
            );

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "訂單 #"
                            + orderId
                            + " 已完成撥款，處理員工 #"
                            + employeeId
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

        return redirectToOrderList();
    }

    // =========================================================
    // 完成退款
    // =========================================================

    @PostMapping("/{orderId}/refund")
    public String completeRefund(

            @PathVariable
            Integer orderId,

            @RequestParam(
                    name = "view",
                    defaultValue =
                            VIEW_CANCELLED_PENDING_REFUND
            )
            String view,

            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer employeeId =
                getLoginEmployeeId(session);

        if (employeeId == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "請先登入員工帳號"
            );

            return redirectToEmployeeLogin();
        }

        try {

            serviceOrderSvc.completeRefund(
                    orderId,
                    employeeId
            );

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "訂單 #"
                            + orderId
                            + " 已完成退款，處理員工 #"
                            + employeeId
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

        return redirectToOrderList();
    }

    // =========================================================
    // 依分類取得訂單
    // =========================================================

    private List<ServiceOrderVO> getOrderList(
            String view) {

        return switch (view) {

            case VIEW_COMPLETED_UNPAID ->
                    serviceOrderSvc
                            .getCompletedUnpaidOrders();

            case VIEW_COMPLETED_PAID ->
                    serviceOrderSvc
                            .getCompletedPaidOrders();

            case VIEW_CANCELLED_NO_REFUND ->
                    serviceOrderSvc
                            .getCancelledNoRefundOrders();

            case VIEW_CANCELLED_PENDING_REFUND ->
                    serviceOrderSvc
                            .getCancelledPendingRefundOrders();

            case VIEW_CANCELLED_REFUNDED ->
                    serviceOrderSvc
                            .getCancelledRefundedOrders();

            default ->
                    serviceOrderSvc.getAll();
        };
    }

    // =========================================================
    // 取得登入員工
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

    private Integer getLoginEmployeeId(
            HttpSession session) {

        EmployeeVO employeeVO =
                getLoginEmployeeVO(session);

        return employeeVO == null
                ? null
                : employeeVO.getEmployeeId();
    }

    // =========================================================
    // 驗證分類
    // =========================================================

    private String normalizeView(
            String view) {

        if (VIEW_COMPLETED_UNPAID.equals(view)
                || VIEW_COMPLETED_PAID.equals(view)
                || VIEW_CANCELLED_NO_REFUND.equals(view)
                || VIEW_CANCELLED_PENDING_REFUND.equals(view)
                || VIEW_CANCELLED_REFUNDED.equals(view)) {

            return view;
        }

        return VIEW_ALL;
    }

    private long safeLong(
            Number value) {

        return value == null
                ? 0L
                : value.longValue();
    }

    private String redirectToOrderList() {

        return "redirect:" + BASE_PATH;
    }

    private String redirectToEmployeeLogin() {

        return "redirect:"
                + EMPLOYEE_LOGIN_PATH;
    }
}