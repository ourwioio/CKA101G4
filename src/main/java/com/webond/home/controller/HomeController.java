package com.webond.home.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;
import com.webond.home.service.HomeService;

@Controller
public class HomeController {

    @Autowired
    private HomeService homeService;
    
    @Autowired
    private ActivityService activityService;
    
    
    

    @GetMapping("/")
    public String home(ModelMap model) {
        List<ActivityVO> endingSoonActivities = homeService.getEndingSoonActivities(4);

        model.addAttribute("recommendedServices", homeService.getRecommendedServices(4));
        model.addAttribute("endingSoonActivities", endingSoonActivities);
        model.addAttribute("randomVenues", homeService.getRandomVenues(4));
        model.addAttribute("registrationStatusMap",
                homeService.getRegistrationStatusMap(endingSoonActivities));

        return "index";
    }

     
}