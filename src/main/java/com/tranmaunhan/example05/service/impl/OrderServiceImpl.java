package com.tranmaunhan.example05.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tranmaunhan.example05.entities.*;
import com.tranmaunhan.example05.entities.*;
import com.tranmaunhan.example05.exceptions.APIException;
import com.tranmaunhan.example05.exceptions.ResourceNotFoundException;
import com.tranmaunhan.example05.payloads.OrderDTO;
import com.tranmaunhan.example05.payloads.OrderItemDTO;
import com.tranmaunhan.example05.payloads.OrderResponse;
import com.tranmaunhan.example05.repository.*;
import com.tranmaunhan.example05.repository.*;
import com.tranmaunhan.example05.service.CartService;
import com.tranmaunhan.example05.service.OrderService;
import com.tranmaunhan.example05.service.UserService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    public UserRepo userRepo;

    @Autowired
    public CartRepo cartRepo;

    @Autowired
    public OrderRepo orderRepo;

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    public OrderItemRepo orderItemRepo;

    @Autowired
    public CartItemRepo cartItemRepo;

    @Autowired
    public ProductRepo productRepo;

    @Autowired
    public UserService userService;

    @Autowired
    public CartService cartService;

    @Autowired
    public ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long cartId, String paymentMethod) {

        Cart cart = cartRepo.findCartByEmailAndCartId(emailId, cartId);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        List<CartItem> cartItems = cart.getCartItems();

        if (cartItems == null || cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        // 1. Tạo Order
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted!");

        Order savedOrder = orderRepo.save(order);

        // 2. Payment
        Payment payment = new Payment();
        payment.setOrder(savedOrder);
        payment.setPaymentMethod(paymentMethod);
        payment = paymentRepo.save(payment);

        savedOrder.setPayment(payment);

        // 3. OrderItems + trừ kho
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            if (product.getQuantity() < quantity) {
                throw new APIException("Not enough stock for product: " + product.getProductName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);

            orderItems.add(orderItem);

            product.setQuantity(product.getQuantity() - quantity);
            productRepo.save(product);
        }

        orderItemRepo.saveAll(orderItems);

        // ✅ XÓA CART ITEMS ĐÚNG CÁCH
        for (CartItem item : cart.getCartItems()) {
            item.setCart(null);
        }
        cart.getCartItems().clear();

        cart.setTotalPrice(0.0);
        cartRepo.save(cart);

        // Map DTO
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderItems.forEach(item ->
                orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class))
        );

        return orderDTO;
    }



    @Override
    public List<OrderDTO> getOrdersByUser(String email) {
        List<Order> orders = orderRepo.findAllByEmail(email);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());

        if (orderDTOs.isEmpty()) {
            throw new APIException("No orders placed yet by the user with email: " + email);
        }

        return orderDTOs;
    }

    @Override
    public OrderDTO getOrder(String emailId, Long orderId) {

        Order order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId);

        if (order == null) {
            throw new ResourceNotFoundException("Order", "orderId", orderId);
        }

        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public OrderDTO getOrderByAdmin(Long orderId) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order", "orderId", orderId));

        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public OrderResponse getAllOrders(
            Integer pageNumber,
            Integer pageSize,
            String sortBy,
            String sortOrder
    ) {
        if ("id".equals(sortBy)) {
            sortBy = "orderId";
        }

        Sort sort = sortOrder.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Order> pageOrders = orderRepo.findAll(pageable);

        List<OrderDTO> orders = pageOrders.getContent()
                .stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();

        OrderResponse response = new OrderResponse();
        response.setContent(orders);
        response.setPageNumber(pageOrders.getNumber());
        response.setPageSize(pageOrders.getSize());
        response.setTotalElements(pageOrders.getTotalElements());
        response.setTotalPages(pageOrders.getTotalPages());
        response.setLastPage(pageOrders.isLast());

        return response;
    }

    @Override
    public OrderDTO updateOrder(String emailId, Long orderId, String orderStatus) {

        Order order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId);

        if (order == null) {
            throw new ResourceNotFoundException("Order", "orderId", orderId);
        }

        order.setOrderStatus(orderStatus);
        orderRepo.save(order);

        return modelMapper.map(order, OrderDTO.class);
    }
}
