package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.response.PhotoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoService {

    PhotoResponse uploadCarPhoto(MultipartFile file, Long carId, String role, String phoneNumber, String userIdHeader,
                                 String timezone, String acceptLanguage);

    PhotoResponse deleteCarPhoto(String role, Long carId, String phoneNumber, String userIdHeader, String timezone,
                                 String acceptLanguage);


    PhotoResponse uploadUserPP(MultipartFile file, String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);

    PhotoResponse deleteUserPP(String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    ResponseEntity<byte[]> getCarPhoto(String role, Long carId, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    ResponseEntity<byte[]> getUserPP(String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    PhotoResponse uploadPartnerPhoto(MultipartFile file, Long partnerId);

    ResponseEntity<byte[]> getPartnerPhotoById(Long partnerId);
}
