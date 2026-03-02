package com.tranmaunhan.example05.service;

import java.util.List;

import com.tranmaunhan.example05.entities.Address;
import com.tranmaunhan.example05.payloads.AddressDTO;


public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO);

    List<AddressDTO> getAddresses();

    AddressDTO getAddressById(Long addressId);
    
    AddressDTO updateAddress(Long addressId, Address address);

    String deleteAddress(Long addressId);
}