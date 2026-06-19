package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.response.AppointmentResponse;
import com.carland.carland_service.service.interfaces.AppointmentService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AppointmentServiceImpl implements AppointmentService {
    @Override
    public List<AppointmentResponse> getBookingListByDate(String date, String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {
        return null;
    }

    @Override
    public AppointmentResponse setAppointmentFromReception(String role, String phoneNumber, Long rangeId, String userIdHeader, String timezone, String acceptLanguage) {
        return null;
    }
}
