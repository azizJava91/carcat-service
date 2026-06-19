package com.carland.carland_service.controller;

import com.carland.carland_service.dto.response.RangeResponse;
import com.carland.carland_service.service.interfaces.RangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/range")
@RequiredArgsConstructor
public class RangeController {

    private final RangeService rangeService;

    @PostMapping("/book")
    public RangeResponse bookAppointment(@RequestParam Long rangeId,
                                         @RequestHeader("role") String role,
                                         @RequestHeader("phoneNumber") String phoneNumber,
                                         @RequestHeader("X-User-Id") String userIdHeader,
                                         @RequestHeader("X-Client-Timezone") String timezone,
                                         @RequestHeader("Accept-Language") String acceptLanguage) {
        return rangeService.bookAppointment(rangeId, role, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }


    @PostMapping("/booking/decision")
    public RangeResponse decideOnBooking(@RequestParam Long rangeId,
                                         @RequestParam boolean accepted,
                                         @RequestHeader("role") String role,
                                         @RequestHeader("phoneNumber") String phoneNumber,
                                         @RequestHeader("X-User-Id") String userIdHeader,
                                         @RequestHeader("X-Client-Timezone") String timezone,
                                         @RequestHeader("Accept-Language") String acceptLanguage) {
        return rangeService.decideOnBooking(rangeId, accepted, role, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }

    @PutMapping("/delete")
    public RangeResponse deleteBookingByCustomer(@RequestParam Long rangeId,
                                                @RequestHeader("role") String role,
                                                @RequestHeader("phoneNumber") String phoneNumber,
                                                @RequestHeader("X-User-Id") String userIdHeader,
                                                @RequestHeader("X-Client-Timezone") String timezone,
                                                @RequestHeader("Accept-Language") String acceptLanguage) {
        return rangeService.deleteBookingByCustomer(rangeId, role, phoneNumber, userIdHeader, timezone, acceptLanguage);
    }


}
