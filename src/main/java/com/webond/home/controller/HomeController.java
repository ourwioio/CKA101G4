package com.webond.home.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import com.webond.home.service.HomeService;
import com.webond.member.model.MemberVO;

@Controller
public class HomeController {

    @Autowired
    private HomeService homeService;
    


    @GetMapping("/")
    public String home(ModelMap model) {
        model.addAttribute("recommendedServices", homeService.getRecommendedServices(4));
        model.addAttribute("endingSoonActivities", homeService.getEndingSoonActivities(4));
        model.addAttribute("randomVenues", homeService.getRandomVenues(4));
        return "index";
    }
}