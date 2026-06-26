package com.carland.carland_service.dto.response.v2;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "visit_parts")
@ToString(exclude = "visit")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServiceHistoryPartV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    private String name;

    private BigDecimal qty;

    private String unit;
}
