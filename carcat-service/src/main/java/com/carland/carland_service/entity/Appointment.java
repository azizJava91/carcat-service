package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;


@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @Column(name = "appointment_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    OffsetDateTime appointmentDate;
    OffsetDateTime appointmentStart;
    OffsetDateTime appointmentEnd;

    String status;
    String serviceName;
    String actionType;
    String serviceCategory;
    @ManyToOne
    @JoinColumn(name = "auto_service_id", nullable = false)
    AutoService autoService;


    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    Customer customer;

    @ManyToOne
    @JoinColumn(name = "range_id")
    private Range range;

}

