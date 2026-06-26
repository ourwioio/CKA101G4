package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;

import java.util.List;

@RestController // 告訴 Spring 這個類別專門回傳 JSON
@RequestMapping("/api/activity") // 網址統一從這裡開始
@CrossOrigin(origins = "*") // 允許所有前端 (Vue) 來呼叫這個 API
public class ActivityController {

    @Autowired
    private ActivityService actSvc;

    // 處理 GET 請求：查全部
    @GetMapping
    public List<ActivityVO> getAll() {
        return actSvc.getAll();
    }

    // 處理 POST 請求：新增資料 (@RequestBody 代表接收前端傳來的 JSON)
    @PostMapping
    public ActivityVO insert(@RequestBody ActivityVO vo) {
        return actSvc.saveActivity(vo);
    }

    // 處理 DELETE 請求：刪除資料
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Integer id) {
        actSvc.deleteActivity(id);
        return "刪除成功";
    }
}