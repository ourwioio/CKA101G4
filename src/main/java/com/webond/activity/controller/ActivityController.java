package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.webond.activity.model.ActivityOrderService;
import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityTypeService;
import com.webond.activity.model.ActivityVO;

@Controller
@RequestMapping("/activity")
public class ActivityController {

	@Autowired
	private ActivityService activitySvc;

	@Autowired
	private ActivityTypeService activityTypeSvc;

	@Autowired
	private ActivityOrderService activityOrderSvc;

	@GetMapping("/listAllActivity")
	public String listAllActivity(Model model) {
		model.addAttribute("activityListData", activitySvc.getAll());
		model.addAttribute("typeListData", activityTypeSvc.getAll());
		return "front-end/activity/listAllActivity";
	}

	@PostMapping("/takeDown")
	public String takeDownActivity(@RequestParam("activityId") Integer activityId) {
		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		if (activityVO != null) {
			activityVO.setActivityStatus((byte) 2);
			activitySvc.saveActivity(activityVO);
			activityOrderSvc.rejectPendingOrdersByActivity(activityId, null);
		}

		return "redirect:/activity/listAllActivity";
	}

	@GetMapping("/addActivity")
	public String addActivityDisabled() {
		return "redirect:/activity/listAllActivity";
	}

	@PostMapping("/insert")
	public String insertDisabled() {
		return "redirect:/activity/listAllActivity";
	}

	@GetMapping("/updateActivity")
	public String updateActivityDisabled() {
		return "redirect:/activity/listAllActivity";
	}

	@PostMapping("/update")
	public String updateDisabled() {
		return "redirect:/activity/listAllActivity";
	}

	@PostMapping("/deleteActivity")
	public String deleteActivityDisabled() {
		return "redirect:/activity/listAllActivity";
	}

	@GetMapping("/activityHome")
	public String activityHome() {
		return "redirect:/activity/admin/home";
	}
}
