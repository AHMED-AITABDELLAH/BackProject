package com.savorybites.backend.service;
import com.savorybites.backend.api.dto.auth.LoginBody;
import com.savorybites.backend.api.dto.restaurant.RestaurantDetailsBody;
import com.savorybites.backend.exception.user.*;
import com.savorybites.backend.model.Address;
import com.savorybites.backend.model.Role;
import com.savorybites.backend.model.Status;
import com.savorybites.backend.repository.VerificationRepository.PasswordResetTokenRepository;
import com.savorybites.backend.model.verificationModels.PasswordResetToken;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.repository.VerificationRepository.VerificationTokenRepository;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Service;
import com.savorybites.backend.api.dto.auth.RestaurantRegistrationBody;
import com.savorybites.backend.model.Restaurant;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RestaurantService {


    private final RestaurantRepository restaurantRepository;
    private final EncryptionService encryptionService;
    private final EmailService emailService;
    private final JWTService jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AddressService addressService;
    private final RestaurantOnboardingService restaurantOnboardingService;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            EncryptionService encryptionService,
            EmailService emailService,
            JWTService jwtService,
            VerificationTokenRepository verificationTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository,
            AddressService addressService, RestaurantOnboardingService restaurantOnboardingService) {
        this.restaurantRepository = restaurantRepository;
        this.encryptionService = encryptionService;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.addressService = addressService;
        this.restaurantOnboardingService = restaurantOnboardingService;
    }


    public ResponseEntity<?> registerRestaurant(RestaurantRegistrationBody body)
            throws UserAlreadyExistsException, StripeException {

        if (restaurantRepository.findByEmailIgnoreCase(body.getEmail()).isPresent())
            throw new UserAlreadyExistsException();

        // Step 1: Create the restaurant object
        Restaurant restaurant = new Restaurant();
        restaurant.setRole(Role.ROLE_RESTAURANT);
        restaurant.setTitle(body.getRestaurantName());
        restaurant.setEmail(body.getEmail());
        restaurant.setPhone(body.getPhone());
        restaurant.setContactEmail(body.getContactEmail());
        restaurant.setPassword(encryptionService.encryptPassword(body.getPassword()));
        restaurant.setShippingFees(body.getShippingFees());
        restaurant.setIsApproved(false);
        restaurant.setIsVerified(false);
        restaurant.setStatus(Status.PENDING_APPROVAL);


        // Step 2: Create the address and link both ways
        Address address = new Address();
        addressService.addAddress(restaurant,body.getAddressRegistrationBody(),address);
        address.setRestaurant(restaurant);         // unidirectional link
        restaurant.setAddress(address);            //sets FK reference



        // Step 3: send wait for approval email
        emailService.sendAccountIsWaitingApproval(restaurant);

        // Step 4: Save the restaurant (cascade saves address)
        restaurantRepository.save(restaurant);


        return ResponseEntity.ok("Restaurant account registred successfully");


    }

    public VerificationToken createVerificationToken(Restaurant restaurant) {
        VerificationToken token = new VerificationToken();
        token.setToken(jwtService.generateVerificationJWT(restaurant)); // Overload if needed
        token.setExpiryDate(Instant.now().plusSeconds(3600));
        token.setRestaurant(restaurant); // New field
        return token;
    }

    public String loginRestaurant(LoginBody loginBody)
            throws UserNotFoundException, PasswordIncorrectException, UserNotVerified {

        Restaurant restaurant = restaurantRepository
                .findByEmailIgnoreCase(loginBody.getEmail())
                .orElseThrow(UserNotFoundException::new);

        if(restaurant.getStatus()==Status.BANNED || restaurant.getStatus()==Status.DECLINED)
            throw new RequestRejectedException("You are not allowed on the platform!");

        if (restaurant.getIsApproved()) {
            if(!restaurant.getIsVerified()) {
                //We get all the VerificationTokens associated with this user
                List<VerificationToken> verificationTokens = restaurant.getVerificationTokens();
                //We check if the user has zero verification tokens or expired one
                if(
                        verificationTokens.isEmpty()
                                ||
                                verificationTokens.get(verificationTokens.size()-1)
                                        .getExpiryDate()
                                        .isBefore(Instant.now())
                )
                //We create a new Verification Token we save it in the DB
                {
                    VerificationToken verificationToken = createVerificationToken(restaurant);
                    verificationTokenRepository.save(verificationToken);
                    // resend an emailMessage with the VerificationToken to the user
                    emailService.sendRestaurantVerificationEmail(verificationToken);
                }
                throw new UserNotVerified();
            }
        }
        else throw new UserNotApproved();

        if (!encryptionService.verifyPassword(loginBody.getPassword(), restaurant.getPassword()))
            throw new PasswordIncorrectException();

        return jwtService.generateToken(restaurant); // Overload if needed
    }


    /// ////////////////////////////Generating Password Reset Token //////////////////////////////
    private PasswordResetToken createPasswordResetToken(Restaurant restaurant){
        Optional<PasswordResetToken> opToken =passwordResetTokenRepository.findFirstByRestaurantOrderByExpiryDateDesc(restaurant);
        if(opToken.isPresent()){
            PasswordResetToken passToken = opToken.get();
            if(passToken.getExpiryDate().isAfter(Instant.now()))
                //we return null because there is a valid token
                // (should wait for the expiry to retry)
                return null;
            passwordResetTokenRepository.delete(passToken);
        }
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(jwtService.generatePasswordResetJWT(restaurant));
        passwordResetToken.setExpiryDate(Instant.now().plusSeconds(15 * 60));
        passwordResetToken.setRestaurant(restaurant);
        passwordResetTokenRepository.save(passwordResetToken);
        return passwordResetToken;

    }
    /// //////////////////////////forgot Password/////////////////////////////////
    public void forgotPassword(String email)
            throws UserNotVerified, UserNotFoundException, EmailAlreadySentException {

        Restaurant restaurant = restaurantRepository.findByEmailIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new); // Throw if user doesn't exist

        if (!restaurant.getIsVerified()) {
            throw new UserNotVerified(); // Ensure user is verified
        }

        PasswordResetToken token = createPasswordResetToken(restaurant);
        if (token == null) {
            throw new EmailAlreadySentException(); // Token already exists and is valid
        }

        emailService.sendPasswordResetEmailForRestaurant(token);
    }


    public ResponseEntity<Object> update(Restaurant restaurant, RestaurantDetailsBody restaurantDetailsBody) {

        restaurant.setProfileImg(restaurantDetailsBody.getProfileImg());
        restaurant.setProfileBanner(restaurantDetailsBody.getProfileBanner());
        restaurant.setTitle(restaurantDetailsBody.getTitle());
        restaurant.setDescription(restaurantDetailsBody.getDescription());
        restaurant.setPhone(restaurantDetailsBody.getPhone());
        restaurant.setShippingFees(restaurantDetailsBody.getShippingFees());

        restaurant.setContactEmail(restaurantDetailsBody.getContactEmail());

        Optional<Restaurant> optionalRestaurant =restaurantRepository.findByEmailIgnoreCase(restaurant.getEmail());

        if(
                optionalRestaurant.isPresent()
                &&
                !Objects.equals(optionalRestaurant.get().getEmail(), restaurantDetailsBody.getEmail())
        )
            throw new UserAlreadyExistsException();

        restaurant.setEmail(restaurantDetailsBody.getEmail());



        if(!restaurantDetailsBody.getOldPassword().isBlank() && !restaurantDetailsBody.getNewPassword().isBlank()){

            if(!encryptionService.verifyPassword(restaurantDetailsBody.getOldPassword(),restaurant.getPassword()))
                throw new PasswordIncorrectException();

            restaurant.setPassword(encryptionService.encryptPassword(restaurantDetailsBody.getNewPassword()));

        }

        restaurantRepository.save(restaurant);

        return ResponseEntity.ok("Your Account Details Have Been Updated Successfully");




    }

    public Restaurant getManagedRestaurant(Restaurant restaurant){
        Optional<Restaurant> optionalRestaurant=restaurantRepository.findRestaurantById(restaurant.getId());
        if(optionalRestaurant.isPresent())
            return optionalRestaurant.get();
        //TODO idk
        throw new UserNotFoundException();
    }





    public List<Restaurant> getRestaurants(){
        return restaurantRepository.findAllAvailableRestaurants();
    }

    public List<Restaurant> getPopularRestaurants(){
        List<Restaurant> restaurantList =restaurantRepository.findAllRestaurantsOrderBySales();
        if (restaurantList.isEmpty())
            return null;
        Collections.shuffle(restaurantList);
        return restaurantList
                .stream()
                .limit(5)
                .collect(Collectors.toList());
    }


    public Restaurant getRestaurantProfile(long restaurantID){
        return restaurantRepository.findRestaurantById(restaurantID).orElseThrow();
    }


    public Restaurant getRestaurantsById(long restaurantID) {
        return restaurantRepository.findById(restaurantID).orElseThrow();
    }

    @Transactional
    public void setSetupCompleted(Restaurant restaurant){
        Restaurant managedRestaurant = getManagedRestaurant(restaurant);
        managedRestaurant.setIsSetupComplete(true);
        restaurantRepository.save(managedRestaurant);
    }
}
