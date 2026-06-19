package com.carland.carland_service.service;

import com.carland.carland_service.entity.Notification;

import java.util.List;

public interface NotificationService {
    Notification editNotification(Long notificationId, boolean setRead, String acceptLanguage);

    Notification deleteNotification(Long notificationId, String acceptLanguage);


    List<Notification> getNotificationListByCustomerId(String userIdHeader, String acceptLanguage);
}
