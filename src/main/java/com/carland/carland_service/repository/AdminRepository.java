package com.carland.carland_service.repository;

import com.carland.carland_service.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Admin findByUserId(Long userId);

    Admin findByPhoneNumber(String phoneNumber);

    Admin findByUserIdAndPhoneNumberAndStatus(Long userId, String phoneNumber, String status);
}
