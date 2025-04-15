package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.address.AddressRegistrationBody;
import com.savorybites.backend.model.Address;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.addressDetailModels.Commune;
import com.savorybites.backend.model.addressDetailModels.Province;
import com.savorybites.backend.model.addressDetailModels.Region;
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
                          CommuneRepository communeRepository,
                          UserService userService)
    {
        this.addressRepository = addressRepository;
        this.provinceRepository = provinceRepository;
        this.regionRepository = regionRepository;
        this.communeRepository = communeRepository;
        this.userService = userService;
    }




    private void assignValuesToAddress(Address address,
                                       AddressRegistrationBody addressRegistrationBody,
                                       Object managedAccount)
    {

        address.setTitle(addressRegistrationBody.getTitle());
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

        if(managedAccount instanceof LocalUser)
        {
            // If the new address is set to default, update the old default address (if it exists)
            if (addressRegistrationBody.isDefault()) {
                Optional<Address> opDefaultAddress = addressRepository.findByUserAndIsDefaultTrue((LocalUser)managedAccount);
                opDefaultAddress.ifPresent
                        (
                                oldDefaultAddress -> {
                                    oldDefaultAddress.setIsDefault(false);  // Set the old default address to false
                                    addressRepository.save(oldDefaultAddress);// Save the updated old default address
                                    addressRepository.flush();
                                }
                        );
                // Set the current address default
                address.setIsDefault(addressRegistrationBody.isDefault());
            }
        }
        else if (managedAccount instanceof Restaurant) {
            address.setIsDefault(true);
        }

        if(addressRegistrationBody.isCoordinationActivated()){
            address.setLatitude(addressRegistrationBody.getLatitude());
            address.setLongitude(addressRegistrationBody.getLongitude());
        }else{

            address.setLatitude(null);
            address.setLongitude(null);
        }
        address.setCoordinationActivated(addressRegistrationBody.isCoordinationActivated());

    }


    @Transactional
    public boolean addAddress(Object managedAccount,
                              AddressRegistrationBody addressRegistrationBody,
                              Address addr)
    {
        if(managedAccount instanceof LocalUser){
            // Retrieve managed user from the user service
            LocalUser managedUser = userService.getManagedUser((LocalUser) managedAccount);
            //assigning the values
            assignValuesToAddress(addr,addressRegistrationBody,managedUser);
            // Link the address to the user
            addr.setUser(managedUser);
            // Save the new address
            addressRepository.save(addr);
            return true;

        } else if (managedAccount instanceof Restaurant managedRestaurant) {
            //assigning the values
            assignValuesToAddress(addr,addressRegistrationBody,managedRestaurant);
            return true;
        }
        return false;



    }



    @Transactional
    public boolean updateAddress(Object managedAccount,
                                 AddressRegistrationBody addressRegistrationBody,
                                 long addressID)
    {
        Optional<Address> optionalAddress;
        Address address = new Address();
        if(managedAccount instanceof LocalUser)
        {
            // Retrieve managed user from the user service
            LocalUser managedUser = userService.getManagedUser((LocalUser) managedAccount);
            optionalAddress = addressRepository.findByUserAndId(managedUser,addressID);
            if(optionalAddress.isEmpty())
                throw new EntityNotFoundException();
            address=optionalAddress.get();

            //assigning the values
            assignValuesToAddress(address,addressRegistrationBody,managedUser);
        }
        else if (managedAccount instanceof Restaurant managedRestaurant) {

            optionalAddress = addressRepository.findById(addressID);
            if(optionalAddress.isEmpty())
                throw new EntityNotFoundException();
            address=optionalAddress.get();
            System.out.println("we got the address");
            //assigning the values
            assignValuesToAddress(address,addressRegistrationBody, managedRestaurant);
        }


        // Save the new address
        addressRepository.save(address);
        return true;
    }


    @Transactional
    public boolean deleteAddress(Object managedAccount,
                                 long addressID)

    {
        Optional<Address> optionalAddress = Optional.empty();
        if(managedAccount instanceof  LocalUser){
            // Retrieve managed user from the user service
            LocalUser managedUser = userService.getManagedUser((LocalUser) managedAccount);
            optionalAddress = addressRepository.findByUserAndId(managedUser,addressID);
        }
        if(optionalAddress.isEmpty())
            throw new EntityNotFoundException();

        //delete the address
        addressRepository.deleteById(addressID);
        return true;
    }

    public Address getAddress(LocalUser user,long addressID)
    {
        Optional<Address> optionalAddress = addressRepository
                    .findByUserAndId(user,addressID);
        return optionalAddress.orElse(null);
    }

    public Address getDefaultAddress(LocalUser user){
        Optional<Address> optionalAddress = addressRepository
                .findByUserAndIsDefaultTrue(user);
        return optionalAddress.orElse(null);
    }


    public List<Address> getAddresses(LocalUser user)
    {
        List<Address> addressList = addressRepository.findAllByUser(user);
        if(addressList.isEmpty())
            return null;
        return addressList;
    }

    public boolean hasAddress(Object managedAccount)
    {
        if(managedAccount instanceof LocalUser){
            List<Address> addressList=getAddresses((LocalUser) managedAccount);
            return addressList != null;
        } else if (managedAccount instanceof Restaurant)
            return addressRepository.findByRestaurant((Restaurant)managedAccount).isPresent();
        return false;
    }

    public List<Region> getRegions(){
        return regionRepository.findAll();
    }

    public List<Province> getProvinces(long regionID){
        return provinceRepository.findAllByRegion_Id(regionID);
    }

    public List<Commune> getCommunes(long provinceID){
        return communeRepository.findAllByProvince_Id(provinceID);
    }
}


