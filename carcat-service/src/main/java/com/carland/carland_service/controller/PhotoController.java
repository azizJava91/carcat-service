package com.carland.carland_service.controller;

import com.carland.carland_service.dto.response.PhotoResponse;
import com.carland.carland_service.service.interfaces.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/photo")
@RequiredArgsConstructor

public class PhotoController {

    private final PhotoService photoService;

    @PostMapping(value = "/for/car/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PhotoResponse uploadCarPhoto(@RequestPart("file") MultipartFile file,
                                        @RequestParam("carId") Long carId,
                                        @RequestHeader("role") String role,
                                        @RequestHeader("phoneNumber") String phoneNumber,
                                        @RequestHeader("X-User-Id") String userIdHeader,
                                        @RequestHeader("X-Client-Timezone") String timezone,
                                        @RequestHeader("Accept-Language") String acceptLanguage) {
        return photoService.uploadCarPhoto(file, carId, role, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }

    @DeleteMapping("/for/car/delete")
    public PhotoResponse deleteCarPhoto(@RequestHeader("role") String role,
                                        @RequestParam("carId") Long carId,
                                        @RequestHeader("phoneNumber") String phoneNumber,
                                        @RequestHeader("X-User-Id") String userIdHeader,
                                        @RequestHeader("X-Client-Timezone") String timezone,
                                        @RequestHeader("Accept-Language") String acceptLanguage) {
        return photoService.deleteCarPhoto(role, carId, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }

    @GetMapping(value = "/for/car/get", produces = MediaType.ALL_VALUE)
    public ResponseEntity<byte[]> getCarPhoto(
            @RequestHeader("role") String role,
            @RequestParam("carId") Long carId,
            @RequestHeader("phoneNumber") String phoneNumber,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader("X-Client-Timezone") String timezone,
            @RequestHeader("Accept-Language") String acceptLanguage) {

        return photoService.getCarPhoto(role, carId, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }


    @PostMapping(value = "/for/user/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public PhotoResponse uploadUserPP(@RequestPart("file") MultipartFile file,
                                      @RequestHeader("role") String role,
                                      @RequestHeader("phoneNumber") String phoneNumber,
                                      @RequestHeader("X-User-Id") String userIdHeader,
                                      @RequestHeader("X-Client-Timezone") String timezone,
                                      @RequestHeader("Accept-Language") String acceptLanguage) {
        return photoService.uploadUserPP(file, role, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }

    @DeleteMapping("/for/user/delete")
    public PhotoResponse deletePP(@RequestHeader("role") String role,
                                  @RequestHeader("phoneNumber") String phoneNumber,
                                  @RequestHeader("X-User-Id") String userIdHeader,
                                  @RequestHeader("X-Client-Timezone") String timezone,
                                  @RequestHeader("Accept-Language") String acceptLanguage) {
        return photoService.deleteUserPP(role, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }

    @GetMapping(value = "/for/user/get", produces = MediaType.ALL_VALUE)
    public ResponseEntity<byte[]> getProfilePicture(
            @RequestHeader("role") String role,
            @RequestHeader("phoneNumber") String phoneNumber,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader("X-Client-Timezone") String timezone,
            @RequestHeader("Accept-Language") String acceptLanguage) {

        return photoService.getUserPP(role, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }

    @GetMapping(value = "/for/partner/get/{partnerId}", produces = MediaType.ALL_VALUE)
    public ResponseEntity<byte[]> getPartnerPhotoById(@PathVariable("partnerId") Long partnerId) {
        return photoService.getPartnerPhotoById(partnerId);
    }


}
