package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.response.PhotoResponse;
import com.carland.carland_service.entity.*;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.enums.EnumUserRoles;
import com.carland.carland_service.enums.EnumUserStatus;
import com.carland.carland_service.exceptions.*;
import com.carland.carland_service.repository.*;
import com.carland.carland_service.service.interfaces.PhotoService;

import com.carland.carland_service.util.CustomImageCrop;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoServiceImpl implements PhotoService {

    private final CarPhotoRepository carPhotoRepository;
    private final CustomerRepository customerRepository;
    private final CarRepository carRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final PartnerRepository partnerRepository;
    private final PartnerPhotoRepository partnerPhotoRepository;
    @Override
    public ResponseEntity<byte[]> getCarPhoto(
            String role,
            Long carId,
            String phoneNumber,
            String userIdHeader,
            String timezone,
            String acceptLanguage) {

        if (role == null || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(
                    EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        CarPhoto carPhoto = carPhotoRepository.findByCarId(carId);

        if (carPhoto == null) {
            throw new ResourceNotFoundException(
                    EnumMessagesLangValues.CAR_PHOTO_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        String fileType = carPhoto.getFileType();
        log.info("file type ============================== {}", fileType);

        if (fileType == null || fileType.isBlank()) {
            fileType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        if (!fileType.contains("/")) {
            fileType = "image/" + fileType.toLowerCase();
        }

        MediaType mediaType = MediaType.parseMediaType(fileType);
        log.info("media type file type ================= {}", mediaType.getType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(carPhoto.getImageData());
    }

    @Override
    public ResponseEntity<byte[]> getUserPP(String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {
        if (role == null || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(
                    EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        UserPhoto userPhoto = userPhotoRepository.findByUserIdAndUserPhoneNumber(Long.valueOf(userIdHeader), phoneNumber);

        if (userPhoto == null) {
            throw new ResourceNotFoundException(
                    EnumMessagesLangValues.CAR_PHOTO_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        String fileType = userPhoto.getFileType();
        log.info("file type ============================== {}", fileType);
        if (fileType == null || fileType.isBlank()) {
            fileType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        if (!fileType.contains("/")) {
            fileType = "image/" + fileType.toLowerCase();
        }

        MediaType mediaType = MediaType.parseMediaType(fileType);
        log.info("media type file type ================= {}", mediaType.getType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(userPhoto.getImageData());
    }

    @Override
    @Transactional
    public PhotoResponse uploadPartnerPhoto(MultipartFile file, Long partnerId) {
        if (file == null || partnerId == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(null));
        }

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Avto Servis tapilmadi"));

        try {
            checkAttack(file, null);

            PartnerPhoto existPhoto = partnerPhotoRepository.findByPartnerId(partner.getId());

            if (existPhoto != null) {
                partnerPhotoRepository.delete(existPhoto);
            }

            Tika tika = new Tika();
            String detectedType = tika.detect(file.getBytes());

            if (!detectedType.startsWith("image/")) {
                throw new InvalidStatusException(EnumMessagesLangValues.INVALID_PHOTO_FORMAT.getMessageByLang(null));
            }

            String fileType = detectedType.substring("image/".length());

            PartnerPhoto partnerPhoto = PartnerPhoto.builder()
                    .fileName("partner " + partner.getId() + " image")
                    .fileType(fileType)
                    .partnerId(partner.getId())
                    .imageData(file.getBytes())
                    .build();

            partnerPhotoRepository.save(partnerPhoto);

            return PhotoResponse.builder()
                    .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(null))
                    .build();
        } catch (IOException e) {
            throw new FileStorageException(EnumMessagesLangValues.FILE_CANT_SET.getMessageByLang(null));
        }
    }

    @Override
    public ResponseEntity<byte[]> getPartnerPhotoById(Long partnerId) {
        if (partnerId == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(null));
        }

        PartnerPhoto partnerPhoto = partnerPhotoRepository.findByPartnerId(partnerId);

        if (partnerPhoto == null) {
            throw new ResourceNotFoundException(
                    EnumMessagesLangValues.PHOTO_NOT_FOUND.getMessageByLang(null));
        }

        String fileType = partnerPhoto.getFileType();
        log.info("file type ============================== {}", fileType);

        if (fileType == null || fileType.isBlank()) {
            fileType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        if (!fileType.contains("/")) {
            fileType = "image/" + fileType.toLowerCase();
        }

        MediaType mediaType = MediaType.parseMediaType(fileType);
        log.info("media type file type ================= {}", mediaType.getType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(partnerPhoto.getImageData());
    }


    @Override
    @Transactional
    public PhotoResponse uploadCarPhoto(MultipartFile file, Long carId, String role, String phoneNumber, String userIdHeader,
                                        String timezone, String acceptLanguage) {
        if (file == null || role == null || phoneNumber == null || userIdHeader == null || acceptLanguage == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        if (!role.equals(EnumUserRoles.USER.name())) {
            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader),
                phoneNumber, EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByCarIdAndCustomer(carId, customer);

        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        try {
            checkAttack(file, acceptLanguage);

            CarPhoto existPhoto = carPhotoRepository.findByCarId(carId);

            if (existPhoto != null) {
                carPhotoRepository.delete(existPhoto);
            }

            Tika tika = new Tika();
            String detectedType = tika.detect(file.getBytes());

            if (!detectedType.startsWith("image/")) {
                throw new InvalidStatusException(EnumMessagesLangValues.INVALID_PHOTO_FORMAT
                        .getMessageByLang(acceptLanguage));
            }

            String fileType = detectedType.substring("image/".length());


//            byte[] processedImageBytes = CustomImageCrop.resizeAndCropImage(file.getBytes(), fileType);

            CarPhoto carPhoto = CarPhoto.builder()
                    .fileName("car " + carId + " image")
                    .fileType(fileType)
                    .carId(carId)
                    .imageData(file.getBytes())
                    .build();

            carPhotoRepository.save(carPhoto);

            return PhotoResponse.builder()
                    .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                    .build();
        } catch (IOException e) {
            throw new FileStorageException(EnumMessagesLangValues.FILE_CANT_SET.getMessageByLang(acceptLanguage));
        }
    }

    @Override
    public PhotoResponse deleteCarPhoto(String role, Long carId, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {

        if (role == null || phoneNumber == null || userIdHeader == null || acceptLanguage == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }
        if (!role.equals(EnumUserRoles.USER.name())) {
            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader),
                phoneNumber, EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        Car car = carRepository.findByCarIdAndCustomer(carId, customer);

        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        CarPhoto carPhoto = carPhotoRepository.findByCarId(carId);

        if (carPhoto == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.PHOTO_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        carPhotoRepository.delete(carPhoto);

        return PhotoResponse.builder()
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }

    @Override
    public PhotoResponse uploadUserPP(MultipartFile file, String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {
        if (file == null || role == null || phoneNumber == null || userIdHeader == null || acceptLanguage == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }
        try {
            checkAttack(file, acceptLanguage);

            UserPhoto userPhoto = userPhotoRepository.findByUserIdAndUserPhoneNumber(Long.valueOf(userIdHeader), phoneNumber);
            if (userPhoto != null) {
                userPhotoRepository.delete(userPhoto);
            }
            Tika tika = new Tika();
            String detectedType = tika.detect(file.getBytes());

            if (!detectedType.startsWith("image/")) {
                throw new InvalidStatusException(EnumMessagesLangValues.INVALID_PHOTO_FORMAT
                        .getMessageByLang(acceptLanguage));
            }

            String fileType = detectedType.substring("image/".length());


//            byte[] processedImageBytes = CustomImageCrop.resizeAndCropImage(file.getBytes(), fileType);

            UserPhoto newPhoto = UserPhoto.builder()
                    .fileName("user " + userIdHeader + " image")
                    .fileType(fileType)
                    .imageData(file.getBytes())
                    .userId(Long.valueOf(userIdHeader))
                    .userPhoneNumber(phoneNumber)
                    .build();

            userPhotoRepository.save(newPhoto);

            return PhotoResponse.builder()
                    .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                    .build();

        } catch (IOException e) {
            throw new FileStorageException(EnumMessagesLangValues.FILE_CANT_SET.getMessageByLang(acceptLanguage));
        }
    }

    @Override
    public PhotoResponse deleteUserPP(String role, String phoneNumber, String userIdHeader, String timezone,
                                      String acceptLanguage) {
        if (role == null || phoneNumber == null || userIdHeader == null || acceptLanguage == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        UserPhoto userPhoto = userPhotoRepository.findByUserIdAndUserPhoneNumber(Long.valueOf(userIdHeader), phoneNumber);

        if (userPhoto == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.PHOTO_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        userPhotoRepository.delete(userPhoto);

        return PhotoResponse.builder()
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }


    public void checkAttack(MultipartFile file, String acceptLanguage) {
        if (file.getOriginalFilename().contains("..")) {
            throw new MissingFieldException(EnumMessagesLangValues.INVALID_PHOTO_NAME.getMessageByLang(acceptLanguage));

        }
    }
}
