package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

    String status; // status eklediim ki eger bu percentage "edited" edilmis ise o zaman direct percentage datalari response a eklensin, eger "created" ise mevcut hesaplanmaya tabih tutulsun ve o sekilde response eklensin

    Long carId;

    LocalDateTime lastNotificationSentAt;
}
