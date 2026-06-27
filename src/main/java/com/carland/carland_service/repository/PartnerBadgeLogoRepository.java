package com.carland.carland_service.repository;

import com.carland.carland_service.entity.PartnerBadgeLogo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerBadgeLogoRepository extends JpaRepository<PartnerBadgeLogo, Long> {

    PartnerBadgeLogo findByPartnerId(Long partnerId);
}
