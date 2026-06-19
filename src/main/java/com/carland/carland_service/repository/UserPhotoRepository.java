package com.carland.carland_service.repository;


import com.carland.carland_service.entity.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {

    UserPhoto findByUserIdAndUserPhoneNumber(Long userId, String phoneNumber);



}







