package com.carland.carland_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "maintenance_templates")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaintenanceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String brand;
    String model;
    Integer year;
    String engineType;
    String transmissionType;

    @OneToMany(mappedBy = "maintenanceTemplate",  fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    List<ServiceEntity> services = new ArrayList<>();

}
