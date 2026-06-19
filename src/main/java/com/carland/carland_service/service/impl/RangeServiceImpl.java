package com.carland.carland_service.service.impl;


import com.carland.carland_service.dto.response.AppointmentResponse;
import com.carland.carland_service.entity.Appointment;
import com.carland.carland_service.entity.AutoService;
import com.carland.carland_service.entity.Customer;
import com.carland.carland_service.entity.Range;
import com.carland.carland_service.enums.EnumAppointmentStatus;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.enums.EnumRangeStatus;
import com.carland.carland_service.enums.EnumUserRoles;
import com.carland.carland_service.exceptions.*;
import com.carland.carland_service.repository.AppointmentRepository;
import com.carland.carland_service.repository.AutoServiceRepository;
import com.carland.carland_service.repository.CustomerRepository;
import com.carland.carland_service.repository.RangeRepository;
import com.carland.carland_service.service.interfaces.PushNotificationService;
import com.carland.carland_service.util.Helper;
import com.carland.carland_service.dto.response.RangeResponse;
import com.carland.carland_service.service.interfaces.RangeService;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RangeServiceImpl implements RangeService {

    private final RangeRepository rangeRepository;
    private final CustomerRepository customerRepository;
    private final AutoServiceRepository autoServiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final Helper helper;
    private final PushNotificationService pushNotificationService;


//    @Value("${appointment.last.delete.time}")
//    private Long lastDeleteHours;

    @Override
    @Transactional
    public RangeResponse bookAppointment(Long rangeId, String role, String phoneNumber,
                                         String userIdHeader, String timezone, String acceptLanguage) {

        if (rangeId == null || role == null || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }
        if (!role.equals(EnumUserRoles.USER.name())) {
            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumber(Long.valueOf(userIdHeader), phoneNumber);
        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Range range = rangeRepository.findByRangeId(rangeId);
        if (range == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.RANGE_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        if (range.getStatus().equals(EnumRangeStatus.PENDING.name()) ||
                range.getAppointments().size() >= range.getWorkerCount()) {
            throw new AlreadyExistsException(EnumMessagesLangValues.ALREADY_BOOKED.getMessageByLang(acceptLanguage));
        }
        if (range.getStatus().equals(EnumRangeStatus.BREAK.name())) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.BREAK_TIME.getMessageByLang(acceptLanguage));
        }

        AutoService autoService = autoServiceRepository.findById(range.getCalendar().getAutoService().getId())
                .orElseThrow(() -> new UserNotFoundException(EnumMessagesLangValues.DOCTOR_NOT_FOUND.getMessageByLang(acceptLanguage)));

        OffsetDateTime dayStart = range.getStart().toLocalDate().atStartOfDay().atOffset(range.getStart().getOffset());
        OffsetDateTime dayEnd = range.getStart().toLocalDate().atTime(23, 59, 59).atOffset(range.getStart().getOffset());

        Optional<Appointment> existing = appointmentRepository
                .findByCustomer_UserIdAndServiceCategoryAndAppointmentDateBetweenAndRange_Calendar_AutoService_Id(
                        customer.getUserId(),
                        range.getCalendar().getServiceCategory(),
                        dayStart,
                        dayEnd,
                        range.getCalendar().getAutoService().getId()
                );


        if(existing.isPresent()){
            throw new AlreadyExistsException(EnumMessagesLangValues.ALREADY_BOOKED_SAME_DAY.getMessageByLang(acceptLanguage));
        }


        OffsetDateTime appointmentDateUtc = range.getStart();

        Appointment appointment = Appointment.builder()
                .appointmentDate(appointmentDateUtc)
                .appointmentStart(range.getStart())
                .appointmentEnd(range.getEnd())
                .autoService(autoService)
                .range(range)
                .serviceCategory(range.getCalendar().getServiceCategory())
                .customer(customer)
                .status(EnumAppointmentStatus.PENDING.name())
                .build();
        appointmentRepository.save(appointment);

        range.getAppointments().add(appointment);

        if (range.getAppointments().size() >= range.getWorkerCount()) {
            range.setStatus(EnumRangeStatus.FULL.name());
        }


        rangeRepository.save(range);

        return RangeResponse.builder()
                .rangeId(range.getRangeId())
                .start(helper.getLocalTimeFromUtcUseTZ(range.getStart(), timezone))
                .end(helper.getLocalTimeFromUtcUseTZ(range.getEnd(), timezone))
                .status(range.getStatus())
                .appointmentResponses(List.of(convertToResponse(appointment, timezone, acceptLanguage)))
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .freeCount(range.getWorkerCount() - range.getAppointments().size())
                .build();
    }

    @Override
    public RangeResponse decideOnBooking(Long rangeId, boolean accepted, String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {
        return null;
    }


//    @Override
//    @Transactional
//    public RangeResponse decideOnBooking(Long rangeId, boolean accepted, String role, String phoneNumber,
//                                         String userIdHeader, String timezone, String acceptLanguage) {
//
//        if (role == null || !role.equals(EnumUserRoles.DOCTOR.name())) {
//            throw new InvalidStatusException(EnumMessagesLangValues.ONLY_DOCTOR_ALLOWED.getMessageByLang(acceptLanguage));
//        }
//        if (rangeId == null || phoneNumber == null || userIdHeader == null) {
//            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
//        }
//
//        Doctor doctor = doctorRepository.findByUserIdAndPhoneNumberAndStatus(
//                Long.valueOf(userIdHeader), phoneNumber, EnumUserStatus.ACTIVE.name());
//        if (doctor == null) {
//            throw new UserNotFoundException(EnumMessagesLangValues.DOCTOR_NOT_FOUND.getMessageByLang(acceptLanguage));
//        }
//        Range range = rangeRepository.findByRangeId(rangeId);
//        if (range == null) {
//            throw new ResourceNotFoundException(EnumMessagesLangValues.APPOINTMENT_NOT_FOUND.getMessageByLang(acceptLanguage));
//        }
//        Patient patient = range.getAppointment().getPatient();
//
//        if (!range.getStatus().equals(EnumRangeStatus.PENDING.name())) {
//            throw new InvalidStatusException(EnumMessagesLangValues.APPOINTMENT_STATUS_ALREADY_SET.getMessageByLang(acceptLanguage));
//        }
//
//        if (!range.getCalendar().getDoctor().getUserId().equals(doctor.getUserId())) {
//            throw new InvalidStatusException(EnumMessagesLangValues.APPOINTMENT_NOT_FOR_DOCTOR.getMessageByLang(acceptLanguage));
//        }
//
//        OffsetDateTime rangeStartUtc = range.getStart();
//        if (rangeStartUtc.isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
//            throw new InvalidStatusException(EnumMessagesLangValues.APPOINTMENT_DATE_PASSED.getMessageByLang(acceptLanguage));
//        }
//
//        if (!range.isBooked()) {
//            throw new InvalidStatusException(EnumMessagesLangValues.APPOINTMENT_NOT_BOOKED.getMessageByLang(acceptLanguage));
//        }
//
//        Appointment appointment = range.getAppointment();
//        if (appointment == null) {
//            throw new ResourceNotFoundException(EnumMessagesLangValues.APPOINTMENT_DATA_NOT_FOUND.getMessageByLang(acceptLanguage));
//        }
//
//        OffsetDateTime decisionTimeUtc = OffsetDateTime.now(ZoneOffset.UTC);
//
//        if (accepted) {
//            range.setStatus(EnumRangeStatus.ACCEPTED.name());
//            appointment.setAppointmentDate(decisionTimeUtc);
//            appointment.setStatus(EnumRangeStatus.ACCEPTED.name());
//            pushNotificationService.sendBookingUpdateNotificationToPatientByDoctor(range, acceptLanguage, timezone, true, patient);
//
//        } else {
//            pushNotificationService.sendBookingUpdateNotificationToPatientByDoctor(range, acceptLanguage, timezone, false, patient);
//
//            appointment.setAppointmentDate(decisionTimeUtc);
//            appointment.setStatus(EnumRangeStatus.REJECTED.name());
//            appointment.setRange(null);
//
//            range.setStatus(EnumRangeStatus.AVAILABLE.name());
//            range.setBooked(false);
//            range.setAppointment(null);
//        }
//
//        rangeRepository.save(range);
//        appointmentRepository.save(appointment);
//
//
//        return RangeResponse.builder()
//                .rangeId(rangeId)
//                .booked(range.isBooked())
//                .start(helper.getLocalTimeFromUtcUseTZ(range.getStart(), timezone))
//                .end(helper.getLocalTimeFromUtcUseTZ(range.getEnd(), timezone))
//                .status(range.getStatus())
//                .message(accepted
//                        ? EnumMessagesLangValues.APPOINTMENT_ACCEPTED.getMessageByLang(acceptLanguage)
//                        : EnumMessagesLangValues.APPOINTMENT_REJECTED.getMessageByLang(acceptLanguage))
//                .build();
//    }

    @Override
    public RangeResponse deleteBookingByCustomer(Long rangeId, String role, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {
        return null;
    }


//    @Override
//    @Transactional
//    public RangeResponse deleteBookingByPatient(Long rangeId, String role, String phoneNumber,
//                                                String userIdHeader, String timezone, String acceptLanguage) {
//
//        if (role == null || !role.equals(EnumUserRoles.USER.name())) {
//            throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
//        }
//        if (rangeId == null || phoneNumber == null || userIdHeader == null) {
//            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
//        }
//
//        Patient patient = patientRepository.findByUserIdAndPhoneNumber(Long.valueOf(userIdHeader), phoneNumber);
//        if (patient == null) {
//            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
//        }
//        if (!patient.getStatus().equals(EnumUserStatus.ACTIVE.name())) {
//            throw new InvalidStatusException(EnumMessagesLangValues.USER_NOT_ACTIVE.getMessageByLang(acceptLanguage));
//        }
//
//        Appointment appointment = appointmentRepository.findByPatientAndRange_RangeId(patient, rangeId);
//        if (appointment == null) {
//            throw new ResourceNotFoundException(EnumMessagesLangValues.APPOINTMENT_NOT_FOUND.getMessageByLang(acceptLanguage));
//        }
//        if (!(appointment.getStatus().equals(EnumAppointmentStatus.PENDING.name())
//                || appointment.getStatus().equals(EnumAppointmentStatus.ACCEPTED.name()))) {
//            throw new InvalidStatusException(EnumMessagesLangValues.APPOINTMENT_STATUS_ALREADY_SET.getMessageByLang(acceptLanguage));
//        }
//
//        Range range = rangeRepository.findByAppointment(appointment);
//        if (range == null) {
//            throw new ResourceNotFoundException(EnumMessagesLangValues.APPOINTMENT_NOT_FOUND.getMessageByLang(acceptLanguage));
//        }
//
//        ZonedDateTime nowLocal = ZonedDateTime.now(ZoneId.of(timezone));
//        ZonedDateTime rangeStartLocal = helper.toZonedDateTime(range.getStart(), timezone);
//
//        if (rangeStartLocal.isBefore(nowLocal)) {
//            throw new InvalidStatusException(EnumMessagesLangValues.PAST_DATE_NOT_ALLOWED.getMessageByLang(acceptLanguage));
//        }
//
//        if (rangeStartLocal.isBefore(nowLocal.plusHours(lastDeleteHours))) {
//            throw new InvalidStatusException(EnumMessagesLangValues.DELETE_TIME_EXPIRED.getMessageByLang(acceptLanguage));
//        }
//
//        pushNotificationService.sendBookingCancellationNotificationToDoctorByPatient(range, acceptLanguage, timezone);
//
//        appointment.setStatus(EnumAppointmentStatus.DELETED_BY_PATIENT.name());
//        appointment.setRange(null);
//
//        range.setStatus(EnumRangeStatus.AVAILABLE.name());
//        range.setBooked(false);
//        range.setAppointment(null);
//
//        rangeRepository.save(range);
//        appointmentRepository.save(appointment);
//
//
//        return RangeResponse.builder()
//                .rangeId(rangeId)
//                .booked(range.isBooked())
//                .start(helper.getLocalTimeFromUtcUseTZ(range.getStart(), timezone))
//                .end(helper.getLocalTimeFromUtcUseTZ(range.getEnd(), timezone))
//                .status(range.getStatus())
//                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
//                .build();
//
//    }

    public AppointmentResponse convertToResponse(Appointment appointment, String timezone, String acceptLanguage) {
        if (appointment == null) return null;

        OffsetDateTime appointmentDateLocal = helper.getLocalDateTimeFromUtcUseTZ(appointment.getAppointmentDate(), timezone);
        OffsetDateTime appointmentStartLocal = helper.getLocalDateTimeFromUtcUseTZ(appointment.getAppointmentStart(), timezone);
        OffsetDateTime appointmentEndLocal = helper.getLocalDateTimeFromUtcUseTZ(appointment.getAppointmentEnd(), timezone);

        String appointmentDateString = helper.formatAppointmentDate(appointmentDateLocal, acceptLanguage);
        String appointmentStartString = helper.formatAppointmentDate(appointmentStartLocal, acceptLanguage);
        String appointmentEndString = helper.formatAppointmentDate(appointmentEndLocal, acceptLanguage);

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .appointmentDate(appointmentDateString)
                .appointmentStart(appointmentStartString)
                .appointmentEnd(appointmentEndString)
                .status(appointment.getStatus())
                .serviceCategory(appointment.getServiceCategory())
                .autoServiceId(appointment.getAutoService() != null ? appointment.getAutoService().getId() : null)
                .autoServiceName(appointment.getAutoService() != null ? appointment.getAutoService().getName() : null)
                .autoServiceNumber(appointment.getAutoService() != null ? appointment.getAutoService().getPhoneNumber() : null)
                .serviceCategory(appointment.getServiceCategory())
                .customerNumber(appointment.getCustomer() != null ? appointment.getCustomer().getPhoneNumber() : null)
                .customerName(appointment.getCustomer() != null ? appointment.getCustomer().getName() + " " + appointment.getCustomer().getSurname() : null)
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }


}
