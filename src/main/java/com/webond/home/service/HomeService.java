package com.webond.home.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webond.activity.model.ActivityService;
import com.webond.activity.model.ActivityVO;
import com.webond.service.model.ServiceVO;
import com.webond.service.service.ServiceService;
import com.webond.venue.model.VenueVO;
import com.webond.venue.service.VenueService;

@Service
public class HomeService {
	

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private VenueService venueService;

    

    public List<ServiceVO> getRecommendedServices(int count) {
        List<ServiceVO> all = serviceService.getAll();
        return all.stream()
                .filter(s -> s.getStatus() != null && s.getStatus() == (byte)1) 
                .sorted(Comparator.comparing(ServiceVO::getCreatedAt).reversed()) // 依實際欄位調整，例如上架時間
                .limit(count)
                .toList();
    }
    

    /** 即將截止的團體活動：依截止時間排序，排除已過期 */
    public List<ActivityVO> getEndingSoonActivities(int count) {
        LocalDateTime now = LocalDateTime.now();

        // 篩出所有「上架中 + 正常舉行 + 尚未截止」的活動
        List<ActivityVO> eligible = activityService.getAll().stream()
                .filter(a -> a.getActivityStatus() != null && a.getActivityStatus() == 0)
                .filter(a -> a.getEndTime() != null && a.getEndTime().isAfter(now))
                .toList();

        // 依截止時間排序，取最急迫的
        List<ActivityVO> endingSoon = eligible.stream()
                .sorted(Comparator.comparing(ActivityVO::getEndTime))
                .limit(count)
                .toList();

        // 已經滿額，直接回傳
        if (endingSoon.size() >= count) {
            return endingSoon;
        }

        // 不足額，從剩下符合資格但沒被選到的活動中隨機補
        List<ActivityVO> remaining = new ArrayList<>(eligible);
        remaining.removeAll(endingSoon);
        Collections.shuffle(remaining);

        int need = count - endingSoon.size();
        List<ActivityVO> filled = new ArrayList<>(endingSoon);
        filled.addAll(remaining.stream().limit(need).toList());

        return filled;
    }
    

    /** 隨機推薦場地 */
    public List<VenueVO> getRandomVenues(int count) {
        List<VenueVO> all = venueService.getAll();
        Collections.shuffle(all);
        return all.stream()
                .filter(v -> v.getVenueStatus() != null && v.getVenueStatus() == (byte)1) 
        		.limit(count).toList();
    }

    
    

}
