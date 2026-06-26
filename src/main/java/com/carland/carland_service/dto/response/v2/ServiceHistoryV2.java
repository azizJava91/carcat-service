package com.carland.carland_service.dto.response.v2;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

    /** CarCat ServiceEntity id from Hyper. */
    private Long universalServiceId;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "visit_service_line_groups",
            joinColumns = @JoinColumn(name = "service_line_id")
    )
    @Column(name = "service_group")
    private List<String> serviceGroups = new ArrayList<>();

    @Column(precision = 12, scale = 2)
    private BigDecimal costAmount;

    private String costCurrency;

    private LocalDate nextServiceDate;

    private Integer nextServiceMileage;
}
