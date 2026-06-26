package com.carland.carland_service.repository;

import com.carland.carland_service.entity.ServiceHistory;
import com.carland.carland_service.entity.ServiceHistoryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceHistoryLineRepository extends JpaRepository<ServiceHistoryLine, Long> {
    List<ServiceHistoryLine> findAllByServiceHistory(ServiceHistory serviceHistory);
    void deleteByServiceHistory(ServiceHistory serviceHistory);
}
