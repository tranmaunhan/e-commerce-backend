package com.tranmaunhan.example05.repository;

import java.util.List;
import java.util.Optional; // Cần import Optional

import com.tranmaunhan.example05.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {

    // 1. Tìm giỏ hàng theo Email và CartId
    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1 AND c.id = ?2")
    Cart findCartByEmailAndCartId(String email, Long cartId);

    // 2. Tìm giỏ hàng theo UserId (Dùng Query thủ công)
    @Query("SELECT c FROM Cart c WHERE c.user.userId = ?1")
    Cart findCartByUserId(Long userId);

    // 3. Tìm giỏ hàng theo UserId (Dùng Spring Data JPA tự động - Khuyên dùng)
    // Lưu ý: Tên phải khớp với thuộc tính trong Entity (ví dụ: c.user.userId)
    Optional<Cart> findByUser_UserId(Long userId);

    // 4. Tìm danh sách giỏ hàng có chứa sản phẩm cụ thể
    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p WHERE p.id = ?1")
    List<Cart> findCartsByProductId(Long productId);
}