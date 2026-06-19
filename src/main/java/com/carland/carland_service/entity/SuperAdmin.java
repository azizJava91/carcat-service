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
@Table(name = "super_admins")
public class SuperAdmin {

    @Id
    Long userId;

    String phoneNumber;
    String name;
    String surname;
    String notificationLanguage;
    String status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_service_id", unique = true)
    AutoService autoService;
}

