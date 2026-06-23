package com.carland.carland_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarResponse {
    private Long carId;
    private Long customerId;
    private String vin;
    private String plateNumber;
    private String brand;
    private String model;
    private Integer modelYear;
    private String color;
    private String engineType;
    private Integer engineVolume;
    private String transmissionType;
    private Long mileage;
    private LocalDateTime updatedAt;
    private String bodyType;
    private String message;
    private List<String> vinProvidedFields;
    private String resource;
    private Long engineTypeId;
}
