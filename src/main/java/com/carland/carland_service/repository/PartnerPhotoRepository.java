package com.carland.carland_service.repository;

import com.carland.carland_service.entity.PartnerPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnerPhotoRepository extends JpaRepository<PartnerPhoto, Long> {

    PartnerPhoto findByPartnerId(Long partnerId);
}
