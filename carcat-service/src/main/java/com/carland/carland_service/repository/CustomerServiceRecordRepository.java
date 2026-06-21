package com.carland.carland_service.repository;

import com.carland.carland_service.entity.Car;
import com.carland.carland_service.entity.CustomerServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerServiceRecordRepository extends JpaRepository<CustomerServiceRecord,Long> {
    CustomerServiceRecord findByServiceNameAndCar(String serviceName, Car car);

    CustomerServiceRecord findByIdAndCar(Long recordId, Car car);

    CustomerServiceRecord findByServiceNameAndActionTypeAndCar(String serviceName, String actionType, Car car);


    List<CustomerServiceRecord> findAllByCar(Car car);


}
