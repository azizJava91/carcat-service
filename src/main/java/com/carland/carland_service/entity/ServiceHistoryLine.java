package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "service_history_lines")
public class ServiceHistoryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_history_id", nullable = false)
    @ToString.Exclude
    ServiceHistory serviceHistory;

    Integer serviceCode;
    Long universalServiceId;
    String serviceName;
    BigDecimal costAmount;
    String costCurrency;
    LocalDate nextServiceDate;
    Integer nextServiceMileage;
}
