package com.carland.carland_service.service.impl;

import com.carland.carland_service.service.interfaces.PushNotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PushNotificationServiceImpl implements PushNotificationService {

    @Override
    public void send(String title, String body, String deviceToken) {

        Message message = Message.builder()
                .setToken(deviceToken)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push göndərildi. token={}, response={}", deviceToken, response);
        } catch (FirebaseMessagingException e) {
            log.error("Firebase push göndərilmədi. token={}", deviceToken, e);
            throw new RuntimeException("Push notification göndərilmədi");
        }
    }
}
