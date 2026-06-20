package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "service_history_parts")
public class ServiceHistoryPart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_history_id", nullable = false)
    @ToString.Exclude
    ServiceHistory serviceHistory;

    String name;
    BigDecimal qty;
    String unit;
    BigDecimal cost;
    BigDecimal finalCost;
    BigDecimal discount;
}
