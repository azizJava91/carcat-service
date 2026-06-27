package com.carland.carland_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "partner_photos")
public class PartnerPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long imageId;
    Long partnerId;
    String fileName;
    String fileType;

    @Lob
    @JdbcTypeCode(Types.BINARY)
    byte[] imageData;
}
