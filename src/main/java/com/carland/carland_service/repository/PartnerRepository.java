package com.carland.carland_service.repository;

import com.carland.carland_service.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partner, Long> {

    Optional<Partner> findByNameIgnoreCaseAndSourceIgnoreCase(String name, String source);

    Optional<Partner> findBySourceIgnoreCaseAndActiveTrue(String source);
}
