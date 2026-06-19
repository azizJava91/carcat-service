package com.carland.carland_service.repository;

import com.carland.carland_service.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {
    SuperAdmin findByUserId(Long userId);

    SuperAdmin findByPhoneNumber(String phoneNumber);

    SuperAdmin findByUserIdAndPhoneNumberAndStatus(Long userId, String phoneNumber, String status);
}
