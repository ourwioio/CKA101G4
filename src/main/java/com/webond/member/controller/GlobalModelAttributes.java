package com.webond.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.webond.member.model.MemberVO;
import com.webond.member.service.NotificationService;
import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    NotificationService notificationService;

    @ModelAttribute("unreadNotificationCount")
    public int unreadNotificationCount(HttpSession session) {
        MemberVO member = (MemberVO) session.getAttribute("memberVO");
        if (member == null) {
            return 0;
        }
        return notificationService.countUnread(member.getMemberId());
    }
}