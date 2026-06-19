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
@Table(name = "admins")
public class Admin {

    @Id
    Long userId;

    String phoneNumber;
    String name;
    String surname;
    String notificationLanguage;
    String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_service_id", nullable = false)
    AutoService autoService;
}

