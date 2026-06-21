package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.response.NotificationResponse;
import com.carland.carland_service.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse userAddDetails(Long userId, String role, String phoneNumber,  String timezone, String acceptLanguage, Long inviterId);

    List<NotificationResponse> getNotificationList(String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);

}
