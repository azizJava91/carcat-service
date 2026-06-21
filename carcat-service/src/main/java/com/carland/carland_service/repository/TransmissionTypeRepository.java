package com.carland.carland_service.repository;

import com.carland.carland_service.entity.BodyType;
import com.carland.carland_service.entity.TransmissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransmissionTypeRepository extends JpaRepository<TransmissionType, Long> {


    List<TransmissionType> findAllByStatus(String status);
}
