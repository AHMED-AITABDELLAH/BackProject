package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.address.AddressRegistrationBody;
import com.savorybites.backend.model.Address;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.repository.AddressRepository;
import com.savorybites.backend.repository.addressDetailRepository.CommuneRepository;
import com.savorybites.backend.repository.addressDetailRepository.ProvinceRepository;
import com.savorybites.backend.repository.addressDetailRepository.RegionRepository;
import jakarta.persistence.EntityExistsException;
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
    private final RestaurantService restaurantService;

    public AddressService(AddressRepository addressRepository,
                          ProvinceRepository provinceRepository,
                          RegionRepository regionRepository,
                          CommuneRepository communeRepository,
                          UserService userService,
                          RestaurantService restaurantService)
    {
        this.addressRepository = addressRepository;
        this.provinceRepository = provinceRepository;
        this.regionRepository = regionRepository;
        this.communeRepository = communeRepository;
        this.userService = userService;
        this.restaurantService = restaurantService;
    }




    private void assignValuesToAddress(Address address,
                                       AddressRegistrationBody addressRegistrationBody,
                                       Object managedAccount)
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

        if(managedAccount instanceof LocalUser){
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
        } else if (managedAccount instanceof Restaurant) {
            // if an address exists
                Optional<Address> opAddress = addressRepository.findByRestaurant((Restaurant)managedAccount);
                if(opAddress.isEmpty()){
                    address.setIsDefault(true);
                }else throw new EntityExistsException("this restaurant already has an address");
        }

        if(addressRegistrationBody.isCoordinationActivated()){
            address.setLatitude(addressRegistrationBody.getLatitude());
            address.setLongitude(addressRegistrationBody.getLongitude());
        }else{
            address.setLatitude(null);
            address.setLongitude(null);
        }

    }


    @Transactional
    public boolean addAddress(Object managedAccount,
                              AddressRegistrationBody addressRegistrationBody)
    {
        Address address = new Address();
        if(managedAccount instanceof LocalUser){
            // Retrieve managed user from the user service
            LocalUser managedUser = userService.getManagedUser((LocalUser) managedAccount);
            //assigning the values
            assignValuesToAddress(address,addressRegistrationBody,managedUser);
            // Link the address to the user
            address.setUser(managedUser);

        } else if (managedAccount instanceof Restaurant) {
            // Retrieve managed user from the user service
            Restaurant managedRestaurant = restaurantService.getManagedRestaurant((Restaurant) managedAccount);
            //assigning the values
            assignValuesToAddress(address,addressRegistrationBody,managedRestaurant);
            // Link the address to the user
            address.setRestaurant(managedRestaurant);

        }
        // Save the new address
        addressRepository.save(address);
        return true;

    }



    @Transactional
    public boolean updateAddress(Object managedAccount,
                                 AddressRegistrationBody addressRegistrationBody,
                                 long addressID)
    {
        Optional<Address> optionalAddress;
        Address address = new Address();
        if(managedAccount instanceof LocalUser){
            // Retrieve managed user from the user service
            LocalUser managedUser = userService.getManagedUser((LocalUser) managedAccount);
            optionalAddress = addressRepository.findByUserAndId(managedUser,addressID);
            if(optionalAddress.isEmpty())
                throw new EntityNotFoundException();
            address=optionalAddress.get();

            //assigning the values
            assignValuesToAddress(address,addressRegistrationBody,managedUser);
        } else if (managedAccount instanceof Restaurant) {
            // Retrieve managed user from the user service
            Restaurant managedUser = restaurantService.getManagedRestaurant((Restaurant) managedAccount);
            optionalAddress = addressRepository.findByRestaurantAndId((Restaurant) managedAccount,addressID);
            if(optionalAddress.isEmpty())
                throw new EntityNotFoundException();
            address=optionalAddress.get();

            //assigning the values
            assignValuesToAddress(address,addressRegistrationBody,managedUser);
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
        } else if (managedAccount instanceof Restaurant) {
            // Retrieve managed Restaurant from the Restaurant service
            Restaurant managedRestaurant = restaurantService.getManagedRestaurant((Restaurant) managedAccount);
            optionalAddress = addressRepository.findByRestaurantAndId(managedRestaurant,addressID);
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

    public Address getRestaurantAddress(Restaurant restaurant)
    {
        Optional<Address> optionalAddress = addressRepository
                .findByRestaurant(restaurant);
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

}


