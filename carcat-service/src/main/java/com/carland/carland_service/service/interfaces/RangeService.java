package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.response.RangeResponse;

public interface RangeService {
    RangeResponse bookAppointment(Long rangeId,  String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    RangeResponse decideOnBooking(Long rangeId, boolean accepted, String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    RangeResponse deleteBookingByCustomer(Long rangeId, String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);




}
