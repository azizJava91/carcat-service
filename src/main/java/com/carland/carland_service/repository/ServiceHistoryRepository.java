package com.carland.carland_service.repository;


import com.carland.carland_service.entity.Car;
import com.carland.carland_service.entity.ServiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceHistoryRepository extends JpaRepository<ServiceHistory, Long> {

    ServiceHistory findByServiceNameAndCar(String serviceName, Car car);

    @Query("SELECT sh FROM ServiceHistory sh " +
            "WHERE sh.serviceName = :serviceName AND sh.car = :car " +
            "ORDER BY sh.doneDate DESC NULLS LAST, sh.id DESC")
    Optional<ServiceHistory> findTopByServiceNameAndCarOrderByDoneDateDesc(@Param("serviceName") String serviceName,
                                                                           @Param("car") Car car);

    List<ServiceHistory> findAllByCar(Car car);
}
