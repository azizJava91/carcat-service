package com.carland.carland_service.util;

import com.carland.carland_service.service.interfaces.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PushCron {

    private final CarService carService;


    //        @Scheduled(cron = "0 0 11 * * *") // her gun saat 11 olan cron
//    @Scheduled(cron = "0 */3 * * * *") // 5 deqiqede bir olan cron
//    @Scheduled(cron = "0 */20 * * * *")
//    public void runDailyServiceReminder() {
//
//        log.info("PushCron started: calculating service reminders");
//
//        carService.calculateAndPushNotification();
//
//        log.info("PushCron finished");
//    }
}
