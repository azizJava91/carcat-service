package com.carland.carland_service.repository;

import com.carland.carland_service.entity.Range;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RangeRepository extends JpaRepository<Range, Long> {
    Range findByRangeId(Long rangeId);


}
