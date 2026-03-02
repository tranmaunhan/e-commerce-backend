package com.tranmaunhan.example05.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tranmaunhan.example05.entities.Address;
import com.tranmaunhan.example05.entities.User;
import com.tranmaunhan.example05.exceptions.APIException;
import com.tranmaunhan.example05.exceptions.ResourceNotFoundException;
import com.tranmaunhan.example05.payloads.AddressDTO;
import com.tranmaunhan.example05.repository.AddressRepo;
import com.tranmaunhan.example05.repository.UserRepo;
import com.tranmaunhan.example05.service.AddressService;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
       String country = addressDTO.getCountry();
        String state = addressDTO.getState();
        String city = addressDTO.getCity();
        String pincode = addressDTO.getPincode();
        String street = addressDTO.getStreet();
        String buildingName = addressDTO.getBuildingName();

        Address addressFromDB = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(country, state, city, pincode, street, buildingName);

        if (addressFromDB != null) {
            throw new APIException("Address already exists with addressId: " + addressFromDB.getAddressId());
        }
        Address address = modelMapper.map(addressDTO, Address.class);

        Address savedAddress = addressRepo.save(address);

        return modelMapper.map(savedAddress, AddressDTO.class);
    }

  
    @Override
    public AddressDTO updateAddress(Long addressId, Address address) {
     Address addressFromDB = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
            address.getCountry(), address.getState(), address.getCity(), address.getPincode(), address.getStreet(), address.getBuildingName());

// 75
// tranman@hitu.edu.vn

    if (addressFromDB == null) {
        addressFromDB = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        addressFromDB.setCountry(address.getCountry());
        addressFromDB.setState(address.getState());
        addressFromDB.setCity(address.getCity());
        addressFromDB.setPincode(address.getPincode());
        addressFromDB.setStreet(address.getStreet());
        addressFromDB.setBuildingName(address.getBuildingName());

        Address updatedAddress = addressRepo.save(addressFromDB);

        return modelMapper.map(updatedAddress, AddressDTO.class);
    } else {
        // Nếu địa chỉ mới (address) đã tồn tại trong DB (addressFromDB), 
        // thì ta chuyển các User đang dùng địa chỉ cũ (addressId) sang dùng địa chỉ mới (addressFromDB)
        List<User> users = userRepo.findByAddress(addressId);
        final Address a = addressFromDB; // Địa chỉ mới đã tồn tại

        // Lặp qua tất cả users đang dùng địa chỉ cũ và gán địa chỉ mới (a)
        users.forEach(user -> user.getAddresses().add(a)); 
        
        // Xóa địa chỉ cũ (addressId)
        deleteAddress(addressId);

        return modelMapper.map(addressFromDB, AddressDTO.class);
    }
    }

    @Override
    public String deleteAddress(Long addressId) {
      User user = userRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", addressId));

        // List<CartItem> cartItems = user.getCart().getCartItems();
        // Long cartId = user.getCart().getCartId();

        // cartItems.forEach(item -> {

        //     Long productId = item.getProduct().getProductId();
        //     cartService.deleteProductFromCart(cartId, productId);
        // });

        userRepo.delete(user);

        return "User with userId " + addressId + " deleted successfully!!!";
    }
    

    @Override
    public List<AddressDTO> getAddresses() {
     List<Address> addresses = addressRepo.findAll(); //

        List<AddressDTO> addressDTOs = addresses.stream()
            .map(address -> modelMapper.map(address, AddressDTO.class)) //
            .collect(Collectors.toList()); //

        return addressDTOs; //
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
      Address address = addressRepo.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));

        return modelMapper.map(address, AddressDTO.class);
    }

   

}