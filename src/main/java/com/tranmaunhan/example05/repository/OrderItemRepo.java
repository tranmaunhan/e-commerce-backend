package com.tranmaunhan.example05.repository;

import com.tranmaunhan.example05.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

}