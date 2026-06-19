package com.carland.carland_service.repository;

import com.carland.carland_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByCustomerId(Long userId);

    List<Notification> findAllByCustomerIdAndStatus(Long userId, String status);
}
