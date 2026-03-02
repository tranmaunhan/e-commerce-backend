package com.tranmaunhan.example05.service;

import org.springframework.security.core.userdetails.UserDetails;

import com.tranmaunhan.example05.payloads.UserDTO;
import com.tranmaunhan.example05.payloads.UserResponse;



public interface UserService {
    UserDTO registerUser(UserDTO userDTO);

    UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    UserDTO getUserByld(Long userId);

    UserDTO updateUser(Long userId, UserDTO userDTO);

    String deleteUser(Long userId);

    UserDetails loadUserByUsername(String email);

     UserDTO getUserByEmail(String email) ;
}