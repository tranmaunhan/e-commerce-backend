package com.tranmaunhan.example05.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tranmaunhan.example05.exceptions.UserNotFoundException;
import com.tranmaunhan.example05.payloads.LoginCredentials;
import com.tranmaunhan.example05.payloads.UserDTO;
import com.tranmaunhan.example05.security.JwtUtil;
import com.tranmaunhan.example05.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerHandler(
            @RequestBody UserDTO user) throws UserNotFoundException {

        // Encode password
        String encodedPass = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPass);

        // Save user
        UserDTO userDTO = userService.registerUser(user);

        // Generate token
        String token = jwtUtil.generateToken(userDTO.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("jwt-token", token);
        response.put("user", userDTO);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginHandler(
            @Valid @RequestBody LoginCredentials credentials)
            throws UserNotFoundException {

        // ===== FIX DỮ LIỆU ĐẦU VÀO =====
        String email = credentials.getEmail().trim().toLowerCase();
        String password = credentials.getPassword().trim();

        // ===== LOG DEBUG (CỰC QUAN TRỌNG) =====
        System.out.println("EMAIL = [" + email + "]");
        System.out.println("PASS  = [" + password + "]");
        System.out.println("LEN   = " + password.length());

        // ===== AUTHENTICATE =====
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);

        authenticationManager.authenticate(authToken);

        // ===== LẤY USER =====
        UserDTO userDTO = userService.getUserByEmail(email);
        System.out.println("USER = " + userDTO.getEmail());

        // ===== JWT =====
        String token = jwtUtil.generateToken(userDTO.getEmail());

        // ===== RESPONSE =====
        Map<String, Object> response = new HashMap<>();
        response.put("jwt-token", token);
        response.put("user", userDTO);

        return ResponseEntity.ok(response);
    }

}
