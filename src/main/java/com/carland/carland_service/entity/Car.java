package com.carland.carland_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long carId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
    Customer customer;

    @Column(nullable = false, unique = true, length = 17)
    private String vin;
    private String plateNumber;
    private String brand;
    private String model;
    private Integer modelYear;
    private Long colorId;
    private String engineType;
    private Integer engineVolume;
    private String transmissionType;
    private String bodyType;
    private Long mileage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    @ToString.Exclude
    private MaintenanceTemplate maintenanceTemplate;

    @OneToMany(mappedBy = "car")
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    List<ServiceHistory> serviceHistoryList = new ArrayList<>();

    @OneToMany(mappedBy = "car")
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    List<CustomerServiceRecord> serviceRecordList = new ArrayList<>();
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vin_provided_fields")
    private List<String> vinProvidedFields;

    @PrePersist
    @PreUpdate
    void ensureVinProvidedFields() {
        if (vinProvidedFields == null) {
            vinProvidedFields = Collections.emptyList();
        }
    }

}

