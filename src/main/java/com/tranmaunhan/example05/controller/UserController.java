package com.tranmaunhan.example05.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tranmaunhan.example05.config.AppConstants;
import com.tranmaunhan.example05.payloads.UserDTO;
import com.tranmaunhan.example05.payloads.UserResponse;
import com.tranmaunhan.example05.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/admin/users")
    public ResponseEntity<UserResponse> getUsers(
        @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
        @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
        @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        
        UserResponse userResponse = userService.getAllUsers(
                pageNumber==0? pageNumber:pageNumber-1, pageSize,"id".equals( sortBy)?"userId":sortBy, sortOrder);
        
        return new ResponseEntity<UserResponse>(userResponse, HttpStatus.OK);
    }
    
    @GetMapping("/public/users/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        UserDTO user = userService.getUserByld(userId);
        
        return new ResponseEntity<UserDTO>(user, HttpStatus.OK);
    }

    @GetMapping("/public/users/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @PutMapping("/public/users/{userId}")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDto, @PathVariable Long userId) {

  UserDTO updateUser = userService.updateUser(userId, userDto);
        
        return new ResponseEntity<UserDTO>(updateUser, HttpStatus.OK);
    }
    
    @DeleteMapping("/admin/users/{userId}") 
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        String status = userService.deleteUser(userId);
        return new ResponseEntity<String>(status, HttpStatus.OK);
    }
}