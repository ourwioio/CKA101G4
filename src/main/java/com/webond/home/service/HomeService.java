package com.webond.home.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    // 1. 活動狀態為 0
    // 2. 活動尚未結束
    // 3. 報名尚未截止
    // 4. 每次重新整理隨機取指定筆數
    // =========================================================

    public List<ActivityVO> getEndingSoonActivities(int count) {

        LocalDateTime now =
                LocalDateTime.now();

        List<ActivityVO> availableActivities =
                activityService.getAll()
                        .stream()

                        // 活動狀態 0：可顯示／正常舉行
                        .filter(activity ->
                                activity.getActivityStatus() != null
                                && activity.getActivityStatus()
                                           .byteValue() == 0 
                                ||activity.getActivityStatus()
                                           .byteValue() == 1
                        )

                        // 活動尚未結束
                        .filter(activity ->
                                activity.getEndTime() != null
                                && activity.getEndTime()
                                           .isAfter(now)
                        )

                        // 報名尚未截止
                        .filter(activity ->
                                activity.getRegistrationDeadline() != null
                                && activity.getRegistrationDeadline()
                                           .isAfter(now)
                        )

                        .toList();

        return pickRandomItems(
                availableActivities,
                count
        );
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