package com.carland.carland_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CarRequest {
    String vin;
    Long carId;
    String plateNumber;
    String brand;
    String model;
    Integer modelYear;
    Long colorId;
    String engineType;
    Long engineTypeId;
    Integer engineVolume;
    String transmissionType;
    String bodyType;
    Long mileage;
    List<String> vinProvidedFields;

}
