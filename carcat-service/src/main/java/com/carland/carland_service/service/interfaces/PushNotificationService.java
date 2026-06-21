package com.carland.carland_service.service.interfaces;

public interface PushNotificationService {
    void send(String title, String body, String deviceToken);

}
