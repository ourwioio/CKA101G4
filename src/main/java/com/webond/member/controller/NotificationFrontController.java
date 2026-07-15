package com.webond.member.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.webond.member.model.MemberVO;
import com.webond.member.model.NotificationVO;
import com.webond.member.service.NotificationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/front/notification")
public class NotificationFrontController {

    @Autowired
    NotificationService notificationService;


    @GetMapping("listAllNotification")
    public String listAllNotification(
            ModelMap model,
            HttpSession session) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute("memberVO");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        List<NotificationVO> notificationList = notificationService.getNotificationByMemberId( loginMember.getMemberId());

        model.addAttribute("notificationVO", notificationList);

        return "front-end/member/listAllNotification";
    }


    @GetMapping("getOneNotification")
    public String getOne(
            @RequestParam("notificationId")
            Integer notificationId,
            HttpSession session,
            ModelMap model) {

        MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");

        if (loginMember == null) {
            return "redirect:/member/login";
        }

        NotificationVO notificationVO =notificationService.getOneNotification(notificationId);

        notificationService.markNotificationAsRead(notificationId);

        model.addAttribute("notificationVO",notificationVO);

        return "front-end/member/listOneNotification";
    }
    
    @PostMapping("/markAllRead")
    @ResponseBody
    public ResponseEntity<String> markAllRead(HttpSession session){

        MemberVO loginMember = (MemberVO) session.getAttribute("memberVO");
        
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        notificationService.markAllNotificationAsRead(loginMember.getMemberId());
        return ResponseEntity.ok("success");   
        
    }


    /**
     * AJAX 輪詢未讀通知數量
     * GET /front/notification/unread-count
     */
    @GetMapping("/unread-count")
    @ResponseBody
    public int getUnreadCount(
            HttpSession session) {

        MemberVO loginMember =
                (MemberVO) session.getAttribute("memberVO");

        if (loginMember == null) {
            return 0;
        }

        return notificationService.countUnread(
                loginMember.getMemberId()
        );
    }
}