package com.carland.carland_service.dto.response.v2;

import com.carland.carland_service.entity.Car;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "visits", uniqueConstraints = {
        @UniqueConstraint(name = "uk_visits_car_hyper_record", columnNames = {"car_id", "hyper_record_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"car", "services", "parts"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "hyper_record_id", nullable = false)
    private Long hyperRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    private String serviceType;

    private LocalDate lastServiceDate;

    private Integer lastServiceMileage;

    private String invoiceNumber;

    private String dealer;

    /** {@link com.carland.carland_service.enums.EnumPartnerId} — references {@code partners.id}. */
    private Long serviceCenterId;

    /** Denormalized partner display name at write time. */
    private String serviceCenterName;

    @Column(precision = 12, scale = 2)
    private BigDecimal costAmount;

    private String costCurrency;

    @Column(precision = 12, scale = 2)
    private BigDecimal finalCostAmount;

    private String finalCostCurrency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "service_groups", columnDefinition = "jsonb")
    private List<String> serviceGroups;

    @Builder.Default
    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ServiceHistoryV2> services = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 32)
    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ServiceHistoryPartV2> parts = new ArrayList<>();

    public void addService(ServiceHistoryV2 service) {
        services.add(service);
        service.setVisit(this);
    }

    public void addPart(ServiceHistoryPartV2 part) {
        parts.add(part);
        part.setVisit(this);
    }
}
