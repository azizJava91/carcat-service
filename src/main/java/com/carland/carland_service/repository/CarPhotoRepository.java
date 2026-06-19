package com.carland.carland_service.repository;

import com.carland.carland_service.entity.CarPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarPhotoRepository extends JpaRepository<CarPhoto, Long> {


    CarPhoto findByCarId(Long carId);
}







