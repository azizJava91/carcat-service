package com.carland.carland_service.dto.response.v2;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "visit_service_lines")
@ToString(exclude = "visit")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServiceHistoryV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    private Integer serviceCode;

    private String serviceName;

    /** Hyper universalServiceId, stored raw exactly as Hyper sends it. */
    private String universalServiceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "service_groups", columnDefinition = "jsonb")
    private List<String> serviceGroups;

    @Column(precision = 12, scale = 2)
    private BigDecimal costAmount;

    private String costCurrency;

    private LocalDate nextServiceDate;

    private Integer nextServiceMileage;
}
