package com.carland.carland_service.repository;

import com.carland.carland_service.dto.response.v2.Visit;
import com.carland.carland_service.entity.Car;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    /** Eager-fetch services; parts loaded via {@link BatchSize} (Hibernate cannot join-fetch two List bags). */
    @EntityGraph(attributePaths = {"services"})
    List<Visit> findAllByCarOrderByLastServiceDateDescIdDesc(Car car);

    Optional<Visit> findByCarAndHyperRecordId(Car car, Long hyperRecordId);
}
