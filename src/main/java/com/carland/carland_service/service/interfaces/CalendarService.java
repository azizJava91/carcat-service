package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.request.CalendarRequest;
import com.carland.carland_service.dto.response.CalendarResponse;

public interface CalendarService {
    CalendarResponse createCalendar(CalendarRequest request, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    CalendarResponse getCalendarByAutoServiceId(CalendarRequest request, String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);

}
