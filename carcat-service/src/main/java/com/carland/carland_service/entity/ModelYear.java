package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "model_years")
public class ModelYear {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long modelYearId;
    Integer modelYear;
    String status;

}
