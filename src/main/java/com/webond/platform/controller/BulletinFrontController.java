package com.webond.platform.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.webond.platform.model.BulletinVO;
import com.webond.platform.service.BulletinService;

@Controller
@RequestMapping("/announcements")
public class BulletinFrontController {
	
	@Autowired
    private BulletinService bulletinSvc;

    // ===== 公告列表（只顯示已發布，可用關鍵字搜尋標題/標籤/內容） =====
    @GetMapping
    public String list(
    		@RequestParam(value = "keyword", required = false) String keyword,
    		@RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
    		ModelMap model) {

        boolean hasDateRange = startDate != null && endDate != null;

        List<BulletinVO> list = hasDateRange
                ? bulletinSvc.getPublishedByDateRange(startDate, endDate)
                : bulletinSvc.getPublishedOrderByDateDesc();
        
        if (keyword != null && !keyword.isBlank()) {
            list = list.stream()
                    .filter(bulletin ->
                            (bulletin.getTitle() != null && bulletin.getTitle().contains(keyword)) ||
                            (bulletin.getTags() != null && bulletin.getTags().contains(keyword)) ||
                            (bulletin.getContent() != null && bulletin.getContent().contains(keyword))
                    )
                    .toList();
        }

        model.addAttribute("bulletinListData", list);
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("searchStartDate", startDate);
        model.addAttribute("searchEndDate", endDate);
        return "front-end/bulletin/myBulletin";
    }

    // ===== 公告內容（單篇，僅限已發布） =====
    @GetMapping("{bulletinId}")
    public String detail(@PathVariable Integer bulletinId, ModelMap model) {
        BulletinVO bulletinVO = bulletinSvc.getPublishedOne(bulletinId);

        if (bulletinVO == null) {
            // 不存在或還是草稿：導回列表，不透露任何錯誤細節
            return "redirect:/announcements";
        }

        model.addAttribute("bulletinVO", bulletinVO);
        return "front-end/bulletin/myBulletinDetail";
    }

    // ===== 公告圖片（僅限已發布，供前台頁面 <img> 讀取） =====
    @GetMapping("{bulletinId}/image")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable Integer bulletinId) {

        BulletinVO bulletinVO = bulletinSvc.getPublishedOne(bulletinId);
        if (bulletinVO == null || bulletinVO.getImage() == null || bulletinVO.getImage().length == 0) {
            return ResponseEntity.notFound().build();
        }

        byte[] imgBytes = bulletinVO.getImage();
        String imgType = "image/jpeg";

        try {
            imgType = URLConnection
                    .guessContentTypeFromStream(new BufferedInputStream(new ByteArrayInputStream(imgBytes)));
            if (imgType == null) {
                imgType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
        } catch (Exception e) {
            // 圖片格式偵測失敗，維持預設 image/jpeg
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imgType))
                .header("Cache-Control", "max-age=3600")
                .body(imgBytes);
    }
}
