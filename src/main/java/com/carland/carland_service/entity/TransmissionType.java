package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "transmission_types")
public class TransmissionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long transmissionTypeId;
    String transmissionType;
    String status;

}
