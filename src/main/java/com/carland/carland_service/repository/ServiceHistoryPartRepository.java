package com.carland.carland_service.repository;

import com.carland.carland_service.entity.ServiceHistory;
import com.carland.carland_service.entity.ServiceHistoryPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceHistoryPartRepository extends JpaRepository<ServiceHistoryPart, Long> {
    List<ServiceHistoryPart> findAllByServiceHistory(ServiceHistory serviceHistory);
}
