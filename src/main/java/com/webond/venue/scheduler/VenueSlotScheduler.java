package com.webond.venue.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.webond.venue.service.VenueService;

@Component
public class VenueSlotScheduler {
	
	@Autowired
	private VenueService venueService;

	@Scheduled(cron = "0 10 0 * * *")
//	@Scheduled(fixedRate = 60000)
	public void generateNextDaySlots() {
		venueService.generateNextDaySlotsForAllVenues();
		System.out.println("已更新下一天時段");
	}
}
