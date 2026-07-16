package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.config.ActivityEmployeeSession;
import com.webond.activity.model.ActivityTypeService;
import com.webond.activity.model.ActivityTypeVO; 

import jakarta.servlet.http.HttpSession;

@Controller 
@RequestMapping({ "/activityType", "/admin/activity/type" })
public class ActivityTypeController {

    private static final String ADMIN_ACTIVITY_HOME = "/admin/activity/home";
    private static final String ADMIN_TYPE_LIST = "/admin/activity/type/listAllActivityType";

    @Autowired
    private ActivityTypeService activityTypeSvc;

    @Autowired
    private ActivityEmployeeSession employeeSession;

    //查詢全部活動類型
    @GetMapping("/listAllActivityType")
    public String listAllActivityType(Model model, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
        }

        model.addAttribute("typeListData", activityTypeSvc.getAll());
        return "back-end/activity/listAllActivityType"; 
    }
    
    //新增類型 (Add)
    @GetMapping("/addActivityType")
    public String addActivityType(Model model, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
        }

        model.addAttribute("activityTypeVO", new ActivityTypeVO());
        return "back-end/activity/addActivityType";
    }

    @PostMapping("/insert")
    public String insert(@ModelAttribute("activityTypeVO") ActivityTypeVO activityTypeVO, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
        }

        activityTypeSvc.saveType(activityTypeVO); 
        return "redirect:" + ADMIN_TYPE_LIST;
    }

    //Update
    @GetMapping("/updateActivityType")
    public String updateActivityType(@RequestParam("id") Integer activityTypeId, Model model, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
        }

        // 使用 service 查詢出該筆資料 (假設您的方法名稱為 getById，且回傳 Optional)
        ActivityTypeVO activityTypeVO = activityTypeSvc.getById(activityTypeId).orElse(null); 
        model.addAttribute("activityTypeVO", activityTypeVO);
        return "back-end/activity/updateActivityType";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("activityTypeVO") ActivityTypeVO activityTypeVO, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
        }

        activityTypeSvc.saveType(activityTypeVO);
        return "redirect:" + ADMIN_TYPE_LIST;
    }
   
    //刪除類型 (Delete)
    @PostMapping("/deleteActivityType")
    public String deleteActivityType(@RequestParam("activityTypeId") Integer activityTypeId, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:" + ADMIN_ACTIVITY_HOME + "?loginRequired=true";
        }

        activityTypeSvc.deleteType(activityTypeId);
        return "redirect:" + ADMIN_TYPE_LIST;
    }

    private boolean isLoginEmployee(HttpSession session) {
        return employeeSession.isLoginEmployee(session);
    }
}
