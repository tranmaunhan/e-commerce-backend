package com.tranmaunhan.example05.service;

import com.tranmaunhan.example05.payloads.CartDTO;

import java.util.List;



public interface CartService {

    CartDTO addProductToCart(Long cartId, Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCart( Long userId);

    Integer updateProductQuantityInCart(Long cartId, Long productId, Integer quantity);

    void updateProductInCarts(Long cartId, Long productId);
    CartDTO getCartById(Long cartId);
    String deleteProductFromCart(Long cartId, Long productId);
    String deleteCartItem(Long cartItemId);
}