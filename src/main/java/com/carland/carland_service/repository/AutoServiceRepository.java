package com.carland.carland_service.repository;

import com.carland.carland_service.entity.AutoService;
import com.carland.carland_service.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutoServiceRepository extends JpaRepository<AutoService, Long> {

    AutoService findBySuperAdmin(SuperAdmin superAdmin);


}
