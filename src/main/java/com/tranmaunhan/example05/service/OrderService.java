package com.tranmaunhan.example05.service;

import com.tranmaunhan.example05.payloads.OrderDTO;
import com.tranmaunhan.example05.payloads.OrderResponse;

import java.util.List;



public interface OrderService {

    OrderDTO placeOrder(String emailId, Long cartId, String paymentMethod);

    OrderDTO getOrder(String emailId, Long orderId);

    List<OrderDTO> getOrdersByUser(String emailId);

     OrderDTO getOrderByAdmin(Long orderId);
    OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    OrderDTO updateOrder(String emailId, Long orderId, String orderStatus);
}