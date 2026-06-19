package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "auto_services")
public class AutoService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    String address;
    String phoneNumber;
    String email;

    @OneToOne(mappedBy = "autoService", cascade = CascadeType.ALL)
    SuperAdmin superAdmin;

    @OneToMany(mappedBy = "autoService", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<Admin> admins = new ArrayList<>();
}
