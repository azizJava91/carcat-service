package com.carland.carland_service.repository;

import com.carland.carland_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByUserId(Long userId);

    Customer findByPhoneNumber(String phoneNumber);

    Customer findByUserIdAndPhoneNumberAndStatus(Long userId, String phoneNumber,  String status);

    Customer findByUserIdAndPhoneNumber(Long aLong, String phoneNumber);
}
