package com.tranmaunhan.example05.repository;

import com.tranmaunhan.example05.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

}