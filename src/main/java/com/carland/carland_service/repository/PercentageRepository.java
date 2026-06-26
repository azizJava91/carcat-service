package com.carland.carland_service.repository;

import com.carland.carland_service.entity.Car;
import com.carland.carland_service.entity.Percentage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PercentageRepository extends JpaRepository<Percentage, Long> {


    List<Percentage> findAllByCarId(Long carId);




    Percentage findByServiceNameAndCarId(String serviceName, Long carId);

    Percentage findByServiceIdAndCarId(Long serviceId, Long carId);
}
