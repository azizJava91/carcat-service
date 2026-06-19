package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.request.BulkRequest;
import com.carland.carland_service.dto.request.DeviceTokenRequest;
import com.carland.carland_service.dto.response.BulkResponse;
import com.carland.carland_service.dto.response.DeviceResponse;
import com.carland.carland_service.entity.Customer;
import com.carland.carland_service.entity.DeviceToken;
import com.carland.carland_service.entity.Notification;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.exceptions.InvalidStatusException;
import com.carland.carland_service.exceptions.MissingFieldException;
import com.carland.carland_service.repository.CustomerRepository;
import com.carland.carland_service.repository.DeviceTokenRepository;
import com.carland.carland_service.repository.NotificationRepository;
import com.carland.carland_service.service.interfaces.DeviceTokenService;
import com.carland.carland_service.service.interfaces.PushNotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenServiceImpl implements DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationService pushNotificationService;
    private final NotificationRepository notificationRepository;

    @Transactional
    @Override
    public DeviceResponse saveOrUpdateToken(DeviceTokenRequest requestToken) {
        DeviceToken tokenConflict = deviceTokenRepository.findByDeviceToken(requestToken.getDeviceToken());
        if (tokenConflict != null && !tokenConflict.getUserId().equals(requestToken.getUserId())) {
            deviceTokenRepository.delete(tokenConflict);
        }

        DeviceToken existing = deviceTokenRepository.findByUserId(requestToken.getUserId());

        if (existing != null) {
            if (existing.getDeviceToken().equals(requestToken.getDeviceToken())) {
                return DeviceResponse.builder()
                        .message("Device token already up to date.")
                        .build();
            } else {
                existing.setDeviceToken(requestToken.getDeviceToken());
                existing.setPlatform(requestToken.getPlatform());
                deviceTokenRepository.save(existing);
                return DeviceResponse.builder()
                        .message("Device token updated successfully.")
                        .build();
            }
        } else {
            DeviceToken newToken = DeviceToken.builder()
                    .userId(requestToken.getUserId())
                    .deviceToken(requestToken.getDeviceToken())
                    .platform(requestToken.getPlatform())
                    .build();
            deviceTokenRepository.save(newToken);
            return DeviceResponse.builder()
                    .message("Device token saved successfully.")
                    .build();
        }
    }

    @Override
    public BulkResponse sendBulk(BulkRequest bulkRequest, String acceptLanguage) {

        if (bulkRequest == null || bulkRequest.getCustomerIdList() == null || bulkRequest.getCustomerIdList().isEmpty()
                || bulkRequest.getTitle() == null || bulkRequest.getBody() == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        if (bulkRequest.getTitle().length() > 100) {
            throw new InvalidStatusException("Başlıq mətni 100 simvolu keçə bilməz");
        }

        if (bulkRequest.getBody().length() > 300) {
            throw new InvalidStatusException("Mesaj mətni 300 simvolu keçə bilməz");
        }

        int totalItemCount = bulkRequest.getCustomerIdList().size();

        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByUserIdIn(bulkRequest.getCustomerIdList());

        int successItemCount = 0;

        for (DeviceToken deviceToken : deviceTokens) {
            try {
                Notification notification = Notification.builder()
                        .created(LocalDate.now())
                        .customerId(deviceToken.getUserId())
                        .notificationText(bulkRequest.getBody())
                        .title(bulkRequest.getTitle())
                        .status("ACTIVE")
                        .isRead(false)
                        .type("BULK")
                        .build();

                notificationRepository.save(notification);

                pushNotificationService.send(bulkRequest.getTitle(), bulkRequest.getBody(), deviceToken.getDeviceToken());
                successItemCount++;
            } catch (Exception e) {
                log.error("Push gönderilemedi. userId={}, token={}", deviceToken.getUserId(), deviceToken.getDeviceToken(), e);
            }
        }

        int failedItemCount = totalItemCount - successItemCount;

        return BulkResponse.builder()
                .message("Bulk notification processed")
                .totalItemCount(totalItemCount)
                .successItemCount(successItemCount)
                .failedItemCount(failedItemCount)
                .build();
    }


}


