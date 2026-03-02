package com.tranmaunhan.example05.controller;

import java.util.List;

import com.tranmaunhan.example05.payloads.CartDTO;
import com.tranmaunhan.example05.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/public/carts/{cartId}/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long cartId, @PathVariable Long productId,
            @PathVariable Integer quantity) {

        CartDTO cartDTO = cartService.addProductToCart(cartId, productId, quantity);

        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);
    }

    @GetMapping("/admin/carts")
    public ResponseEntity<List<CartDTO>> getCarts() {

        List<CartDTO> cartDTOs = cartService.getAllCarts();

        return new ResponseEntity<List<CartDTO>>(cartDTOs, HttpStatus.OK);
    }

    // get By UserId
    @GetMapping("/public/users/{userId}/cart")
    public ResponseEntity<CartDTO> getCartById(@PathVariable Long userId) {

        CartDTO cartDTO = cartService.getCart(userId);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @GetMapping("/admin/carts/{cartId}")
    public ResponseEntity<CartDTO> getCartByIdForAdmin(@PathVariable Long cartId) {

        CartDTO cartDTO = cartService.getCartById(cartId);

        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/public/carts/{cartId}/products/{productId}/quantity/{quantity}")
    // @PutMapping("/public/carts/{cartId}/products/{productId}/quantity/{quantity}")
    public ResponseEntity<Integer> updateQuantity(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @PathVariable Integer quantity) {
        Integer updatedQuantity = cartService.updateProductQuantityInCart(cartId, productId, quantity);

        return ResponseEntity.ok(updatedQuantity);
    }

    @DeleteMapping("/public/carts/{UserId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long UserId, @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(UserId, productId);

        return new ResponseEntity<String>(status, HttpStatus.OK);
    }

    // Xóa sản phẩm trong cart theo cartItemId
    @DeleteMapping("/public/carts/item/{cartItemId}")
    public ResponseEntity<String> deleteCartItem(@PathVariable Long cartItemId) {
        String status = cartService.deleteCartItem(cartItemId);
        System.out.println("oke");
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

}