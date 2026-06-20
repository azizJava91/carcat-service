package com.carland.carland_service.repository;

import com.carland.carland_service.entity.EngineType;
import com.carland.carland_service.entity.MaintenanceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaintenanceTemplateRepository extends JpaRepository<MaintenanceTemplate, Long> {
    MaintenanceTemplate findByBrandAndModelAndYearAndEngineTypeAndTransmissionType(
            String brand,
            String model,
            Integer year,
            String engineType,
            String transmissionType
    );

    Optional<MaintenanceTemplate> findByEngineType(EngineType engineType);
}
