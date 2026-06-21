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
@Table(name = "engine_types")
public class EngineType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long engineTypeId;
    String engineType;
    String status;

}
