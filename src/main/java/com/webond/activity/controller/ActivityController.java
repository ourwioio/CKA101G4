package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

	// 查詢全部列表
	@GetMapping("/listAllActivity")
	public String listAllActivity(Model model) {
		// 呼叫 ActivityService 的 getAll()
		model.addAttribute("activityListData", activitySvc.getAll());

		// 取得活動類型，供前端 Thymeleaf 轉換名稱用
		model.addAttribute("typeListData", activityTypeSvc.getAll());

		return "front-end/activity/listAllActivity";
	}

	// 新增活動 (Add)
	// 1. 導向新增頁面
	@GetMapping("/addActivity")
	public String addActivity(Model model) {
		// 準備一個空的 VO，讓 Thymeleaf 表單 (th:object) 綁定
		model.addAttribute("activityVO", new ActivityVO());
		// 傳遞類型清單給下拉選單使用
		model.addAttribute("typeListData", activityTypeSvc.getAll());

		return "front-end/activity/addActivity";
	}

	// 2. 處理表單送出
	@PostMapping("/insert")
	public String insert(@ModelAttribute("activityVO") ActivityVO activityVO) {
		// 呼叫剛才更新的 saveActivity 進行新增
		activitySvc.saveActivity(activityVO);

		// 新增完成後，重新導向回列表頁面
		return "redirect:/front-end/activity/listAllActivity";
	}

	// 修改活動 (Update)
	// 1. 導向修改頁面
	@GetMapping("/updateActivity")
	public String updateActivity(@RequestParam("id") Integer activityId, Model model) {
		// 呼叫 Service 的 getOneActivity 找出原本的資料
		ActivityVO activityVO = activitySvc.getOneActivity(activityId);

		// 將查出的舊資料放進 Model，Thymeleaf 就會自動填寫進表單
		model.addAttribute("activityVO", activityVO);
		model.addAttribute("typeListData", activityTypeSvc.getAll());

		return "front-end/activity/updateActivity";
	}

	// 2. 處理修改送出
	@PostMapping("/update")
	public String update(@ModelAttribute("activityVO") ActivityVO formVO) {

	    // 先取得資料庫原本的資料
	    ActivityVO actVO = activitySvc.getOneActivity(formVO.getActivityId());
	    // 找不到資料就回列表
	    if (actVO == null) {
	        return "redirect:/activity/listAllActivity";
	    }
	    // 只更新畫面有提供的欄位
	    actVO.setActivityTitle(formVO.getActivityTitle());
	    actVO.setActivityTypeId(formVO.getActivityTypeId());
	    actVO.setActivityPrice(formVO.getActivityPrice());
	    actVO.setMaxParticipants(formVO.getMaxParticipants());
	    actVO.setActivityStatus(formVO.getActivityStatus());
	    // 儲存
	    activitySvc.saveActivity(actVO);

	    return "redirect:/activity/listAllActivity";
	}

	// 刪除活動 (Delete)

	@PostMapping("/deleteActivity")
	public String deleteActivity(@RequestParam("activityId") Integer activityId) {
		// 呼叫 Service 的 deleteActivity 進行刪除
		activitySvc.deleteActivity(activityId);

		return "redirect:/activity/listAllActivity";
	}
}