package com.carland.carland_service.repository;

import com.carland.carland_service.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    DeviceToken findByUserId(Long userId);

    DeviceToken findByDeviceToken(String deviceToken);

    List<DeviceToken> findAllByUserIdIn(List<Long> customerIdList);

}
