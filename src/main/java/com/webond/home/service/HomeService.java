package com.webond.home.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    //act controller private 
    private Map<Integer, String> buildRegistrationStatusMap(List<ActivityVO> activityList) {
        Map<Integer, String> statusMap = new HashMap<>();
        for (ActivityVO activityVO : activityList) {
            statusMap.put(activityVO.getActivityId(), getRegistrationStatusText(activityVO));
        }
        return statusMap;
    }

    private String getRegistrationStatusText(ActivityVO activityVO) {
        if (activityVO == null) {
            return "活動不存在";
        }

        LocalDateTime now = LocalDateTime.now();
        if (activityVO.getEndTime() != null && now.isAfter(activityVO.getEndTime())) {
            return "活動已結束";
        }

        if (activityVO.getRegistrationStartTime() != null && now.isBefore(activityVO.getRegistrationStartTime())) {
            return "尚未開放報名";
        }

        if (activityVO.getRegistrationDeadline() != null && now.isAfter(activityVO.getRegistrationDeadline())) {
            return "報名已截止";
        }

        if (activityVO.getRegistrationDeadline() != null
                && Duration.between(now, activityVO.getRegistrationDeadline()).toHours() <= 168) {
            return "即將截止";
        }

        return "報名中";
    }
    
    // for controller    
    public Map<Integer, String> getRegistrationStatusMap(List<ActivityVO> activityList) {
        return buildRegistrationStatusMap(activityList);
    }


    // =========================================================
    // 隨機推薦個人服務
    //
    // 條件：
    // 1. 只顯示上架中的服務
    // 2. 每次重新整理隨機取指定筆數
    // =========================================================

    public List<ServiceVO> getRecommendedServices(int count) {

        List<ServiceVO> activeServices =
                serviceService.getActiveServices();

        return pickRandomItems(
                activeServices,
                count
        );
    }


    // =========================================================
    // 隨機推薦團體活動
    //
    // 條件：
    // 1. 即將截止
    // 2. 報名中
    // =========================================================

    public List<ActivityVO> getEndingSoonActivities(int count) {

        List<ActivityVO> availableActivities =
                activityService.getAll()
                        .stream()

                        // 活動狀態 0 或 1：可顯示／正常舉行 或 延期
                        .filter(activity ->
                                activity.getActivityStatus() != null
                                && (activity.getActivityStatus().byteValue() == 0
                                    || activity.getActivityStatus().byteValue() == 1)
                        )

                        // 只抓「報名中」或「即將截止」的活動
                        .filter(activity -> {
                            String status = getRegistrationStatusText(activity);
                            return "報名中".equals(status) || "即將截止".equals(status);
                        })

                        .toList();

        return pickRandomItems(availableActivities,count);
    }


    // =========================================================
    // 隨機推薦場地
    //
    // 條件：
    // 1. 只顯示上架中的場地
    // 2. 每次重新整理隨機取指定筆數
    // =========================================================

    public List<VenueVO> getRandomVenues(int count) {

        List<VenueVO> activeVenues =
                venueService.getAll()
                        .stream()

                        .filter(venue ->
                                venue.getVenueStatus() != null
                                && venue.getVenueStatus()
                                        .byteValue() == 1
                        )

                        .toList();

        return pickRandomItems(
                activeVenues,
                count
        );
    }


    // =========================================================
    // 共用方法：從清單中隨機取出指定筆數
    //
    // 例如：
    // 原本有 10 筆，count = 3
    // 就會洗牌後隨機回傳 3 筆
    //
    // 若資料只有 2 筆，就只回傳 2 筆，不會發生錯誤
    // =========================================================

    private <T> List<T> pickRandomItems(
            List<T> sourceList,
            int count) {

        if (sourceList == null
                || sourceList.isEmpty()
                || count <= 0) {

            return Collections.emptyList();
        }

        // 複製一份，避免直接修改原始 List
        List<T> shuffledList =
                new ArrayList<>(sourceList);

        // 隨機洗牌
        Collections.shuffle(shuffledList);

        // 避免要求數量超過目前資料筆數
        int resultSize =
                Math.min(
                        count,
                        shuffledList.size()
                );

        return new ArrayList<>(
                shuffledList.subList(
                        0,
                        resultSize
                )
        );
    }
}