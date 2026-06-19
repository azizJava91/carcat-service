package com.carland.carland_service.repository;

import com.carland.carland_service.entity.EngineType;
import com.carland.carland_service.entity.ModelYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelYearRepository extends JpaRepository<ModelYear, Long> {


    List<ModelYear> findAllByStatus(String status);

    List<ModelYear> findAllByStatusOrderByModelYearDesc(String status);

}
