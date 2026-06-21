package com.carland.carland_service.controller;

import com.carland.carland_service.dto.response.AppointmentResponse;
import com.carland.carland_service.service.interfaces.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointment")
@RequiredArgsConstructor

public class AppointmentController {
    private final AppointmentService appointmentService;

    @GetMapping("/list")
    public List<AppointmentResponse> getBookingListByDate(@RequestParam String date,
                                                          @RequestHeader("role") String role,
                                                          @RequestHeader("phoneNumber") String phoneNumber,
                                                          @RequestHeader("X-User-Id") String userIdHeader,
                                                          @RequestHeader("X-Client-Timezone") String timezone,
                                                          @RequestHeader("Accept-Language") String acceptLanguage) {

        return appointmentService.getBookingListByDate(date, role, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }

    @PostMapping("/reception")
    public AppointmentResponse setAppointmentFromReception(@RequestParam Long rangeId,
                                                           @RequestHeader("role") String role,
                                                           @RequestHeader("phoneNumber") String phoneNumber,
                                                           @RequestHeader("X-User-Id") String userIdHeader,
                                                           @RequestHeader("X-Client-Timezone") String timezone,
                                                           @RequestHeader("Accept-Language") String acceptLanguage) {

        return appointmentService.setAppointmentFromReception(role, phoneNumber, rangeId, userIdHeader, timezone, acceptLanguage);
    }

//    @GetMapping("/get/byId")
//    public ReceptionAppointmentResponse getAppointmentById(@RequestParam Long appointmentId,
//                                                           @RequestHeader("role") String role,
//                                                           @RequestHeader("phoneNumber") String phoneNumber,
//                                                           @RequestHeader("X-User-Id") String userIdHeader,
//                                                           @RequestHeader("X-Client-Timezone") String timezone,
//                                                           @RequestHeader("Accept-Language") String acceptLanguage) {
//        return appointmentService.getAppointmentById(appointmentId, role, phoneNumber, userIdHeader, timezone, acceptLanguage);
//    }

}
