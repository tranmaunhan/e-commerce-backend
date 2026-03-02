package com.tranmaunhan.example05.service.impl;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tranmaunhan.example05.entities.Address;
import com.tranmaunhan.example05.entities.Role;
import com.tranmaunhan.example05.entities.User;
import com.tranmaunhan.example05.entities.Cart;
import com.tranmaunhan.example05.exceptions.APIException;
import com.tranmaunhan.example05.exceptions.ResourceNotFoundException;
import com.tranmaunhan.example05.payloads.AddressDTO;
import com.tranmaunhan.example05.payloads.UserDTO;
import com.tranmaunhan.example05.payloads.UserResponse;
import com.tranmaunhan.example05.repository.AddressRepo;
import com.tranmaunhan.example05.repository.RoleRepo;
import com.tranmaunhan.example05.repository.UserRepo;
import com.tranmaunhan.example05.repository.CartRepo;
import com.tranmaunhan.example05.service.UserService;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class UserDetailsServiceImpl implements UserService,UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        try {
            User user = modelMapper.map(userDTO, User.class);

            Role role = roleRepo.findByRoleName("USER")
                    .orElseThrow(() -> new APIException("ROLE USER chưa tồn tại"));

            user.getRoles().add(role);

            if (userDTO.getAddress() == null) {
                throw new APIException("Address không được null khi đăng ký");
            }
            var addrDTO = userDTO.getAddress();

            Address address = addressRepo
                    .findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
                            addrDTO.getCountry(),
                            addrDTO.getState(),
                            addrDTO.getCity(),
                            addrDTO.getPincode(),
                            addrDTO.getStreet(),
                            addrDTO.getBuildingName()
                    );

            if (address == null) {
                address = new Address(
                        addrDTO.getCountry(),
                        addrDTO.getState(),
                        addrDTO.getCity(),
                        addrDTO.getPincode(),
                        addrDTO.getStreet(),
                        addrDTO.getBuildingName()
                );
                address = addressRepo.save(address);
            }

            user.setAddresses(List.of(address));
            User registeredUser = userRepo.save(user);
            if (role.getRoleName().equals("USER")) {
                System.out.println("User đăng ký với ROLE = " + role.getRoleName());

                Cart cart = new Cart();
                cart.setUser(registeredUser);
                cart.setTotalPrice(0.0);
                registeredUser.setCart(cart);
                cartRepo.save(cart);
            }
            UserDTO response = modelMapper.map(registeredUser, UserDTO.class);
            response.setAddress(
                    modelMapper.map(
                            registeredUser.getAddresses().get(0),
                            AddressDTO.class
                    )
            );
            return response;
        } catch (DataIntegrityViolationException e) {
            throw new APIException("User already exists with emailId: " + userDTO.getEmail());
        }
    }


  @Override
public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
    Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
    Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);
    Page<User> pageUsers = userRepo.findAll(pageDetails);
    List<User> users = pageUsers.getContent();

    if (users.size() == 0) {
        throw new APIException("No User exists !!!");
    }

    List<UserDTO> userDTOs = users.stream().map(user -> {
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        if (user.getAddresses().size() != 0) {
            dto.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
        }
        return dto;

    }).collect(Collectors.toList());
    UserResponse userResponse = new UserResponse();
    userResponse.setContent(userDTOs);
    userResponse.setPageNumber(pageUsers.getNumber());
    userResponse.setPageSize(pageUsers.getSize());
    userResponse.setTotalElements(pageUsers.getTotalElements());
    userResponse.setTotalPages(pageUsers.getTotalPages());
    userResponse.setLastPage(pageUsers.isLast());

    return userResponse;
}



    @Override
    public UserDTO getUserByld(Long userId) {
     User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
    UserDTO userDTO = modelMapper.map(user, UserDTO.class);
    userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
    return userDTO;
    }



    @Override
    public UserDTO getUserByEmail(String email) {
        Optional<User> optionalUser = userRepo.findByEmail(email);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        User user = optionalUser.get();
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getUserId());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setMobileNumber(user.getMobileNumber());


        return userDTO;
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
     User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

    String encodedPass = passwordEncoder.encode(userDTO.getPassword());

    user.setFirstName(userDTO.getFirstName());
    user.setLastName(userDTO.getLastName());
    user.setMobileNumber(userDTO.getMobileNumber());
    user.setEmail(userDTO.getEmail());
    user.setPassword(encodedPass);

    if (userDTO.getAddress() != null) {
        String country = userDTO.getAddress().getCountry();
        String state = userDTO.getAddress().getState();
        String city = userDTO.getAddress().getCity();
        String pincode = userDTO.getAddress().getPincode();
        String street = userDTO.getAddress().getStreet();
        String buildingName = userDTO.getAddress().getBuildingName();

        Address address = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(country, state, city, pincode, street, buildingName);

        if (address == null) {
            address = new Address(country, state, city, pincode, street, buildingName);
            address = addressRepo.save(address);
        }

        user.setAddresses(List.of(address));
    }

    userDTO = modelMapper.map(user, UserDTO.class);

    userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));

    // CartDTO cart = modelMapper.map(user.getCart(), CartDTO.class);

    // List<ProductDTO> products = user.getCart().getCartItems().stream()
    //         .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)).collect(Collectors.toList());

    // userDTO.setCart(cart);

    // userDTO.getCart().setProducts(products);

    return userDTO;
    }

    @Override
    public String deleteUser(Long userId) {
      User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

    // List<CartItem> cartItems = user.getCart().getCartItems();
    // Long cartId = user.getCart().getCartId();
    // cartItems.forEach(item -> {

    //     Long productId = item.getProduct().getProductId();
    //     cartService.deleteProductFromCart(cartId, productId);
    // });

    userRepo.delete(user);

    return "User with userId " + userId + " deleted successfully!!!";
    }
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
  @Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    // Lấy user theo email
    User appUser = userRepo.findByEmailWithRoles(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

    // Convert roles -> GrantedAuthority
    List<GrantedAuthority> authorities = appUser.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_"+role.getRoleName()))
            .collect(Collectors.toList());

      System.out.println("AUTH LOADED = " + authorities);

    // Trả về UserDetails của Spring Security
    return new org.springframework.security.core.userdetails.User(
            appUser.getEmail(),
            appUser.getPassword(),
            authorities
    );
}



}
