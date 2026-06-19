package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "ranges")
public class Range {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long rangeId;

    @Column(name = "start_time")
    OffsetDateTime start;

    @Column(name = "end_time")
    OffsetDateTime end;

    String status;
    Integer workerCount;


    @OneToMany
    @JoinColumn(name = "range_id")
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "calendar_id")
    Calendar calendar;
}

