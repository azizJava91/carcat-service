package com.carland.carland_service.service.impl;

import com.carland.carland_service.dto.request.CarRequest;
import com.carland.carland_service.dto.request.PercentageRequest;
import com.carland.carland_service.dto.request.RecordRequest;
import com.carland.carland_service.dto.response.*;
import com.carland.carland_service.entity.*;
import com.carland.carland_service.enums.ColorTranslation;
import com.carland.carland_service.enums.EngineTypeTranslation;
import com.carland.carland_service.enums.EnumMessagesLangValues;
import com.carland.carland_service.enums.EnumUserStatus;
import com.carland.carland_service.exceptions.*;
import com.carland.carland_service.repository.*;
import com.carland.carland_service.service.interfaces.CarService;
import com.carland.carland_service.service.interfaces.PushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
@Slf4j
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final MaintenanceTemplateRepository maintenanceTemplateRepository;
    private final ServiceHistoryRepository serviceHistoryRepository;
    private final AdminRepository adminRepository;
    private final CustomerServiceRecordRepository customerServiceRecordRepository;
    private final ServiceEntityRepository serviceEntityRepository;
    private final VinService vinService;
    private final ColorRepository colorRepository;
    private final PercentageRepository percentageRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final PushNotificationService pushNotificationService;
    private final LogRepository logRepository;
    private final EngineTypeRepository engineTypeRepository;
//    private static final List<String> simulatedVins = List.of(
//            "JTJGB7CX2R4121777",
//            "LFMAAA0C6S0640604",
//            "19XZE4F54NE012640",
//            "5TDKBRCH8RS143667"
//    );

    @Override
    public CarResponse checkVin(String vin, String acceptLanguage) {


        // 2️⃣ Normal DB kontrolu
        Car carFromDb = carRepository.findByVin(vin);

        if (carFromDb != null && carFromDb.getCustomer() != null) {
            log.info("car ucun car !=null ve car.get customer != null controlu yandi");
            throw new AlreadyExistsException(EnumMessagesLangValues.CAR_ALREADY_EXISTS.getMessageByLang(acceptLanguage));
        } else if (carFromDb != null && carFromDb.getCustomer() == null) {

            List<String> vinProvidedFields = carFromDb != null ? carFromDb.getVinProvidedFields() : null;


            return CarResponse.builder()
                    .vin(carFromDb != null ? carFromDb.getVin() : null)
                    .brand(carFromDb != null ? carFromDb.getBrand() : null)
                    .model(carFromDb != null ? carFromDb.getModel() : null)
                    .modelYear(carFromDb != null ? carFromDb.getModelYear() : null)
                    .bodyType(carFromDb != null ? carFromDb.getBodyType() : null)
                    .transmissionType(carFromDb != null ? carFromDb.getTransmissionType() : null)
                    .engineVolume(carFromDb != null ? carFromDb.getEngineVolume() : null)
                    .engineType(carFromDb != null ? carFromDb.getEngineType() : null)
                    .vinProvidedFields(vinProvidedFields)
                    .resource("fromDb")
                    .plateNumber(carFromDb != null ? carFromDb.getPlateNumber() : null)
                    .engineTypeId(carFromDb.getEngineTypeId())
                    .mileage(carFromDb != null ? carFromDb.getMileage() : null)
                    .build();
        } else {

            // 3️⃣ NHTSA decode flow
            Map<String, String> decodeVin = vinService.extractFieldsFromVin(vin);

            List<String> vinProvidedFields = new ArrayList<>();

            String brand = decodeVin.get("brand");
            if (hasValue(brand)) vinProvidedFields.add("brand");

            String model = decodeVin.get("model");
            if (hasValue(model)) vinProvidedFields.add("model");

            String modelYearStr = decodeVin.get("modelYear");
            Integer modelYear = null;
            if (hasValue(modelYearStr)) {
                try {
                    modelYear = Integer.valueOf(modelYearStr);
                    vinProvidedFields.add("modelYear");
                } catch (NumberFormatException e) {
                    log.warn("Model year parse edilmedi: {}", modelYearStr);
                }
            }

            String bodyType = decodeVin.get("bodyType");
            if (hasValue(bodyType)) vinProvidedFields.add("bodyType");

            String transmissionType = decodeVin.get("transmissionType");
            if (hasValue(transmissionType)) vinProvidedFields.add("transmissionType");

            String engineVolumeStr = decodeVin.get("engineVolume");
            Integer engineVolume = null;
            if (hasValue(engineVolumeStr)) {
                engineVolume = convertEngineVolumeSafe(engineVolumeStr);
                if (engineVolume != null) {
                    vinProvidedFields.add("engineVolume");
                }
            }

            String engineType = decodeVin.get("engineType");
            if (hasValue(engineType)) vinProvidedFields.add("engineType");

            return CarResponse.builder()
                    .vin(vin)
                    .brand(brand)
                    .model(model)
                    .modelYear(modelYear)
                    .bodyType(bodyType)
                    .transmissionType(transmissionType)
                    .engineVolume(engineVolume)
                    .engineType(engineType)
                    .vinProvidedFields(vinProvidedFields)
                    .resource("fromDecoderTool")
                    .build();
        }
    }

    @Override
    public List<Color> getColors(String acceptLanguage) {

        List<Color> colors = colorRepository.findAll();

        if (colors.isEmpty()) {
            throw new ResourceNotFoundException(
                    EnumMessagesLangValues.COLOR_NOT_FOUND.getMessageByLang(acceptLanguage)
            );
        }

        List<String> azOrder = List.of(
                "Ağ", "Bej", "Bənövşəyi", "Bordo", "Boz", "Gümüş", "İncə ağ",
                "Mat qara", "Mavi", "Metalik gümüş", "Narıncı", "Qara", "Qəhvəyi",
                "Qırmızı", "Qızıl", "Sarı", "Tünd mavi", "Yaşıl", "Digər"
        );

        List<String> enOrder = List.of(
                "Beige", "Black", "Blue", "Brown", "Gold", "Gray / Grey", "Green",
                "Maroon", "Matte Black", "Metallic silver", "Navy blue", "Orange",
                "Pearl white", "Purple", "Red", "Silver", "White", "Yellow", "Other"
        );

        return colors.stream()
                .peek(color -> {
                    String translated = ColorTranslation.translate(color.getColor(), acceptLanguage);
                    color.setColor(translated);
                })
                .sorted((c1, c2) -> {
                    String lang = acceptLanguage.toLowerCase();

                    if ("az".equals(lang)) {
                        return Integer.compare(
                                azOrder.indexOf(c1.getColor()),
                                azOrder.indexOf(c2.getColor())
                        );
                    } else {
                        if ("Other".equalsIgnoreCase(c1.getColor())) return 1;
                        if ("Other".equalsIgnoreCase(c2.getColor())) return -1;
                        return c1.getColor().compareToIgnoreCase(c2.getColor());
                    }
                })
                .toList();
    }


    @Override
    public List<RecordResponse> getServiceRecords(Long carId, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {

        if (carId == null || phoneNumber == null || userIdHeader == null || acceptLanguage == null) {
            log.info("body de missing fieldler var");
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader),
                phoneNumber, EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            log.info("customer tapilmadi");
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        log.info("Customer adi:{}", customer.getName());

        Car car = carRepository.findByCarIdAndCustomer(carId, customer);

        if (car == null) {
            log.info("car tapilmadi");
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        log.info("car id : {}", car.getCarId());

        List<CustomerServiceRecord> customerServiceRecordList = car.getServiceRecordList();

        if (customerServiceRecordList == null || customerServiceRecordList.isEmpty()) {
            log.info("customer service record list bosdur");
            throw new ResourceNotFoundException(EnumMessagesLangValues.RECORD_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        log.info("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
        log.info("record list:  {}", customerServiceRecordList);
        log.info("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");


        List<RecordResponse> responses = customerServiceRecordList.stream()
                .map(record -> RecordResponse.builder()
                        .id(record.getId())
                        .serviceName(record.getServiceName())
                        .serviceNameAz(record.getServiceNameAz())
                        .serviceNameRu(record.getServiceNameRu())
                        .actionType(record.getActionType())
                        .doneDate(record.getDoneDate())
                        .doneKm(record.getDoneKm())
                        .build())
                .toList();
        log.info("responses: {}", responses);
        return responses;
    }


    @Override
    public CarResponse editCarDetails(CarRequest carRequest, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {

        if (carRequest == null || phoneNumber == null || userIdHeader == null || carRequest.getCarId() == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }


        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(
                Long.valueOf(userIdHeader), phoneNumber, EnumUserStatus.ACTIVE.name());
        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        Car car = carRepository.findByCarIdAndCustomer(carRequest.getCarId(), customer);

        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        if (carRequest.getBrand() == null) {
            log.info("null");
        } else if (carRequest.getBrand().equals("")) {
            log.info("blank");
        } else {
            car.setBrand(carRequest.getBrand());
        }

        if (carRequest.getModel() == null) {
            log.info("null");
        } else if (carRequest.getModel().equals("")) {
            log.info("blank");
        } else {
            car.setModel(carRequest.getModel());
        }


        if (carRequest.getMileage() != null) {
            car.setMileage(carRequest.getMileage());
        }

        if (carRequest.getPlateNumber() != null) {
            car.setPlateNumber(carRequest.getPlateNumber());
        }

//        if (carRequest.getColorId() != null) {
//            car.setColorId(carRequest.getColorId());
//        }

        if (carRequest.getEngineTypeId() != null) {
            EngineType engineType = engineTypeRepository.findByEngineTypeId(carRequest.getEngineTypeId());
            car.setEngineType(engineType.getEngineType());
        }

        if (carRequest.getEngineVolume() != null) {
            car.setEngineVolume(carRequest.getEngineVolume());
        }

//        if (carRequest.getTransmissionType() != null) {
//            car.setTransmissionType(carRequest.getTransmissionType());
//        }

        if (carRequest.getBodyType() != null) {
            car.setBodyType(carRequest.getBodyType());
        }

        if (carRequest.getModelYear() != null) {
            car.setModelYear(carRequest.getModelYear());
        }
        carRepository.save(car);
        return convertCarEntityToResponse(car, acceptLanguage, "null");
    }


//    @Override
//    public void calculateAndPushNotification() {
//        List<Car> cars = carRepository.findAllWithCustomer();
//
//        for (Car car : cars) {
//
//            Customer customer = car.getCustomer();
//            if (customer == null) continue;
//
//            DeviceToken deviceToken = deviceTokenRepository.findByUserId(customer.getUserId());
//            if (deviceToken == null || deviceToken.getDeviceToken() == null) continue;
//
//            List<Percentage> percentages = percentageRepository.findAllByCarId(car.getCarId());
//            if (percentages == null || percentages.isEmpty()) continue;
//
//            for (Percentage percentage : percentages) {
//
//                if ((percentage.getKmPercentage() == null || percentage.getKmPercentage() == 0)
//                        && (percentage.getMonthPercentage() == null || percentage.getMonthPercentage() == 0)) {
//                    continue;
//                }
//
//                // 1 hafta kontrolü
//                if (!canSendNotification(percentage)) continue;
//
//                // %10 threshold
//                boolean kmLow = percentage.getKmPercentage() != null && percentage.getKmPercentage() <= 10;
//                boolean monthLow = percentage.getMonthPercentage() != null && percentage.getMonthPercentage() <= 10;
//
//                if (!kmLow && !monthLow) continue;
//
//                // Mesaj oluştur
//                String[] message = buildMessage(percentage, customer.getNotificationLanguage());
//
//                // Push gönder
//                boolean pushSent = sendServiceReminder(deviceToken.getDeviceToken(), message[0], message[1]);
//
//                if (pushSent) {
//                    percentage.setLastNotificationSentAt(LocalDateTime.now());
//                    percentageRepository.save(percentage);
//                }
//            }
//        }
//    }

    @Override
    public void calculateAndPushNotification() {
        List<Car> cars = carRepository.findAllWithCustomer();

        for (Car car : cars) {

            Customer customer = car.getCustomer();
            if (customer == null) continue;

            DeviceToken deviceToken = deviceTokenRepository.findByUserId(customer.getUserId());
            if (deviceToken == null || deviceToken.getDeviceToken() == null) continue;


            sendServiceReminder(deviceToken.getDeviceToken(), "Yağ dəyişimi vaxtı yaxınlaşır ⏰\n", car.getPlateNumber() + " - yağ dəyişiminə 230 km qalıb. Vaxtında baxım avtomobilinizi qoruyar \uD83D\uDEDE");
        }
    }

    @Override
    public PercentageResponseMain getServicePercentageList(Long carId, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {

        if (carId == null || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber, EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByCarIdAndCustomer(carId, customer);

        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.forLanguageTag(acceptLanguage));

        List<CarServicePercentageResponse> responseList = new ArrayList<>();

        MaintenanceTemplate template = car.getMaintenanceTemplate();
        log.info("template,{}", template.getId());
        List<ServiceEntity> serviceEntities = serviceEntityRepository.findAllByMaintenanceTemplate(template);
        log.info("service entities names,{}", serviceEntities.stream().map(serviceEntity -> serviceEntity.getServiceName() + " , ").toList());
        List<CustomerServiceRecord> customerServiceRecordList = customerServiceRecordRepository.findAllByCar(car);
        log.info("customerServiceRecordList, {}", customerServiceRecordList.stream().map(customerServiceRecord -> customerServiceRecord.getServiceName() + " , ").toList());
        List<ServiceHistory> serviceHistories = serviceHistoryRepository.findAllByCar(car);
        log.info("serviceHistories {}", serviceHistories.stream().map(serviceHistory -> serviceHistory.getServiceName() + " , ").toList());
        for (ServiceEntity serviceEntity : serviceEntities) {
            CustomerServiceRecord record = customerServiceRecordRepository.findByServiceNameAndCar(serviceEntity.getServiceName(), car);

            Percentage percentage = percentageRepository.findByServiceNameAndCarId(serviceEntity.getServiceName(), car.getCarId());

            boolean useEditedPercentage = percentage != null && "EDITED".equalsIgnoreCase(percentage.getStatus());

            // ===================== EDITED FLOW =====================
            if (useEditedPercentage) {

                LocalDate lastServiceDate = percentage.getLastServiceDate();
                Integer lastServiceKm = percentage.getLastServiceKm();
                Integer nextServiceKm = percentage.getNextServiceKm();

                Integer kmPercentage = null;
                Integer remainingKm = null;

                // ================= KM PERCENTAGE (EDITED LOGIC) =================
                if (lastServiceKm != null && nextServiceKm != null && car.getMileage() != null) {

                    long totalKm = nextServiceKm - lastServiceKm;
                    long remainingKmRaw = nextServiceKm - car.getMileage();

                    remainingKm = (int) Math.max(remainingKmRaw, 0);

                    if (totalKm > 0) {
                        kmPercentage = (int) Math.round((remainingKmRaw * 100.0) / totalKm);
                        kmPercentage = Math.max(0, Math.min(100, kmPercentage));
                    } else {
                        kmPercentage = 0;
                    }
                }

                // ================= MONTH / DAY PERCENTAGE (DAY-BASED) =================
                Integer monthPercentageDigit = null;
                String remainingDaysValue = null;
                LocalDate nextServiceDate = percentage.getNextServiceDate();

                if (lastServiceDate != null && nextServiceDate != null) {

                    long lastDay = lastServiceDate.toEpochDay();
                    long nextDay = nextServiceDate.toEpochDay();
                    long nowDay = LocalDate.now().toEpochDay();

                    long totalDays = nextDay - lastDay;     // full period
                    long remainingDays = nextDay - nowDay;  // remaining period

                    remainingDays = Math.max(remainingDays, 0);

                    if (totalDays > 0) {
                        monthPercentageDigit = (int) Math.round((remainingDays * 100.0) / totalDays);
                        monthPercentageDigit = Math.max(0, Math.min(100, monthPercentageDigit));
                    } else {
                        monthPercentageDigit = 0;
                    }

                    // sadece UI icin approximate ay (gun/30), yuzde icin KULLANILMIYOR
                    remainingDaysValue = String.valueOf(remainingDays);
                }

                responseList.add(

                        CarServicePercentageResponse.builder()
                                .percentageId(percentage.getId())
                                .serviceName(switch (acceptLanguage) {
                                    case "az" -> percentage.getServiceNameAz();
                                    case "ru" -> percentage.getServiceNameRu();
                                    default -> percentage.getServiceName();
                                })
                                .actionType(percentage.getActionType())
                                .intervalKm(percentage.getIntervalKm())
                                .intervalMonth(percentage.getIntervalMonth())
                                .kmPercentage(kmPercentage)
                                .monthPercentageDigit(monthPercentageDigit)
                                .remainingKm(remainingKm)
                                .remainingMonths(remainingDaysValue)
                                .lastServiceKm(lastServiceKm)
                                .lastServiceDate(lastServiceDate != null ? capitalizeMonth(lastServiceDate.format(formatter), Locale.forLanguageTag(acceptLanguage)) : null)
                                .nextServiceKm(nextServiceKm)
                                .nextServiceDate(nextServiceDate != null ? capitalizeMonth(nextServiceDate.format(formatter), Locale.forLanguageTag(acceptLanguage)) : null)
                                .status(percentage.getStatus())
                                .servicedStatus(record != null ? record.getServicedStatus() : null)
                                .build()
                );

                continue;
            }


// ================= CREATED / DEFAULT FLOW =================

            List<CustomerServiceRecord> csrList = customerServiceRecordList.stream()
                    .filter(r -> r.getServiceName().equalsIgnoreCase(serviceEntity.getServiceName()) && r.getActionType().equalsIgnoreCase(serviceEntity.getActionType()))
                    .toList();

            List<ServiceHistory> shList = serviceHistories.stream()
                    .filter(h -> h.getServiceName().equalsIgnoreCase(serviceEntity.getServiceName())
                            && h.getActionType() != null
                            && h.getActionType().stream().anyMatch(action -> action.equalsIgnoreCase(serviceEntity.getActionType())))
                    .toList();

            LocalDate lastServiceDate = Stream.concat(csrList.stream()
                            .map(CustomerServiceRecord::getDoneDate), shList
                            .stream()
                            .map(ServiceHistory::getDoneDate))
                    .filter(Objects::nonNull)
                    .max(LocalDate::compareTo)
                    .orElse(car.getCreatedAt().toLocalDate());

            Integer lastServiceKm = Stream.concat(csrList.stream().map(CustomerServiceRecord::getDoneKm), shList
                            .stream()
                            .map(ServiceHistory::getDoneKm))
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);

// ================= KM CALC =================

            Integer remainingKm = null;
            Integer kmPercentage = null;
            Integer nextServiceKm = null;

            if (serviceEntity.getIntervalKm() != null && car.getMileage() != null) {

                long intervalKm = serviceEntity.getIntervalKm();
                long usedKm = car.getMileage() - lastServiceKm;

                remainingKm = (int) Math.max(intervalKm - usedKm, 0);
                nextServiceKm = Math.toIntExact(lastServiceKm + intervalKm);

                kmPercentage = (int) Math.round((remainingKm * 100.0) / intervalKm);

                kmPercentage = Math.max(0, Math.min(100, kmPercentage));
            }

// ================= MONTH CALC (GÜN BAZLI) =================

            String remainingDaysValue = null;
            Integer monthPercentageDigit = null;
            LocalDate nextServiceDate = null;

            if (serviceEntity.getIntervalMonth() != null) {

                int intervalMonth = serviceEntity.getIntervalMonth();

                nextServiceDate = lastServiceDate.plusMonths(intervalMonth);

                long lastDay = lastServiceDate.toEpochDay();
                long nextDay = nextServiceDate.toEpochDay();
                long nowDay = LocalDate.now().toEpochDay();

                long totalDays = nextDay - lastDay;       // next - last
                long remainingDays = nextDay - nowDay;    // next - current

                remainingDays = Math.max(remainingDays, 0);

                if (totalDays > 0) {
                    monthPercentageDigit = (int) Math.round((remainingDays * 100.0) / totalDays);
                    monthPercentageDigit = Math.max(0, Math.min(100, monthPercentageDigit));
                } else {
                    monthPercentageDigit = 0;
                }

                remainingDaysValue = String.valueOf(remainingDays);
            }

// ================= RESPONSE =================

            responseList.add(
                    CarServicePercentageResponse.builder()
                            .percentageId(percentage != null ? percentage.getId() : null)
                            .serviceName(switch (acceptLanguage) {
                                case "az" -> percentage.getServiceNameAz();
                                case "ru" -> percentage.getServiceNameRu();
                                default -> percentage.getServiceName();
                            })
                            .actionType(serviceEntity.getActionType())
                            .intervalKm(serviceEntity.getIntervalKm())
                            .intervalMonth(serviceEntity.getIntervalMonth())
                            .kmPercentage(kmPercentage)
                            .monthPercentageDigit(monthPercentageDigit)
                            .remainingKm(remainingKm)
                            .remainingMonths(remainingDaysValue)
                            .lastServiceKm(lastServiceKm)
                            .lastServiceDate(capitalizeMonth(lastServiceDate.format(formatter), Locale.forLanguageTag(acceptLanguage)))
                            .nextServiceKm(nextServiceKm)
                            .nextServiceDate(nextServiceDate != null ? capitalizeMonth(nextServiceDate.format(formatter), Locale.forLanguageTag(acceptLanguage)) : null)
                            .status(percentage.getStatus())
                            .servicedStatus(record != null ? record.getServicedStatus() : null)
                            .build()
            );

        }
        System.err.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        responseList.forEach(System.out::println);
        System.err.println("########################################################################################################################################");
        return PercentageResponseMain.builder()
                .carId(car.getCarId())
                .vin(car.getVin())
                .responseList(responseList)
                .build();
    }

    @Override
    public CarServicePercentageResponse editPercentage(PercentageRequest request, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {

        if (request == null || request.getCarId() == null || request.getPercentageId() == null || phoneNumber == null || userIdHeader == null) {
            log.error("missing body var");
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader),
                phoneNumber, EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            log.error("customer null");
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByCarIdAndCustomer(request.getCarId(), customer);

        if (car == null) {
            log.error("car null");
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Percentage percentage = percentageRepository.findById(request.getPercentageId())
                .orElseThrow(() -> {
                    log.error("Percentage tapılmadı. ID: {}", request.getPercentageId());
                    return new ResourceNotFoundException("Hesablama tapilmadi");
                });
        if (!percentage.getCarId().equals(car.getCarId())) {
            log.error("Hesablama bu avtomobile aid deyil");
            throw new ResourceNotFoundException("Hesablama bu avtomobile aid deyil");
        }

        if (request.getLastServiceKm() != null) {
            percentage.setLastServiceKm(request.getLastServiceKm());
        }
        if (request.getNextServiceKm() != null) {
            percentage.setNextServiceKm(request.getNextServiceKm());
        }
        if (request.getLastServiceDate() != null) {
            percentage.setLastServiceDate(request.getLastServiceDate());
        }
        if (request.getNextServiceDate() != null) {
            percentage.setNextServiceDate(request.getNextServiceDate());
        }
        percentage.setStatus("EDITED");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.forLanguageTag(acceptLanguage));

        percentageRepository.save(percentage);

        CarServicePercentageResponse response = CarServicePercentageResponse.builder()
                .percentageId(percentage.getId())
                .serviceName(switch (acceptLanguage) {
                    case "az" -> percentage.getServiceNameAz();
                    case "ru" -> percentage.getServiceNameRu();
                    default -> percentage.getServiceName();
                })
                .actionType(percentage.getActionType())

                .intervalKm(percentage.getIntervalKm())
                .intervalMonth(percentage.getIntervalMonth())

                .kmPercentage(percentage.getKmPercentage())
                .monthPercentage(percentage.getMonthPercentage())

                .remainingKm(percentage.getRemainingKm())
                .remainingMonths(percentage.getRemainingMonths().format(formatter))

                .lastServiceKm(percentage.getLastServiceKm())
                .lastServiceDate(percentage.getLastServiceDate().format(formatter))
                .nextServiceKm(percentage.getNextServiceKm())
                .nextServiceDate(percentage.getNextServiceDate().format(formatter))
                .build();
        log.info("response: {}", response);
        return response;
    }


    @Override
    public PercentageResponseMain executeServicePercentages(
            Long carId,
            String phoneNumber,
            String userIdHeader,
            String timezone,
            String acceptLanguage) {

        if (carId == null || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(
                    EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(
                Long.valueOf(userIdHeader),
                phoneNumber,
                EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(
                    EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByCarIdAndCustomer(carId, customer);
        if (car == null) {
            throw new ResourceNotFoundException(
                    EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        List<Percentage> percentages =
                percentageRepository.findAllByCarId(car.getCarId());

        if (percentages.isEmpty()) {
            throw new ResourceNotFoundException(
                    EnumMessagesLangValues.SERVICE_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        /* ===== Locale & Date Formatter ===== */
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag(acceptLanguage));
        List<CarServicePercentageResponse> responseList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Percentage percentage : percentages) {
            //  EDITED olsa birbasa hesablamadan db den versin manipulation olmasin update , save olmasin
            if ("EDITED".equals(percentage.getStatus())) {

                responseList.add(
                        CarServicePercentageResponse.builder()
                                .serviceName(percentage.getServiceName())
                                .actionType(percentage.getActionType())
                                .intervalKm(percentage.getIntervalKm())
                                .intervalMonth(percentage.getIntervalMonth())
                                .kmPercentage(percentage.getKmPercentage())
                                .monthPercentage(percentage.getMonthPercentage())
                                .remainingKm(percentage.getRemainingKm())
                                .remainingMonths(
                                        percentage.getRemainingMonths() != null
                                                ? percentage.getRemainingMonths().format(formatter)
                                                : null
                                )
                                .lastServiceKm(percentage.getLastServiceKm())
                                .lastServiceDate(
                                        percentage.getLastServiceDate() != null
                                                ? percentage.getLastServiceDate().format(formatter)
                                                : null
                                )
                                .nextServiceKm(percentage.getNextServiceKm())
                                .nextServiceDate(
                                        percentage.getNextServiceDate() != null
                                                ? percentage.getNextServiceDate().format(formatter)
                                                : null
                                )
                                .build()
                );

                continue;
            }
            CustomerServiceRecord customerRecord =
                    customerServiceRecordRepository
                            .findByServiceNameAndCar(percentage.getServiceName(), car);

            ServiceHistory serviceHistory =
                    serviceHistoryRepository
                            .findTopByServiceNameAndCarOrderByDoneDateDesc(
                                    percentage.getServiceName(), car)
                            .orElse(null);

            /* ===== LAST SERVICE ===== */
            LocalDate lastServiceDate = car.getCreatedAt() != null ? car.getCreatedAt().toLocalDate() : today;
            Integer lastServiceKm = 0;

            if (customerRecord != null && serviceHistory != null) {
                LocalDate customerDone = customerRecord.getDoneDate() != null ? customerRecord.getDoneDate() : lastServiceDate;
                int customerKm = customerRecord.getDoneKm() != null ? customerRecord.getDoneKm() : 0;

                LocalDate historyDone = serviceHistory.getDoneDate() != null ? serviceHistory.getDoneDate() : lastServiceDate;
                int historyKm = serviceHistory.getDoneKm() != null ? serviceHistory.getDoneKm() : 0;

                if (customerDone.isAfter(historyDone)) {
                    lastServiceDate = customerDone;
                    lastServiceKm = customerKm;
                } else {
                    lastServiceDate = historyDone;
                    lastServiceKm = historyKm;
                }
            } else if (customerRecord != null) {
                lastServiceDate = customerRecord.getDoneDate() != null ? customerRecord.getDoneDate() : lastServiceDate;
                lastServiceKm = customerRecord.getDoneKm() != null ? customerRecord.getDoneKm() : 0;
            } else if (serviceHistory != null) {
                lastServiceDate = serviceHistory.getDoneDate() != null ? serviceHistory.getDoneDate() : lastServiceDate;
                lastServiceKm = serviceHistory.getDoneKm() != null ? serviceHistory.getDoneKm() : 0;
            }

            /* ===== TIME & KM ===== */
            long monthsPassed = (today.getYear() - lastServiceDate.getYear()) * 12L
                    + (today.getMonthValue() - lastServiceDate.getMonthValue());
            if (monthsPassed < 0) monthsPassed = 0;

            long kmPassed = Math.max(0, car.getMileage() - lastServiceKm);

            /* ===== USED % ===== */
            int intervalKm = Math.toIntExact(percentage.getIntervalKm() != null ? percentage.getIntervalKm() : 0L);
            int intervalMonth = percentage.getIntervalMonth() != null ? percentage.getIntervalMonth() : 0;

            double usedKmPercentage = intervalKm != 0 ? (double) kmPassed / intervalKm * 100 : 0;
            double usedMonthPercentage = intervalMonth != 0 ? (double) monthsPassed / intervalMonth * 100 : 0;

            usedKmPercentage = Math.min(100.0, usedKmPercentage);
            usedMonthPercentage = Math.min(100.0, usedMonthPercentage);

            /* ===== REMAINING % (INTEGER) ===== */
            int remainingKmPercentage = (int) Math.round(100.0 - usedKmPercentage);
            int remainingMonthPercentage = (int) Math.round(100.0 - usedMonthPercentage);

            /* ===== NEXT SERVICE ===== */
            Integer nextServiceKm = lastServiceKm + intervalKm;
            LocalDate nextServiceDate = lastServiceDate.plusMonths(intervalMonth);
            Integer remainingKm = (int) Math.max(0, intervalKm - kmPassed);

            /* ===== RESPONSE ===== */
            responseList.add(
                    CarServicePercentageResponse.builder()
                            .serviceName(switch (acceptLanguage) {
                                case "az" -> percentage.getServiceNameAz();
                                case "ru" -> percentage.getServiceNameRu();
                                default -> percentage.getServiceName();
                            })
                            .actionType(percentage.getActionType())
                            .intervalKm((long) intervalKm)
                            .intervalMonth(intervalMonth)
                            .kmPercentage(remainingKmPercentage)
                            .monthPercentage(remainingMonthPercentage)
                            .remainingKm(remainingKm)
                            .remainingMonths(nextServiceDate.format(formatter))
                            .lastServiceKm(lastServiceKm)
                            .lastServiceDate(lastServiceDate.format(formatter))
                            .nextServiceKm(nextServiceKm)
                            .nextServiceDate(nextServiceDate.format(formatter))
                            .build()
            );

            /* ===== SAVE BACK TO PERCENTAGE ===== */
            percentage.setLastServiceDate(lastServiceDate);
            percentage.setLastServiceKm(lastServiceKm);
            percentage.setNextServiceDate(nextServiceDate);
            percentage.setNextServiceKm(nextServiceKm);
            percentage.setRemainingKm(remainingKm);
            percentage.setRemainingMonths(nextServiceDate);
            percentage.setKmPercentage(remainingKmPercentage);
            percentage.setMonthPercentage(remainingMonthPercentage);
            percentage.setStatus("CREATED");
            percentageRepository.save(percentage);
        }

        return PercentageResponseMain.builder()
                .carId(car.getCarId())
                .vin(car.getVin())
                .responseList(responseList)
                .build();
    }


    @Override
    @Transactional
    public CarResponse addCar(CarRequest carRequest, String phoneNumber, String userIdHeader,
                              String timezone, String acceptLanguage) {

        log.info("[addCar] START | phoneNumber={}, userIdHeader={}, timezone={}, acceptLanguage={}",
                phoneNumber, userIdHeader, timezone, acceptLanguage);
        log.info("[addCar] CarRequest | vin={}, plateNumber={}, brand={}, model={}, modelYear={}, colorId={}, " +
                        "engineType={}, engineTypeId={}, engineVolume={}, transmissionType={}, bodyType={}, " +
                        "mileage={}, carId={}, vinProvidedFields={}",
                carRequest != null ? carRequest.getVin() : null,
                carRequest != null ? carRequest.getPlateNumber() : null,
                carRequest != null ? carRequest.getBrand() : null,
                carRequest != null ? carRequest.getModel() : null,
                carRequest != null ? carRequest.getModelYear() : null,
                carRequest != null ? carRequest.getColorId() : null,
                carRequest != null ? carRequest.getEngineType() : null,
                carRequest != null ? carRequest.getEngineTypeId() : null,
                carRequest != null ? carRequest.getEngineVolume() : null,
                carRequest != null ? carRequest.getTransmissionType() : null,
                carRequest != null ? carRequest.getBodyType() : null,
                carRequest != null ? carRequest.getMileage() : null,
                carRequest != null ? carRequest.getCarId() : null,
                carRequest != null ? carRequest.getVinProvidedFields() : null);

        log.info("[addCar] CHECK required fields | carRequest={}, phoneNumber={}, userIdHeader={}, engineTypeId={}",
                carRequest != null, phoneNumber != null, userIdHeader != null,
                carRequest != null ? carRequest.getEngineTypeId() : null);
        if (carRequest == null || phoneNumber == null || userIdHeader == null || carRequest.getEngineTypeId() == null) {
            log.warn("[addCar] FAIL MissingFieldException | reason=carRequest/phoneNumber/userIdHeader/engineTypeId null");
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }
        log.info("[addCar] PASS required fields check");

        log.info("[addCar] CHECK vin/plateNumber/mileage | vin={}, plateNumber={}, mileage={}",
                carRequest.getVin(), carRequest.getPlateNumber(), carRequest.getMileage());
        if (carRequest.getVin() == null || carRequest.getPlateNumber() == null || carRequest.getMileage() == null) {
            log.warn("[addCar] FAIL MissingFieldException | reason=vin/plateNumber/mileage null");
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }
        log.info("[addCar] PASS vin/plateNumber/mileage check");

        log.info("[addCar] customerRepository.findByUserIdAndPhoneNumberAndStatus | userId={}, phoneNumber={}, status={}",
                userIdHeader, phoneNumber, EnumUserStatus.ACTIVE.name());
        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(
                Long.valueOf(userIdHeader), phoneNumber, EnumUserStatus.ACTIVE.name());
        log.info("[addCar] customer lookup result | customerUserId={}", customer != null ? customer.getUserId() : null);
        if (customer == null) {
            log.warn("[addCar] FAIL UserNotFoundException | userIdHeader={}, phoneNumber={}", userIdHeader, phoneNumber);
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        log.info("[addCar] PASS customer found | customerUserId={}", customer.getUserId());

        log.info("[addCar] carRepository.findByVin | vin={}", carRequest.getVin());
        Car existingCar = carRepository.findByVin(carRequest.getVin());
        log.info("[addCar] existing car lookup result | carId={}, hasCustomer={}, customerUserId={}",
                existingCar != null ? existingCar.getCarId() : null,
                existingCar != null && existingCar.getCustomer() != null,
                existingCar != null && existingCar.getCustomer() != null ? existingCar.getCustomer().getUserId() : null);

        if (existingCar != null && existingCar.getCustomer() != null) {
            log.warn("[addCar] FAIL AlreadyExistsException | carId={}, ownerUserId={}, requestUserId={}",
                    existingCar.getCarId(), existingCar.getCustomer().getUserId(), customer.getUserId());
            throw new AlreadyExistsException("avtomobil basqasina mexsusdur");
        }

        if (existingCar != null) {
            log.info("[addCar] BRANCH existing car without customer | linking carId={} to customerUserId={}",
                    existingCar.getCarId(), customer.getUserId());
            existingCar.setCustomer(customer);
            customer.getCars().add(existingCar);
            log.info("[addCar] calling convertCarEntityToResponse | carId={}, resource=fromDb", existingCar.getCarId());
            CarResponse response = convertCarEntityToResponse(existingCar, acceptLanguage, "fromDb");
            log.info("[addCar] END success (existing car linked) | carId={}, vin={}", response.getCarId(), response.getVin());
            return response;
        }

        log.info("[addCar] BRANCH new car flow | engineTypeId={}", carRequest.getEngineTypeId());
        log.info("[addCar] engineTypeRepository.findByEngineTypeId | engineTypeId={}", carRequest.getEngineTypeId());
        EngineType engineType = engineTypeRepository.findByEngineTypeId(carRequest.getEngineTypeId());
        log.info("[addCar] engine type lookup result | engineTypeId={}, engineType={}",
                engineType != null ? engineType.getEngineTypeId() : null,
                engineType != null ? engineType.getEngineType() : null);

        log.info("[addCar] maintenanceTemplateRepository.findByEngineType | engineType={}", engineType.getEngineType());
        MaintenanceTemplate maintenanceTemplate = maintenanceTemplateRepository.findByEngineType(engineType)
                .orElseThrow(() -> {
                    log.warn("[addCar] FAIL ResourceNotFoundException | reason=maintenance template not found for engineType={}",
                            engineType.getEngineType());
                    return new ResourceNotFoundException(EnumMessagesLangValues.TEMPLATE_NOT_FOUND.getMessageByLang(acceptLanguage));
                });
        log.info("[addCar] PASS maintenance template found | templateId={}, templateName={}",
                maintenanceTemplate.getId(), maintenanceTemplate.getName());

        log.info("[addCar] building new Car entity | vin={}, plateNumber={}, brand={}, model={}",
                carRequest.getVin(), carRequest.getPlateNumber(), carRequest.getBrand(), carRequest.getModel());
        Car newCar = Car.builder()
                .vin(carRequest.getVin())
                .plateNumber(carRequest.getPlateNumber())
                .brand(carRequest.getBrand())
                .model(carRequest.getModel())
                .modelYear(carRequest.getModelYear())
                .engineType(engineType.getEngineType())
                .engineTypeId(engineType.getEngineTypeId())
                .engineVolume(carRequest.getEngineVolume())
                .transmissionType(carRequest.getTransmissionType())
                .bodyType(carRequest.getBodyType())
                .mileage(carRequest.getMileage())
                .colorId(carRequest.getColorId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .customer(customer)
                .maintenanceTemplate(maintenanceTemplate)
                .vinProvidedFields(carRequest.getVinProvidedFields())
                .build();

        log.info("[addCar] carRepository.save | vin={}", newCar.getVin());
        carRepository.save(newCar);
        log.info("[addCar] car saved | carId={}, vin={}", newCar.getCarId(), newCar.getVin());

        log.info("[addCar] creating Percentage + CustomerServiceRecord | serviceCount={}",
                maintenanceTemplate.getServices() != null ? maintenanceTemplate.getServices().size() : 0);
        for (ServiceEntity serviceEntity : maintenanceTemplate.getServices()) {
            log.info("[addCar] processing ServiceEntity | serviceName={}, actionType={}, intervalKm={}, intervalMonth={}",
                    serviceEntity.getServiceName(), serviceEntity.getActionType(),
                    serviceEntity.getIntervalKm(), serviceEntity.getIntervalMonth());

            Percentage percentage = Percentage.builder()
                    .intervalKm(serviceEntity.getIntervalKm())
                    .intervalMonth(serviceEntity.getIntervalMonth())
                    .serviceName(serviceEntity.getServiceName())
                    .serviceNameAz(serviceEntity.getNameAz())
                    .serviceNameRu(serviceEntity.getNameRu())
                    .actionType(serviceEntity.getActionType())
                    .carId(newCar.getCarId())
                    .build();

            log.info("[addCar] percentageRepository.save | carId={}, serviceName={}", newCar.getCarId(), percentage.getServiceName());
            percentageRepository.save(percentage);

            CustomerServiceRecord customerServiceRecord = CustomerServiceRecord.builder()
                    .serviceName(serviceEntity.getServiceName())
                    .serviceNameAz(serviceEntity.getNameAz())
                    .serviceNameRu(serviceEntity.getNameRu())
                    .actionType(serviceEntity.getActionType())
                    .car(newCar)
                    .build();
            log.info("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
            log.info("[addCar] customerServiceRecordRepository.save  serviceName={}", customerServiceRecord.getServiceName());
            log.info("OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");

            customerServiceRecordRepository.save(customerServiceRecord);
        }
        log.info("[addCar] PASS all service records created");

        log.info("[addCar] linking car to customer | customerUserId={}, carId={}", customer.getUserId(), newCar.getCarId());
        customer.getCars().add(newCar);
        log.info("[addCar] customerRepository.save | customerUserId={}", customer.getUserId());
        customerRepository.save(customer);
        log.info("[addCar] PASS customer updated");

        log.info("[addCar] calling convertCarEntityToResponse | carId={}, resource=fromDecoderTool", newCar.getCarId());
        CarResponse response = convertCarEntityToResponse(newCar, acceptLanguage, "fromDecoderTool");
        log.info("[addCar] END success (new car) | carId={}, vin={}, plateNumber={}",
                response.getCarId(), response.getVin(), response.getPlateNumber());
        return response;
    }

    @Override
    public CarResponse removeCar(CarRequest carRequest, String phoneNumber, String userIdHeader, String timezone,
                                 String acceptLanguage) {

        if (carRequest == null || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(
                Long.valueOf(userIdHeader), phoneNumber, EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByCarIdAndCustomer(carRequest.getCarId(), customer);

        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        customer.getCars().remove(car);
        car.setCustomer(null);
        carRepository.save(car);
        customerRepository.save(customer);

        return CarResponse.builder()
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }


    @Override
    public CarResponse getCarByVinCode(CarRequest carRequest, String phoneNumber, String userIdHeader,
                                       String timezone, String acceptLanguage) {

        if (carRequest == null || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }
        if (carRequest.getVin() == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber, EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car existingCar = carRepository.findByVin(carRequest.getVin());


        if (existingCar != null && !existingCar.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new NotMatchException(EnumMessagesLangValues.CAR_NOT_MATCH_WITH_CUSTOMER.getMessageByLang(acceptLanguage));
        }

        if (existingCar == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        return convertCarEntityToResponse(existingCar, acceptLanguage, "null");

    }

    @Override
    public List<CarResponse> getCarListByUserId(String phoneNumber, String userIdHeader,
                                                String timezone, String acceptLanguage) {
        if (phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber, EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        List<Car> carList = carRepository.findAllByCustomer(customer);

        if (carList == null || carList.isEmpty()) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        List<CarResponse> responses = carList.stream().map(car -> convertCarEntityToResponse(car, acceptLanguage, "null")).collect(Collectors.toList());
        Log log1 = new Log();
        List<Long> carIds = responses.stream().map(CarResponse::getCarId).toList();
        log1.setUserId(userIdHeader);
        log1.setLog(LocalDateTime.now() + " **** " + phoneNumber + " **** " + userIdHeader + " **** " + carIds);
        logRepository.save(log1);
        return responses;
    }


    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


    @Override
    public CarResponse updateMileage(CarRequest carRequest, String phoneNumber, String userIdHeader, String timezone,
                                     String acceptLanguage) {
        if (carRequest == null || carRequest.getVin() == null || carRequest.getMileage() == null || phoneNumber == null
                || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Long userId = Long.valueOf(userIdHeader);
        Car car;

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(userId, phoneNumber,
                EnumUserStatus.ACTIVE.name());

        if (customer != null) {
            log.info("Mileage update edən avtomobil sahibidir: {}", customer.getUserId());
            car = carRepository.findByVinAndCustomer(carRequest.getVin(), customer);
        } else {
            Admin admin = adminRepository.findByUserIdAndPhoneNumberAndStatus(userId, phoneNumber,
                    EnumUserStatus.ACTIVE.name());

            if (admin == null) {
                log.warn("Mileage update eden ne avtomobil sahibi ne de admindir. Istek redd edilir.");
                throw new InvalidStatusException(EnumMessagesLangValues.INVALID_ROLE_PERMISSION.getMessageByLang(acceptLanguage));
            }

            log.info("Mileage update eden admindir : {}", admin.getUserId());
            car = carRepository.findByVin(carRequest.getVin());
        }

        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        car.setMileage(carRequest.getMileage());
        car.setUpdatedAt(LocalDateTime.now(ZoneId.of(timezone)));

        carRepository.save(car);

        return convertCarEntityToResponse(car, acceptLanguage, "null");
    }

    @Override
    public RecordResponse addRecord(RecordRequest request, String phoneNumber, String userIdHeader,
                                    String timezone, String acceptLanguage) {

        if (request == null || request.getCarId() == null || phoneNumber == null || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }
        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber,
                EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByCarIdAndCustomer(request.getCarId(), customer);

        if (car == null || !customer.getCars().contains(car)) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        ServiceEntity serviceEntity = serviceEntityRepository.findByServiceNameAndActionType(request.getServiceName(), request.getActionType());

        if (serviceEntity == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.SERVICE_NOT_FOUND.getMessageByLang(acceptLanguage));
        }
        CustomerServiceRecord existingRecord = customerServiceRecordRepository.findByServiceNameAndActionTypeAndCar(request.getServiceName(), request.getActionType(), car);
        if (existingRecord != null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.RECORD_ALREADY_EXISTS.getMessageByLang(acceptLanguage));
        }
        CustomerServiceRecord record = CustomerServiceRecord.builder()
                .serviceName(request.getServiceName())
                .actionType(request.getActionType())
                .doneDate(request.getDoneDate())
                .doneKm(request.getDoneKm())
                .car(car)
                .build();
        customerServiceRecordRepository.save(record);
        return RecordResponse.builder()
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }

    @Override
    public RecordResponse updateRecord(RecordRequest request, String phoneNumber, String userIdHeader,
                                       String timezone, String acceptLanguage) {

        if (request == null || request.getCarId() == null || request.getRecordId() == null || phoneNumber == null
                || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }
        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber,
                EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByCarIdAndCustomer(request.getCarId(), customer);

        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        CustomerServiceRecord record = customerServiceRecordRepository.findByIdAndCar(request.getRecordId(), car);

        if (record == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.RECORD_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        if (request.getDoneDate() != null) {
            record.setDoneDate(request.getDoneDate());
        }

        if (request.getDoneKm() != null) {
            record.setDoneKm(request.getDoneKm());
        }
        record.setServicedStatus(request.getServicedStatus());

        customerServiceRecordRepository.save(record);

        return RecordResponse.builder()
                .servicedStatus(record.getServicedStatus())
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }

    @Override
    public RecordResponse getRecord(RecordRequest request, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) {
        if (request == null || request.getCarId() == null || request.getServiceName() == null || phoneNumber == null
                || userIdHeader == null) {
            throw new MissingFieldException(EnumMessagesLangValues.MISSING_BODY.getMessageByLang(acceptLanguage));
        }

        Customer customer = customerRepository.findByUserIdAndPhoneNumberAndStatus(Long.valueOf(userIdHeader), phoneNumber,
                EnumUserStatus.ACTIVE.name());

        if (customer == null) {
            throw new UserNotFoundException(EnumMessagesLangValues.USER_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        Car car = carRepository.findByCarIdAndCustomer(request.getCarId(), customer);

        if (car == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.CAR_NOT_FOUND.getMessageByLang(acceptLanguage));
        }

        CustomerServiceRecord record = customerServiceRecordRepository.findByServiceNameAndCar(request.getServiceName(), car);

        if (record == null) {
            throw new ResourceNotFoundException(EnumMessagesLangValues.RECORD_NOT_FOUND.getMessageByLang(acceptLanguage));
        }


        return RecordResponse.builder()
                .id(record.getId())
                .serviceName(record.getServiceName())
                .serviceNameAz(record.getServiceNameAz())
                .serviceNameRu(record.getServiceNameRu())
                .actionType(record.getActionType())
                .doneDate(record.getDoneDate())
                .doneKm(record.getDoneKm())
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .build();
    }


    private CarResponse convertCarEntityToResponse(Car car, String acceptLanguage, String resource) {
        Color color = colorRepository.findByColorId(car.getColorId());
        String colorResponse = color != null ? ColorTranslation.translate(color.getColor(), acceptLanguage) : "unknown";
        return CarResponse.builder()
                .carId(car.getCarId())
                .customerId(car.getCustomer().getUserId())
                .vin(car.getVin())
                .plateNumber(car.getPlateNumber())
                .brand(car.getBrand())
                .model(car.getModel())
                .modelYear(car.getModelYear())
                .color(colorResponse)
                .engineType(
                        EngineTypeTranslation.translate(
                                car.getEngineType(),
                                acceptLanguage
                        )
                )                .engineVolume(car.getEngineVolume())
                .engineTypeId(car.getEngineTypeId())
                .transmissionType(car.getTransmissionType())
                .mileage(car.getMileage())
                .updatedAt(car.getUpdatedAt())
                .bodyType(car.getBodyType())
                .message(EnumMessagesLangValues.SUCCESS.getMessageByLang(acceptLanguage))
                .vinProvidedFields(car.getVinProvidedFields())
                .resource(resource)
                .build();
    }

    private Integer convertEngineVolumeSafe(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }

        try {
            return convertEngineVolume(val);
        } catch (Exception e) {
            log.warn("Engine volume parse edilemedi: {}", val);
            return null;
        }
    }

    private Integer convertEngineVolume(String value) {
        if (value == null) return null;

        value = value.trim();

        if (value.contains(".")) {
            double liters = Double.parseDouble(value);
            return (int) (liters * 1000); // 1.5 → 1500
        }

        return Integer.parseInt(value);
    }

    private String capitalizeMonth(String date, Locale locale) {
        if (!locale.getLanguage().equals("az")) {
            return date;
        }

        String[] parts = date.split(" ");
        if (parts.length < 3) return date;

        parts[1] = parts[1].substring(0, 1).toUpperCase(locale)
                + parts[1].substring(1);

        return String.join(" ", parts);
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private boolean canSendNotification(Percentage percentage) {

        LocalDateTime lastSent = percentage.getLastNotificationSentAt();

        if (lastSent == null) {
            return true;
        }

        // TEST için: 15 dakikada bir tekrar gönder
        return lastSent.plusMinutes(5).isBefore(LocalDateTime.now());

        // PROD için tekrar 7 güne döndür
        // return lastSent.plusDays(7).isBefore(LocalDateTime.now());
    }


    private String[] buildMessage(Percentage percentage, String lang) {
        // ---------------- Service Name ----------------
//        String serviceNameTranslated = ServiceNameAz.translate(percentage.getServiceName(), lang);

        // ---------------- Threshold türünü seç (en düşük olan) ----------------
        Integer km = percentage.getKmPercentage();
        Integer month = percentage.getMonthPercentage();

        boolean kmLow = km != null && km > 0 && km <= 10;
        boolean monthLow = month != null && month > 0 && month <= 10;

        boolean isKmBased;

        if (kmLow && monthLow) {
            // ikisi de düşükse en küçük olanı baz al
            isKmBased = km <= month;
        } else if (kmLow) {
            isKmBased = true;
        } else if (monthLow) {
            isKmBased = false;
        } else {
            // hiçbiri düşük değilse default km
            isKmBased = true;
        }

        String title;
        String body;

        // ---------------- GÜN SAYISINI MANUEL HESAPLA ----------------
        long remainingDays = 0;
        if (!isKmBased) {
            LocalDate today = LocalDate.now();
            LocalDate nextServiceDate = percentage.getNextServiceDate();

            if (nextServiceDate != null) {
                remainingDays = nextServiceDate.toEpochDay() - today.toEpochDay();
                if (remainingDays < 0) remainingDays = 0; // negatif olmasın
            }
        }

        // ---------------- MESAJ OLUŞTUR ----------------
        if ("az".equalsIgnoreCase(lang)) {
            title = percentage.getServiceNameAz() + " vaxtı yaxınlaşır🛞";

            if (isKmBased) {
                body = "Avtomobilinizin " + percentage.getServiceNameAz()
                        + " üçün " + percentage.getRemainingKm()
                        + " km qalıb. Zəhmət olmasa baxımı planlayın😀";
            } else {
                body = "Avtomobilinizin " + percentage.getServiceNameAz()
                        + " üçün " + remainingDays
                        + " gün qalıb. Zəhmət olmasa baxımı planlayın😀";
            }

        } else { // default en
            title = percentage.getServiceName() + " reminder 🛞";

            if (isKmBased) {
                body = "Your car’s " + percentage.getServiceName()
                        + " is due in " + percentage.getRemainingKm()
                        + " km. Please schedule your maintenance 😀";
            } else {
                body = "Your car’s " + percentage.getServiceName()
                        + " is due in " + remainingDays
                        + " days. Please schedule your maintenance 😀";
            }
        }

        return new String[]{title, body};
    }


    private boolean sendServiceReminder(String deviceToken, String title, String body) {
        try {
            pushNotificationService.send(title, body, deviceToken);
            log.info("PUSH SENT -> token={}, title={}, body={}", deviceToken, title, body);
            return true;
        } catch (RuntimeException e) {
            log.error("Push gönderilemedi -> token={}, title={}, body={}, hata={}", deviceToken, title, body, e.getMessage());
            return false;
        }
    }


}


