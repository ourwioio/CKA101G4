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

    public static final String BASE_PATH = "/admin/services/orders";

    private static final String EMPLOYEE_SESSION_KEY = "employeeVO";
    private static final int DEFAULT_FAKE_EMPLOYEE_ID = 1001;

    private static final String VIEW_ALL = "all";
    private static final String VIEW_COMPLETED_UNPAID = "completed-unpaid";
    private static final String VIEW_COMPLETED_PAID = "completed-paid";
    private static final String VIEW_CANCELLED_NO_REFUND = "cancelled-no-refund";
    private static final String VIEW_CANCELLED_PENDING_REFUND = "cancelled-pending-refund";
    private static final String VIEW_CANCELLED_REFUNDED = "cancelled-refunded";

    private final ServiceOrderService serviceOrderSvc;

    public AdminServiceOrderController(ServiceOrderService serviceOrderSvc) {
        this.serviceOrderSvc = serviceOrderSvc;
    }

    @GetMapping
    public String listOrders(
            @RequestParam(defaultValue = VIEW_ALL) String view,
            HttpSession session,
            Model model) {

        EmployeeVO employeeVO = getLoginEmployeeVO(session);

        if (employeeVO == null) {
            return redirectToFakeLogin();
        }

        String selectedView = normalizeView(view);
        List<ServiceOrderVO> orderList = getOrderList(selectedView);

        model.addAttribute("orderList", orderList);
        model.addAttribute("selectedView", selectedView);
        model.addAttribute("loginEmployeeId", employeeVO.getEmployeeId());
        model.addAttribute("loginEmployeeVO", employeeVO);

        model.addAttribute(
                "completedTotalAmount",
                safeLong(serviceOrderSvc.getCompletedOrderTotalAmount())
        );

        model.addAttribute(
                "completedOrderCount",
                safeLong(serviceOrderSvc.getCompletedOrderCount())
        );

        model.addAttribute(
                "completedUnpaidTotalAmount",
                safeLong(serviceOrderSvc.getCompletedUnpaidTotalAmount())
        );

        model.addAttribute(
                "completedUnpaidOrderCount",
                safeLong(serviceOrderSvc.getCompletedUnpaidOrderCount())
        );

        model.addAttribute(
                "pendingRefundAmount",
                safeLong(serviceOrderSvc.getPendingRefundAmount())
        );

        model.addAttribute(
                "pendingRefundOrderCount",
                safeLong(serviceOrderSvc.getPendingRefundOrderCount())
        );

        return "back-end/service/adminServiceOrderList";
    }

    @PostMapping("/{orderId}/payout")
    public String completePayout(
            @PathVariable Integer orderId,
            @RequestParam(defaultValue = VIEW_COMPLETED_UNPAID) String view,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer employeeId = getLoginEmployeeId(session);

        if (employeeId == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "請先登入員工帳號"
            );

            return redirectToFakeLogin();
        }

        try {
            serviceOrderSvc.payout(orderId, employeeId);

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "訂單 #" + orderId
                            + " 已完成撥款，處理員工 #"
                            + employeeId
            );

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        redirectAttributes.addAttribute("view", normalizeView(view));

        return redirectToOrderList();
    }

    @PostMapping("/{orderId}/refund")
    public String completeRefund(
            @PathVariable Integer orderId,
            @RequestParam(defaultValue = VIEW_CANCELLED_PENDING_REFUND) String view,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer employeeId = getLoginEmployeeId(session);

        if (employeeId == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    "請先登入員工帳號"
            );

            return redirectToFakeLogin();
        }

        try {
            serviceOrderSvc.completeRefund(orderId, employeeId);

            redirectAttributes.addFlashAttribute(
                    "successMsg",
                    "訂單 #" + orderId
                            + " 已完成退款，處理員工 #"
                            + employeeId
            );

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(
                    "errorMsg",
                    e.getMessage()
            );
        }

        redirectAttributes.addAttribute("view", normalizeView(view));

        return redirectToOrderList();
    }

    // 測試用假登入，正式登入串接完成後可以移除

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

            return redirectToOrderList();
        }

        EmployeeVO employeeVO = new EmployeeVO();
        employeeVO.setEmployeeId(fakeEmployeeId);

        session.setAttribute(EMPLOYEE_SESSION_KEY, employeeVO);

        redirectAttributes.addFlashAttribute(
                "successMsg",
                "已假登入員工 #" + fakeEmployeeId
        );

        return redirectToOrderList();
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

        return redirectToOrderList();
    }

    private List<ServiceOrderVO> getOrderList(String view) {

        return switch (view) {
            case VIEW_COMPLETED_UNPAID ->
                    serviceOrderSvc.getCompletedUnpaidOrders();

            case VIEW_COMPLETED_PAID ->
                    serviceOrderSvc.getCompletedPaidOrders();

            case VIEW_CANCELLED_NO_REFUND ->
                    serviceOrderSvc.getCancelledNoRefundOrders();

            case VIEW_CANCELLED_PENDING_REFUND ->
                    serviceOrderSvc.getCancelledPendingRefundOrders();

            case VIEW_CANCELLED_REFUNDED ->
                    serviceOrderSvc.getCancelledRefundedOrders();

            default ->
                    serviceOrderSvc.getAll();
        };
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

    private String normalizeView(String view) {

        if (VIEW_COMPLETED_UNPAID.equals(view)
                || VIEW_COMPLETED_PAID.equals(view)
                || VIEW_CANCELLED_NO_REFUND.equals(view)
                || VIEW_CANCELLED_PENDING_REFUND.equals(view)
                || VIEW_CANCELLED_REFUNDED.equals(view)) {

            return view;
        }

        return VIEW_ALL;
    }

    private long safeLong(Number value) {
        return value == null ? 0L : value.longValue();
    }

    private String redirectToOrderList() {
        return "redirect:" + BASE_PATH;
    }

    private String redirectToFakeLogin() {
        return "redirect:" + BASE_PATH + "/fake-login";
    }
}