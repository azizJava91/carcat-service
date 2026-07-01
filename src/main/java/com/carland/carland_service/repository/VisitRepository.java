package com.carland.carland_service.repository;

import com.carland.carland_service.dto.response.v2.Visit;
import com.carland.carland_service.entity.Car;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    boolean existsByCar(Car car);

    /** Eager-fetch services; parts loaded via {@link org.hibernate.annotations.BatchSize} (Hibernate cannot join-fetch two List bags). */
    @EntityGraph(attributePaths = {"services"})
    List<Visit> findAllByCarOrderByLastServiceDateDescIdDesc(Car car);

    @Query("SELECT v.hyperRecordId FROM Visit v WHERE v.car = :car")
    Set<Long> findHyperRecordIdsByCar(@Param("car") Car car);

    Optional<Visit> findByCarAndHyperRecordId(Car car, Long hyperRecordId);

    @EntityGraph(attributePaths = {"services"})
    @Query("SELECT v FROM Visit v WHERE v.car.carId = :carId AND v.hyperRecordId = :hyperRecordId")
    Optional<Visit> findWithDetailsByCarIdAndHyperRecordId(@Param("carId") Long carId, @Param("hyperRecordId") Long hyperRecordId);

    @EntityGraph(attributePaths = {"services", "parts"})
    Optional<Visit> findWithDetailsByCarAndHyperRecordId(Car car, Long hyperRecordId);
}
