package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.request.AutoServiceRequest;
import com.carland.carland_service.dto.request.ServiceHistoryRequest;
import com.carland.carland_service.dto.request.ServiceRequest;
import com.carland.carland_service.dto.response.AutoServiceResponse;
import com.carland.carland_service.dto.response.ServiceHistoryResponse;
import com.carland.carland_service.dto.response.ServiceResponse;
import com.carland.carland_service.entity.*;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.enums.EnumUserRoles;
import com.carland.carland_service.enums.EnumUserStatus;
import com.carland.carland_service.exceptions.*;
import com.carland.carland_service.repository.*;
import com.carland.carland_service.service.interfaces.AutoServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutoServiceServiceImpl implements AutoServiceService {

    private final SuperAdminRepository superAdminRepository;
    private final AutoServiceRepository autoServiceRepository;
    private final AdminRepository adminRepository;
    private final CarRepository carRepository;
    private final ServiceEntityRepository serviceEntityRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;

    @Override
    public AutoServiceResponse createAutoService(AutoServiceRequest autoServiceRequest, String phoneNumber, String role,
                                                 String userIdHeader, String timezone, String acceptLanguage) {
        if (!role.equals(EnumUserRoles.SUPER_ADMIN.name())) {
            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
        }
        if (autoServiceRequest == null || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        SuperAdmin superAdmin = superAdminRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader),
                phoneNumber, EnumUserStatus.ACTIVE.name());

        if (superAdmin == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        AutoService autoService = autoServiceRepository.findBySuperAdmin(superAdmin);

        if (autoService != null) {
            throw new AlreadyExistsException(EnumMessagesLangValues.AUTO_SERVICE_ALREADY_EXISTS.getMessageByLang(acceptLanguage));
        }

        AutoService newAutoService = AutoService.builder()
                .name(autoServiceRequest.getName())
                .address(autoServiceRequest.getAddress())
                .phoneNumber(autoServiceRequest.getPhoneNumber())
                .email(autoServiceRequest.getEmail())
                .superAdmin(superAdmin)
                .build();


        autoServiceRepository.save(newAutoService);
        superAdmin.setAutoService(newAutoService);
        superAdminRepository.save(superAdmin);
        return AutoServiceResponse.builder()
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }

    @Override
    public ServiceHistoryResponse insertServiceHistory(ServiceHistoryRequest request, String phoneNumber,
                                                       String userIdHeader, String role, String timezone,
                                                       String acceptLanguage) {

        if (request == null || request.getVin() == null || phoneNumber == null || userIdHeader == null || role == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        if (!role.equals(EnumUserRoles.ADMIN.name())) {
            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
        }

        Admin admin = adminRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber,
                EnumUserStatus.ACTIVE.name());

        if (admin == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        AutoService autoService = admin.getAutoService();

        if (autoService == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.AUTO_SERVICE_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByVin(request.getVin());

        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        ServiceEntity serviceEntity = serviceEntityRepository.findByServiceName(request.getServiceName());
        if (serviceEntity == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.SERVICE_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        ServiceHistory serviceHistory = ServiceHistory.builder()
                .serviceName(request.getServiceName())
                .actionType(serviceEntity.getActionType())
                .serviceAmount(request.getServiceAmount())
                .workedAutoServiceId(autoService.getId())
                .doneDate(request.getDoneDate())
                .doneKm(request.getDoneKm())
                .car(car)
                .build();
        serviceHistoryRepository.save(serviceHistory);

        return ServiceHistoryResponse.builder()
                .id(serviceHistory.getId())
                .serviceName(serviceHistory.getServiceName())
                .actionType(serviceHistory.getActionType())
                .doneKm(serviceHistory.getDoneKm())
                .doneDate(serviceHistory.getDoneDate())
                .serviceName(serviceHistory.getServiceName())
                .serviceAmount(serviceHistory.getServiceAmount())
                .workedAutoServiceId(serviceHistory.getWorkedAutoServiceId())
                .workedAutoServiceName(autoService.getName())
                .build();
    }

    @Override
    public ServiceResponse getService(ServiceRequest request, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {
        if (request.getServiceName() == null || request.getAutoServiceId() == null) {
            throw new MissingFieldException(EnumMessagesLangValues.SERVICE_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        AutoService autoService = autoServiceRepository.findById(request.getAutoServiceId()).orElseThrow(
                () -> new ResourceNotFoundException(EnumMessagesLangValues.AUTO_SERVICE_NOT_FOUND.getMessageByLang(acceptLanguage)));

        return null;
    }

    @Override
    public ServiceResponse addServiceAmount(ServiceRequest request, String phoneNumber, String userIdHeader, String role, String timezone, String acceptLanguage) {
        return null;
    }

}
