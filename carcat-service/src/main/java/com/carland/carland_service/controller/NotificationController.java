package com.carland.carland_service.controller;

import com.carland.carland_service.entity.Notification;
import com.carland.carland_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/edit/byId")
    public Notification editNotification(@RequestParam Long notificationId,
                                         @RequestParam boolean setRead,
                                         @RequestHeader("Accept-Language") String acceptLanguage) {
        return notificationService.editNotification(notificationId, setRead, acceptLanguage);
    }

    @PostMapping("/delete/byId")
    public Notification deleteNotification(@RequestParam Long notificationId,
                                           @RequestHeader("Accept-Language") String acceptLanguage) {
        return notificationService.deleteNotification(notificationId, acceptLanguage);
    }


    @GetMapping("/get/list/by/customer")
    public List<Notification> getNotificationListByCustomerId(@RequestHeader("X-User-Id") String userIdHeader,
                                                              @RequestHeader("Accept-Language") String acceptLanguage) {
        return notificationService.getNotificationListByCustomerId(userIdHeader, acceptLanguage);
    }

}
