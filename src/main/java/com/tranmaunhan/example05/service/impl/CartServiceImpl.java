package com.tranmaunhan.example05.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.tranmaunhan.example05.entities.Cart;
import com.tranmaunhan.example05.entities.CartItem;
import com.tranmaunhan.example05.entities.Product;
import com.tranmaunhan.example05.exceptions.APIException;
import com.tranmaunhan.example05.exceptions.ResourceNotFoundException;
import com.tranmaunhan.example05.payloads.CartDTO;
import com.tranmaunhan.example05.payloads.ProductDTO;
import com.tranmaunhan.example05.repository.CartItemRepo;
import com.tranmaunhan.example05.repository.CartRepo;
import com.tranmaunhan.example05.repository.ProductRepo;
import com.tranmaunhan.example05.service.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CartItemRepo cartItemRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override

    public CartDTO addProductToCart(Long cartId, Long productId, Integer quantity) {

        // 1. Lấy cart
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        // 2. Lấy product
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // 3. Kiểm tra tồn kho
        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException(
                    "Only " + product.getQuantity() + " items left in stock");
        }

        // 4. Kiểm tra cart đã có product chưa
        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

        // =========================
        // CASE 1: ĐÃ CÓ → CỘNG THÊM
        // =========================
        if (cartItem != null) {

            int newQuantity = cartItem.getQuantity() + quantity;

            if (product.getQuantity() < quantity) {
                throw new APIException("Not enough stock");
            }

            // cập nhật cart item
            cartItem.setQuantity(newQuantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());

            cartItemRepo.save(cartItem);

        }
        // =========================
        // CASE 2: CHƯA CÓ → THÊM MỚI
        // =========================
        else {
            CartItem newCartItem = new CartItem();

            newCartItem.setCart(cart);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(quantity);
            newCartItem.setProductPrice(product.getSpecialPrice());
            newCartItem.setDiscount(product.getDiscount());

            cartItemRepo.save(newCartItem);
        }

        // 5. Trừ tồn kho
        product.setQuantity(product.getQuantity() - quantity);
        productRepo.save(product);

        // 6. Cập nhật totalPrice
        cart.setTotalPrice(
                cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepo.save(cart);

        // 7. Build CartDTO
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cartDTO.setEmail(cart.getUser().getEmail());

        List<ProductDTO> products = cart.getCartItems().stream()
                .map(cartItemEntity -> {
                    ProductDTO productDTO = modelMapper.map(cartItemEntity.getProduct(), ProductDTO.class);

                    // ⚠️ QUAN TRỌNG: quantity từ cart_item
                    productDTO.setQuantity(cartItemEntity.getQuantity());
                    return productDTO;
                })
                .collect(Collectors.toList());

        cartDTO.setProducts(products);

        return cartDTO;
    }

    @Override
    public CartDTO getCartById(Long cartId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cartDTO.setEmail(cart.getUser().getEmail());
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                .collect(Collectors.toList());
        cartDTO.setProducts(products);
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepo.findAll();

        if (carts.size() == 0) {
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            cartDTO.setEmail(cart.getUser().getEmail());
            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;
        }).collect(Collectors.toList());

        return cartDTOs;
    }

    @Override

    public CartDTO getCart(Long userId) {
        Cart cart = cartRepo.findCartByUserId(userId);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", userId);
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> products = cart.getCartItems().stream()
                .map(cartItem -> {
                    ProductDTO productDTO = modelMapper.map(
                            cartItem.getProduct(),
                            ProductDTO.class);

                    // quantity lấy từ cart_item
                    productDTO.setQuantity(cartItem.getQuantity());

                    // 🔥 thêm cartItemId
                    productDTO.setCartItemId(cartItem.getCartItemId());

                    return productDTO;
                })
                .collect(Collectors.toList());

        cartDTO.setProducts(products);

        return cartDTO;
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepo.save(cartItem);
    }

    @Override
    public Integer updateProductQuantityInCart(Long cartId, Long productId, Integer quantity) {

        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product not available in cart");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Not enough stock");
        }

        // cập nhật tồn kho
        product.setQuantity(
                product.getQuantity() + cartItem.getQuantity() - quantity);

        // cập nhật cart item
        cartItem.setQuantity(quantity);
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());

        cartItemRepo.save(cartItem);
        productRepo.save(product);

        // 👉 CHỈ TRẢ VỀ quantity mới
        return cartItem.getQuantity();
    }

    @Override
    @Transactional
    public String deleteProductFromCart(Long userId, Long productId) {

        Cart cart = cartRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        cartItemRepo.deleteByCartIdAndProductId(cart.getCartId(), productId);

        return "Product removed from the cart for User ID: " + userId;
    }




    @Override
    @Transactional
    public String deleteCartItem(Long cartItemId) {

        CartItem cartItem = cartItemRepo.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("CartItem not found"));

        Cart cart = cartItem.getCart();
        // System.out.println(cart);

        double itemTotal = cartItem.getProductPrice() * cartItem.getQuantity();
        cart.setTotalPrice(cart.getTotalPrice() - itemTotal);

        Product product = cartItem.getProduct();
        product.setQuantity(product.getQuantity() + cartItem.getQuantity());

        // Quan trọng
        cart.getCartItems().remove(cartItem);

        cartItemRepo.delete(cartItem);
        cartItemRepo.flush();

        return "Deleted successfully";
    }

}