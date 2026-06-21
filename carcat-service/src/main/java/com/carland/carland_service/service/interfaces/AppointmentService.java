package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.response.AppointmentResponse;

import java.util.List;

public interface AppointmentService {
    List<AppointmentResponse> getBookingListByDate(String date, String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    AppointmentResponse setAppointmentFromReception(String role, String phoneNumber, Long rangeId, String userIdHeader, String timezone, String acceptLanguage);




}
