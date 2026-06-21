package com.carland.carland_service.service.impl;

import com.carland.carland_service.entity.Notification;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.exceptions.ResourceNotFoundException;
import com.carland.carland_service.repository.NotificationRepository;
import com.carland.carland_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationServiceImpl  implements NotificationService {

private final NotificationRepository notificationRepository;
    @Override
    public Notification editNotification(Long notificationId, boolean setRead, String acceptLanguage) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new ResourceNotFoundException(EnumMessagesLangValues.NOTIFICATION_NOT_FOUND.getMessageByLang(acceptLanguage)));
       notification.setRead(true);
       notificationRepository.save(notification);
        return notification;
    }

    @Override
    public Notification deleteNotification(Long notificationId, String acceptLanguage) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new ResourceNotFoundException(EnumMessagesLangValues.NOTIFICATION_NOT_FOUND.getMessageByLang(acceptLanguage)));
        notification.setStatus("deActive");
        notificationRepository.save(notification);
        return notification;
    }

    @Override
    public List<Notification> getNotificationListByCustomerId(String userIdHeader, String acceptLanguage) {
        List<Notification> notifications= notificationRepository.findAllByCustomerIdAndStatus(Long.valueOf(userIdHeader), "ACTIVE");
        if (notifications.isEmpty()){
            throw new ResourceNotFoundException(EnumMessagesLangValues.NOTIFICATION_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        return notifications;
    }
}
