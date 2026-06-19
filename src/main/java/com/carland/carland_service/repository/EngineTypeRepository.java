package com.carland.carland_service.repository;

import com.carland.carland_service.entity.BodyType;
import com.carland.carland_service.entity.EngineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EngineTypeRepository extends JpaRepository<EngineType, Long> {



    List<EngineType> findAllByStatusOrderByEngineTypeIdAsc(String status);
}
