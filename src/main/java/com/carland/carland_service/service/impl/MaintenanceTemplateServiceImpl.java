package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.request.MaintenanceTemplateRequest;
import com.carland.carland_service.dto.request.ServiceRequest;
import com.carland.carland_service.dto.response.MaintenanceTemplateResponse;
import com.carland.carland_service.dto.response.ServiceResponse;
import com.carland.carland_service.entity.Brand;
import com.carland.carland_service.entity.EngineType;
import com.carland.carland_service.entity.MaintenanceTemplate;
import com.carland.carland_service.entity.ServiceEntity;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.enums.EnumUserRoles;
import com.carland.carland_service.enums.EnumUserStatus;
import com.carland.carland_service.exceptions.AlreadyExistsException;
import com.carland.carland_service.exceptions.InvalidStatusException;
import com.carland.carland_service.exceptions.MissingFieldException;
import com.carland.carland_service.exceptions.ResourceNotFoundException;
import com.carland.carland_service.repository.BrandRepository;
import com.carland.carland_service.repository.MaintenanceTemplateRepository;
import com.carland.carland_service.repository.ServiceEntityRepository;
import com.carland.carland_service.service.interfaces.MaintenanceTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceTemplateServiceImpl implements MaintenanceTemplateService {


    @Value("${super.admin.phone}")
    private String superAdminPhoneNumber;


    private final MaintenanceTemplateRepository maintenanceTemplateRepository;
    private final ServiceEntityRepository serviceEntityRepository;
    private final BrandRepository brandRepository;

    @Override
    public MaintenanceTemplateResponse createMaintenanceTemplate(MaintenanceTemplateRequest request,
                                                                 String role, String phoneNumber, String userIdHeader,
                                                                 String timezone, String acceptLanguage) {
//        if (!phoneNumber.equals(superAdminPhoneNumber) || !role.equals(EnumUserRoles.BOSS.name()) || !userIdHeader.equals("1")) {
//            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
//        }
//
//        if (request.getEngineType() == null ) {
//            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
//        }
//
//
//        MaintenanceTemplate existingTemplate = maintenanceTemplateRepository.
//                findByBrandAndModelAndYearAndEngineTypeAndTransmissionType(
//                        request.getBrand(), request.getModel(), request.getYear(), request.getEngineType(), request.getTransmissionType());
//        if (existingTemplate != null) {
//            throw new AlreadyExistsException(EnumMessagesLangValues.TEMPLATE_ALREADY_EXISTS.getMessageByLang(acceptLanguage));
//        }
//
//        MaintenanceTemplate newTemplate = MaintenanceTemplate.builder()
//                .engineType(request.getEngineType())
//                .transmissionType(request.getTransmissionType())
//                .build();
//
//        maintenanceTemplateRepository.save(newTemplate);
//
//        boolean exists = brandRepository.existsByBrandName(newTemplate.getBrand());
//
//        if (exists) {
//            log.info("brand found, not create new brand");
//        } else {
//            Brand brand = Brand.builder()
//                    .brandName(newTemplate.getBrand())
//                    .status(EnumUserStatus.ACTIVE.name())
//                    .build();
//
//            brandRepository.save(brand);
//        }

        return null;
    }

    @Override
    public List<MaintenanceTemplateResponse> getMaintenanceTemplateList(String phoneNumber, String userIdHeader,
                                                                        String timezone, String acceptLanguage) {

        List<MaintenanceTemplate> templates = maintenanceTemplateRepository.findAll();

        if (templates.isEmpty()) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.TEMPLATE_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        return templates.stream().map(template -> convert(template, acceptLanguage)).toList();
    }

    @Override
    public MaintenanceTemplateResponse addServiceToTemplate(Long templateId, ServiceRequest request, String phoneNumber,
                                                            String userIdHeader, String role, String timezone, String acceptLanguage) {

        if (templateId == null || request == null || request.getServiceName() == null || request.getActionType() == null ||
                request.getIntervalKm() == null || request.getIntervalMonth() == null || role == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }
        if (!phoneNumber.equals(superAdminPhoneNumber) || !role.equals(EnumUserRoles.BOSS.name()) || !userIdHeader.equals("1")) {
            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
        }

        MaintenanceTemplate template = maintenanceTemplateRepository.findById(templateId).orElseThrow(
                () -> new ResourceNotFoundException(EnumMessagesLangValues.TEMPLATE_NOT_FOUND.getMessageByLang(acceptLanguage)));

        ServiceEntity service = serviceEntityRepository.findByServiceNameAndActionTypeAndIntervalKmAndIntervalMonthAndMaintenanceTemplate(
                request.getServiceName(), request.getActionType(), request.getIntervalKm(), request.getIntervalMonth(), template
        );

        if (service != null) {
            throw new AlreadyExistsException(EnumMessagesLangValues.SERVICE_ALREADY_EXISTS.getMessageByLang(acceptLanguage));
        }

        ServiceEntity newService = ServiceEntity.builder()
                .serviceName(request.getServiceName())
                .actionType(request.getActionType())
                .intervalMonth(request.getIntervalMonth())
                .intervalKm(request.getIntervalKm())
                .maintenanceTemplate(template)
                .build();

        template.getServices().add(newService);

        serviceEntityRepository.save(newService);
        maintenanceTemplateRepository.save(template);

        List<ServiceResponse> responses = template.getServices().stream().map(this::convert).toList();
        MaintenanceTemplateResponse response = convert(template, acceptLanguage);
        response.setServiceResponseList(responses);

        return response;
    }

    private MaintenanceTemplateResponse convert(MaintenanceTemplate template, String acceptLanguage) {

        EngineType engineType = template.getEngineType();
        return MaintenanceTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .engineType(engineType.getEngineType())
                .engineTypeId(engineType.getEngineTypeId())
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .serviceResponseList(template.getServices().stream().map(this::convert).toList())
                .build();
    }

    private ServiceResponse convert(ServiceEntity service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .serviceName(service.getServiceName())
                .actionType(service.getActionType())
                .intervalMonth(service.getIntervalMonth())
                .intervalKm(service.getIntervalKm())
                .build();
    }
}
