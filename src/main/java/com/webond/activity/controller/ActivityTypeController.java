package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.model.ActivityTypeService;
import com.webond.activity.model.ActivityTypeVO; 

import jakarta.servlet.http.HttpSession;

@Controller 
@RequestMapping("/activityType")
public class ActivityTypeController {

    private static final String ACTIVITY_ADMIN_EMPLOYEE_ID = "activityAdminEmployeeId";

    @Autowired
    private ActivityTypeService activityTypeSvc;

    //查詢全部活動類型
    @GetMapping("/listAllActivityType")
    public String listAllActivityType(Model model, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:/activity/admin/home?loginRequired=true";
        }

        model.addAttribute("typeListData", activityTypeSvc.getAll());
        return "back-end/activity/listAllActivityType"; 
    }
    
    //新增類型 (Add)
    @GetMapping("/addActivityType")
    public String addActivityType(Model model, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:/activity/admin/home?loginRequired=true";
        }

        model.addAttribute("activityTypeVO", new ActivityTypeVO());
        return "back-end/activity/addActivityType";
    }

    @PostMapping("/insert")
    public String insert(@ModelAttribute("activityTypeVO") ActivityTypeVO activityTypeVO, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:/activity/admin/home?loginRequired=true";
        }

        activityTypeSvc.saveType(activityTypeVO); 
        return "redirect:/activityType/listAllActivityType";
    }

    //Update
    @GetMapping("/updateActivityType")
    public String updateActivityType(@RequestParam("id") Integer activityTypeId, Model model, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:/activity/admin/home?loginRequired=true";
        }

        // 使用 service 查詢出該筆資料 (假設您的方法名稱為 getById，且回傳 Optional)
        ActivityTypeVO activityTypeVO = activityTypeSvc.getById(activityTypeId).orElse(null); 
        model.addAttribute("activityTypeVO", activityTypeVO);
        return "back-end/activity/updateActivityType";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute("activityTypeVO") ActivityTypeVO activityTypeVO, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:/activity/admin/home?loginRequired=true";
        }

        activityTypeSvc.saveType(activityTypeVO);
        return "redirect:/activityType/listAllActivityType";
    }
   
    //刪除類型 (Delete)
    @PostMapping("/deleteActivityType")
    public String deleteActivityType(@RequestParam("activityTypeId") Integer activityTypeId, HttpSession session) {
        if (!isLoginEmployee(session)) {
            return "redirect:/activity/admin/home?loginRequired=true";
        }

        activityTypeSvc.deleteType(activityTypeId);
        return "redirect:/activityType/listAllActivityType";
    }

    private boolean isLoginEmployee(HttpSession session) {
        return session.getAttribute(ACTIVITY_ADMIN_EMPLOYEE_ID) instanceof Integer;
    }
}
