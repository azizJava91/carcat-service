package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.request.CalendarRequest;
import com.carland.carland_service.dto.response.CalendarResponse;
import com.carland.carland_service.dto.response.RangeResponse;
import com.carland.carland_service.entity.Admin;
import com.carland.carland_service.entity.AutoService;
import com.carland.carland_service.entity.Calendar;
import com.carland.carland_service.entity.Range;
import com.carland.carland_service.enums.*;
import com.carland.carland_service.exceptions.*;
import com.carland.carland_service.repository.AdminRepository;
import com.carland.carland_service.repository.AutoServiceRepository;
import com.carland.carland_service.repository.CalendarRepository;
import com.carland.carland_service.service.interfaces.CalendarService;
import com.carland.carland_service.util.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {
    private final AdminRepository adminRepository;
    private final CalendarRepository calendarRepository;
    private final Helper helper;
    private final AutoServiceRepository autoServiceRepository;

    @Override
    @Transactional
    public CalendarResponse createCalendar(CalendarRequest calendarRequest, String phoneNumber,
                                           String userIdHeader, String timezone, String acceptLanguage) {

        if (phoneNumber == null || userIdHeader == null || calendarRequest.getDay() == null ||
                calendarRequest.getStart() == null || calendarRequest.getEnd() == null ||
                calendarRequest.getRangeMinutes() == null || calendarRequest.getServiceCategory() == null ||
                calendarRequest.getWorkerCount() == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Admin admin = adminRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber,
                EnumUserStatus.ACTIVE.name());

        if (admin == null) {
            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
        }

        if (calendarRequest.getRangeMinutes() <= 0) {
            throw new MissingFieldException(EnumMessagesLangValues.INVALID_RANGE_MINUTES.getMessageByLang(acceptLanguage));
        }

        LocalDate todayLocal = LocalDate.now(ZoneId.of(timezone));
        LocalTime nowLocal = LocalTime.now(ZoneId.of(timezone));

        if (calendarRequest.getDay().isBefore(todayLocal)) {
            throw new InvalidStatusException(EnumMessagesLangValues.PAST_DATE_NOT_ALLOWED.getMessageByLang(acceptLanguage));
        }
        if (calendarRequest.getDay().isEqual(todayLocal) && calendarRequest.getStart().isBefore(nowLocal)) {
            throw new InvalidStatusException(EnumMessagesLangValues.START_TIME_ALREADY_PASSED.getMessageByLang(acceptLanguage));
        }

        if (!calendarRequest.getStart().isBefore(calendarRequest.getEnd())) {
            throw new MissingFieldException(EnumMessagesLangValues.START_AFTER_END.getMessageByLang(acceptLanguage));
        }

        AutoService autoService = admin.getAutoService();
        if (autoService == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.AUTO_SERVICE_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        OffsetDateTime startUtc = helper.getUtcTimeFromDayAndTimeAndTimeZone(calendarRequest.getDay(),
                calendarRequest.getStart(), timezone);
        OffsetDateTime endUtc = helper.getUtcTimeFromDayAndTimeAndTimeZone(
                calendarRequest.getDay(), calendarRequest.getEnd(), timezone);

        LocalDate utcDay = helper.getUtcDayFromUtcTime(startUtc);

        Calendar existingCalendar = calendarRepository.findByDayAndServiceCategoryAndAutoService(utcDay,
                calendarRequest.getServiceCategory(), autoService);
        if (existingCalendar != null) {
            throw new AlreadyExistsException(EnumMessagesLangValues.CALENDAR_ALREADY_EXISTS.getMessageByLang(acceptLanguage));
        }

        List<Range> rangeList = createRangeList(
                calendarRequest.getDay(),
                calendarRequest.getStart(),
                calendarRequest.getEnd(),
                calendarRequest.getRangeMinutes(),
                timezone,
                calendarRequest.getWorkerCount()
        );

        Calendar calendar = Calendar.builder()
                .day(utcDay)
                .start(startUtc)
                .end(endUtc)
                .autoService(autoService)
                .timeRanges(rangeList)
                .rangeMinutes(calendarRequest.getRangeMinutes())
                .status(EnumCalendarStatus.ACTIVE.name())
                .serviceCategory(calendarRequest.getServiceCategory())
                .build();

        rangeList.forEach(range -> range.setCalendar(calendar));

        calendarRepository.save(calendar);

        List<RangeResponse> rangeResponseList = mapToRangeResponseList(rangeList, timezone, acceptLanguage);

        return CalendarResponse.builder()
                .timeRanges(rangeResponseList)
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }

    @Override
    public CalendarResponse getCalendarByAutoServiceId(CalendarRequest request, String role, String phoneNumber,
                                                       String userIdHeader, String timezone, String acceptLanguage) {

        if (request == null || request.getDay() == null || request.getServiceCategory() == null || role == null ||
                phoneNumber == null || userIdHeader == null || request.getAutoServiceId() == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        AutoService autoService = autoServiceRepository.findById(request.getAutoServiceId()).orElseThrow(() -> new
                ResourceNotFoundException(EnumMessagesLangValues.AUTO_SERVICE_NOT_FOUND.getMessageByLang(acceptLanguage)));

//        Admin admin = adminRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber,
//                EnumUserStatus.ACTIVE.name());
//
//        if (admin == null) {
//            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
//        }
//        if (role.equals(EnumUserRoles.ADMIN.name()) && !autoService.getAdmins().contains(admin)) {
//            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
//        }

        Calendar calendar = calendarRepository.findByDayAndServiceCategoryAndAutoService(request.getDay(),
                request.getServiceCategory(), autoService);

        if (calendar == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CALENDAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        List<Range> ranges = calendar.getTimeRanges();

        return CalendarResponse.builder()
                .timeRanges(mapToRangeResponseList(ranges, timezone, acceptLanguage))
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }


    private List<RangeResponse> mapToRangeResponseList(List<Range> rangeList, String timezone, String acceptLanguage) {
        return mapToRangeResponseList(rangeList, timezone, acceptLanguage, null);
    }

    private List<RangeResponse> mapToRangeResponseList(List<Range> rangeList, String timezone, String acceptLanguage, @Nullable OffsetDateTime cutoffUtc) {
        return rangeList.stream()
                .sorted(Comparator.comparing(Range::getStart))
                .filter(range -> cutoffUtc == null || range.getStart().isAfter(cutoffUtc))
                .map(range -> RangeResponse.builder()
                        .rangeId(range.getRangeId())
                        .start(helper.getLocalTimeFromUtcUseTZ(range.getStart(), timezone))
                        .end(helper.getLocalTimeFromUtcUseTZ(range.getEnd(), timezone))
                        .status(range.getStatus())
                        .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                        .freeCount(range.getWorkerCount() - range.getAppointments().size())
                        .build())
                .toList();
    }


    private List<Range> createRangeList(LocalDate day, LocalTime start, LocalTime end, Integer rangeMinutes, String timezone, Integer workerCount) {
        List<Range> ranges = new ArrayList<>();

        OffsetDateTime currentStartUtc = helper.getUtcTimeFromDayAndTimeAndTimeZone(day, start, timezone);
        OffsetDateTime endUtc = helper.getUtcTimeFromDayAndTimeAndTimeZone(day, end, timezone);

        while (currentStartUtc.isBefore(endUtc)) {
            OffsetDateTime currentEndUtc = currentStartUtc.plusMinutes(rangeMinutes);
            if (currentEndUtc.isAfter(endUtc)) {
                currentEndUtc = endUtc;
            }

            ranges.add(Range.builder()
                    .start(currentStartUtc)
                    .end(currentEndUtc)
                    .workerCount(workerCount)
                    .status(EnumRangeStatus.AVAILABLE.name())
                    .build()
            );

            if (currentEndUtc.equals(endUtc)) {
                break;
            }

            currentStartUtc = currentEndUtc;
        }

        return ranges;
    }

}
