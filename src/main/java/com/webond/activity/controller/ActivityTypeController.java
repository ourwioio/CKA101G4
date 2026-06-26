package com.webond.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webond.activity.model.ActivityTypeService;
import com.webond.activity.model.ActivityTypeVO;

import java.util.List;

@RestController
@RequestMapping("/api/activity-types")
@CrossOrigin(origins = "*") // 允許前端 Vue 專案跨域調用 API
public class ActivityTypeController {

    // ✨ 修正：不再直接注入 Repository，而是注入更具安全保障的 Service 層！
    @Autowired
    private ActivityTypeService typeService;

    /**
     * API: 獲取所有活動類型字典
     */
    @GetMapping
    public ResponseEntity<List<ActivityTypeVO>> getAll() {
        List<ActivityTypeVO> list = typeService.getAll();
        return ResponseEntity.ok(list);
    }

    /**
     * API: 新增/修改活動類型
     */
    @PostMapping
    public ResponseEntity<?> insert(@RequestBody ActivityTypeVO typeVO) {
        try {
            ActivityTypeVO saved = typeService.saveType(typeVO);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            // 回傳 400 Bad Request 以及邏輯錯誤訊息
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API: 刪除活動類型
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            typeService.deleteType(id);
            return ResponseEntity.ok("分類標籤下架成功！");
        } catch (Exception e) {
            // 回傳 400 Bad Request 並告知無法刪除的原因
            return ResponseEntity.badRequest().body("無法刪除此分類。原因可能為：" + e.getMessage());
        }
    }
}