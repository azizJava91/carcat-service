package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "service_histories")
public class ServiceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String serviceName;   // Source: HyperResponse.serviceType
    List<String> actionType;  // Source: HyperResponse.serviceGroups
    LocalDate doneDate;   // Source: HyperResponse.lastServiceDate
    Integer doneKm;       // Source: HyperResponse.lastServiceMileage


    /**
     * The service center identifier.
     *
     * Currently, the system works only with the Hyper Service, so this field
     * is set to "Hyper Service" by default.
     *
     * In the future, this value will be made dynamic to support multiple service centers.
     */
    String serviceCenter;

    String dealer;
    BigDecimal serviceAmount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    @ToString.Exclude
    Car car;

}
