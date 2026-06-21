package com.carland.carland_service.repository;

import com.carland.carland_service.entity.MaintenanceTemplate;
import com.carland.carland_service.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {
    ServiceEntity findByServiceNameAndActionTypeAndIntervalKmAndIntervalMonthAndMaintenanceTemplate(String serviceName, String actionType, Long intervalKm, Integer intervalMonth, MaintenanceTemplate template);

    ServiceEntity findByServiceName(String serviceName);

    ServiceEntity findByServiceNameAndActionType(String serviceName, String actionType);

    List<ServiceEntity> findAllByMaintenanceTemplate(MaintenanceTemplate template);
}
