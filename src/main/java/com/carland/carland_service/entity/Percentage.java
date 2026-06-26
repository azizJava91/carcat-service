package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "percentages")
public class Percentage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String serviceName;
    String actionType;

    String serviceNameRu;
    String serviceNameAz;

    Long intervalKm;
    Integer intervalMonth;

    LocalDate lastServiceDate;
    Integer lastServiceKm;

    LocalDate nextServiceDate;
    Integer nextServiceKm;

    Integer remainingKm;
    LocalDate remainingMonths;

    Integer kmPercentage;
    Integer monthPercentage;

    Long serviceId;

    @Column(nullable = false)
    @ColumnDefault("false")
    boolean important;

    /**
     * Added status field to control percentage handling.
     * If status is "edited", percentage values are returned directly in the response.
     * If status is "created", percentage values are recalculated and then added to the response.
     */
    String status;

    Long carId;

    LocalDateTime lastNotificationSentAt;


}
