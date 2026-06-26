package com.carland.carland_service.repository;

import com.carland.carland_service.dto.response.v2.Visit;
import com.carland.carland_service.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findAllByCarOrderByLastServiceDateDescIdDesc(Car car);

    Optional<Visit> findByCarAndHyperRecordId(Car car, Long hyperRecordId);
}
