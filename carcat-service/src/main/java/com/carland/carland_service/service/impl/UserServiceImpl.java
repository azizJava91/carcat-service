package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.response.NameSurname;
import com.carland.carland_service.dto.response.NotificationResponse;
import com.carland.carland_service.dto.response.UserResponse;
import com.carland.carland_service.entity.Customer;
import com.carland.carland_service.entity.Notification;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.enums.EnumUserStatus;
import com.carland.carland_service.exceptions.MissingFieldException;
import com.carland.carland_service.exceptions.ResourceNotFoundException;
import com.carland.carland_service.exceptions.UserNotFoundException;
import com.carland.carland_service.feign.NameSurnameFeign;
import com.carland.carland_service.repository.CustomerRepository;
import com.carland.carland_service.repository.NotificationRepository;
import com.carland.carland_service.service.interfaces.UserService;
import com.carland.carland_service.util.Helper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final Helper helper;
    private final CustomerRepository customerRepository;
    private final NotificationRepository notificationRepository;
    private final NameSurnameFeign nameSurnameFeign;

    @Override
    public UserResponse userAddDetails(Long userId, String role, String phoneNumber,
                                       String timezone, String acceptLanguage, Long inviterId) {

        if (userId == null || role == null || phoneNumber == null || timezone == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_FIELDS.getMessageByLang(acceptLanguage));
        }


        NameSurname nameSurname = nameSurnameFeign.getNameSurname(userId);
        log.info("name surname feign response : {}", nameSurname);
        helper.checkOrCreateUserByRole(userId, role, phoneNumber, nameSurname.getName(), nameSurname.getSurname(), acceptLanguage, inviterId);

        return UserResponse.builder()
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }

    @Override
    public List<NotificationResponse> getNotificationList(String role, String phoneNumber, String userIdHeader,
                                                          String timezone, String acceptLanguage) {

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader),
                phoneNumber, EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        List<Notification> notifications = notificationRepository.findAllByCustomerId(customer.getUserId());

        if (notifications.isEmpty()) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.NOTIFICATION_NOT_FOUND.
                    getMessageByLang(acceptLanguage));
        }


        return notifications.stream()
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .notificationText(n.getNotificationText())
                        .build())
                .toList();
    }
}
