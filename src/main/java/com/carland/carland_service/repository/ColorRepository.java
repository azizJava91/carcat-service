package com.carland.carland_service.repository;

import com.carland.carland_service.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long> {

    List<Color> findAllByOrderByColorIdAsc();

    Color findByColorId(Long colorId);


}
