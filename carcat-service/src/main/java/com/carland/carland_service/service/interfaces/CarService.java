package com.carland.carland_service.service.interfaces;

import com.carland.carland_service.dto.request.CarRequest;
import com.carland.carland_service.dto.request.PercentageRequest;
import com.carland.carland_service.dto.request.RecordRequest;
import com.carland.carland_service.dto.response.*;
import com.carland.carland_service.entity.Color;
import com.carland.carland_service.entity.CustomerServiceRecord;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

public interface CarService {
    CarResponse addCar(CarRequest carRequest, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage) ;

    CarResponse getCarByVinCode(CarRequest carRequest, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    List<CarResponse> getCarListByUserId( String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


   PercentageResponseMain executeServicePercentages(Long carId, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    CarResponse updateMileage(CarRequest carRequest, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    RecordResponse addRecord( RecordRequest request, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    RecordResponse updateRecord(RecordRequest request, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);

    RecordResponse getRecord(RecordRequest request, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    CarResponse removeCar(CarRequest carRequest, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);

    CarResponse checkVin(String vin ,  String acceptLanguage);

    List<Color> getColors(String  acceptLanguage);

    List<RecordResponse> getServiceRecords(Long carId, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);

    CarResponse editCarDetails(CarRequest carRequest, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);


    PercentageResponseMain getServicePercentageList(Long carId, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);

    CarServicePercentageResponse editPercentage(PercentageRequest request, String phoneNumber, String userIdHeader, String timezone, String acceptLanguage);

    void calculateAndPushNotification();

}
