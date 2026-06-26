package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "service_histories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_service_histories_car_partner_record", columnNames = {"car_id", "partner_record_id"})
})
public class ServiceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Long partnerRecordId;
    String serviceName;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    List<String> actionType;
    LocalDate doneDate;
    Integer doneKm;
    String serviceCenter;
    @Column(name = "service_center_id")
    Long serviceCenterId;
    BigDecimal serviceAmount;
    String amountCurrency;
    String dealer;
    LocalDate nextServiceDate;
    Integer nextServiceMileage;
    String source;

    @OneToMany(mappedBy = "serviceHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    List<ServiceHistoryLine> lines = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    @ToString.Exclude
    Car car;

}
