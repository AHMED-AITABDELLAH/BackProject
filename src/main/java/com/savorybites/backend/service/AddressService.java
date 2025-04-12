package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.address.AddressRegistrationBody;
import com.savorybites.backend.model.Address;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.repository.AddressRepository;
import com.savorybites.backend.repository.addressDetailRepository.CommuneRepository;
import com.savorybites.backend.repository.addressDetailRepository.ProvinceRepository;
import com.savorybites.backend.repository.addressDetailRepository.RegionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final ProvinceRepository provinceRepository;
    private final RegionRepository regionRepository;
    private final CommuneRepository communeRepository;
    private final UserService userService;

    public AddressService(AddressRepository addressRepository,
                          ProvinceRepository provinceRepository,
                          RegionRepository regionRepository,
                          CommuneRepository communeRepository, UserService userService)
    {
        this.addressRepository = addressRepository;
        this.provinceRepository = provinceRepository;
        this.regionRepository = regionRepository;
        this.communeRepository = communeRepository;
        this.userService = userService;
    }




    private void assignValuesToAddress(Address address,
                                       AddressRegistrationBody addressRegistrationBody,
                                       LocalUser managedUser)
    {
        // Set the Region
        address.setRegion(
                regionRepository
                        .findById(addressRegistrationBody.getRegionID())
                        .orElseThrow(() ->
                                new RuntimeException("Region not found with ID: " + addressRegistrationBody.getRegionID()))
        );

        // Set the Province
        address.setProvince(
                provinceRepository
                        .findByIdAndRegion(addressRegistrationBody.getProvinceID(),address.getRegion())
                        .orElseThrow(() ->
                                new RuntimeException("Province not found with ID: " + addressRegistrationBody.getProvinceID()))
        );

        // Set the Commune
        address.setCommune(
                communeRepository
                        .findByIdAndProvince(addressRegistrationBody.getCommuneID(),address.getProvince())
                        .orElseThrow(() ->
                                new RuntimeException("Commune not found with ID: " + addressRegistrationBody.getCommuneID()))
        );

        // Set the Street
        address.setStreet(addressRegistrationBody.getStreet());

        // If the new address is set to default, update the old default address (if it exists)
        if (addressRegistrationBody.isDefault()) {
            Optional<Address> opDefaultAddress = addressRepository.findByUserAndIsDefaultTrue(managedUser);
            opDefaultAddress.ifPresent
                    (
                            oldDefaultAddress -> {
                                oldDefaultAddress.setDefault(false);  // Set the old default address to false
                                addressRepository.save(oldDefaultAddress);// Save the updated old default address
                                addressRepository.flush();
                            }
                    );
        }

        // Set the current address default
        address.setDefault(addressRegistrationBody.isDefault());

        if(addressRegistrationBody.isCoordinationActivated()){
            address.setLatitude(addressRegistrationBody.getLatitude());
            address.setLongitude(addressRegistrationBody.getLongitude());
        }else{
            address.setLatitude(null);
            address.setLongitude(null);
        }

    }


    @Transactional
    public boolean addAddress(LocalUser user,
                              AddressRegistrationBody addressRegistrationBody)
    {
        // Retrieve managed user from the user service
        LocalUser managedUser = userService.getManagedUser(user);
        Address address = new Address();

        //assigning the values
        assignValuesToAddress(address,addressRegistrationBody,managedUser);
        // Link the address to the user
        address.setUser(managedUser);
        // Save the new address
        addressRepository.save(address);
        return true;
    }



    @Transactional
    public boolean updateAddress(LocalUser user,
                                 AddressRegistrationBody addressRegistrationBody,
                                 long addressID)
    {
        // Retrieve managed user from the user service
        LocalUser managedUser = userService.getManagedUser(user);
        Optional<Address> optionalAddress = addressRepository.findByUserAndId(managedUser,addressID);
        if(optionalAddress.isEmpty())
            throw new EntityNotFoundException();

        Address address=optionalAddress.get();

        //assigning the values
        assignValuesToAddress(address,addressRegistrationBody,managedUser);

        // Save the new address
        addressRepository.save(address);
        return true;
    }


    @Transactional
    public boolean deleteAddress(LocalUser user,
                                 long addressID)
    {
        // Retrieve managed user from the user service
        LocalUser managedUser = userService.getManagedUser(user);
        Optional<Address> optionalAddress = addressRepository.findByUserAndId(managedUser,addressID);
        if(optionalAddress.isEmpty())
            throw new EntityNotFoundException();
        //delete the address
        addressRepository.deleteById(addressID);
        return true;
    }

    public Address getAddress(LocalUser user,long addressID){
        Optional<Address> optionalAddress = addressRepository.findByUserAndId(user,addressID);
        return optionalAddress.orElse(null);
    }


    public List<Address> getAddresses(LocalUser user){
        List<Address> addressList = addressRepository.findAllByUser(user);
        if(addressList.isEmpty())
            return null;
        return addressList;
    }

    public boolean hasAddress(LocalUser user){
        List<Address> addressList=getAddresses(user);
        return addressList != null;
    }












}
