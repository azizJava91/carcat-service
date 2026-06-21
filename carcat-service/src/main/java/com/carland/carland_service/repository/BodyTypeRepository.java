package com.carland.carland_service.repository;

import com.carland.carland_service.entity.BodyType;
import com.carland.carland_service.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BodyTypeRepository extends JpaRepository<BodyType, Long> {



    List<BodyType> findAllByStatusOrderByBodyTypeIdAsc(String status);
}
