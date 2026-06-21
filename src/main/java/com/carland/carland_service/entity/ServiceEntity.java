package com.carland.carland_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "services")
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String serviceName;//    service_item_id

    String actionType;//    category

    String nameAz;//    name_az

    String nameEn;//    name_en

    String nameRu;//    name_ru

    Long intervalKm;//    standard interval km

    Integer intervalMonth;//    standard interval time

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    MaintenanceTemplate maintenanceTemplate;
}
