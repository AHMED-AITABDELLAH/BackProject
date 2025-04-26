package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.auth.LoginBody;
import com.savorybites.backend.api.dto.user.UserDetailsBody;
import com.savorybites.backend.api.dto.auth.UserRegistrationBody;
import com.savorybites.backend.exception.user.*;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.Role;
import com.savorybites.backend.model.Status;
import com.savorybites.backend.model.verificationModels.PasswordResetToken;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import com.savorybites.backend.repository.VerificationRepository.PasswordResetTokenRepository;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.repository.VerificationRepository.VerificationTokenRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RestaurantRepository restaurantRepository;
    private final CartService cartService;
    private final StripeService stripeService;

    public UserService(UserRepository userRepository,
                       EncryptionService encryptionService,
                       JWTService jwtService,
                       EmailService emailService,
                       VerificationTokenRepository verificationTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       RestaurantRepository restaurantRepository,
                       CartService cartService, StripeService stripeService) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.restaurantRepository = restaurantRepository;
        this.cartService = cartService;
        this.stripeService = stripeService;
    }

    /// ////////////////////////////////////////////SignUP-Registration//////////////////////////////////////
    @Transactional
    public void Registration(UserRegistrationBody userRegistrationBody)
            throws UserAlreadyExistsException{

        if (
                (userRepository
                        .findByEmailIgnoreCase(userRegistrationBody.getEmail()))
                        .isPresent()
        )
            throw new UserAlreadyExistsException();


        LocalUser newUser = new LocalUser();
        newUser.setRole(Role.ROLE_USER);
        newUser.setUsername(userRegistrationBody.getUsername());
        newUser.setEmail(userRegistrationBody.getEmail());
        newUser.setIsVerified(false);
        newUser.setStatus(Status.UNVERIFIED);

        //Encrypting the password before storing it
        newUser.setPassword(encryptionService.encryptPassword(userRegistrationBody.getPassword()));

        //Here we delete old verification token
        verificationTokenRepository.deleteByExpiryDateBefore(Instant.now());
        //here we send a verification email to the user to confirm it
        VerificationToken verificationToken = createVerificationToken(newUser);
        emailService.sendVerificationEmail(verificationToken);


        userRepository.save(newUser);
        //Create Cart associated with the user
        cartService.createCart(newUser);
        verificationTokenRepository.save(verificationToken);
    }

    /// //////////Generating verification JWT upon Registering/Login////////////////////////
    ///
    private VerificationToken createVerificationToken(LocalUser user){
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setExpiryDate(Instant.now().plusSeconds(3600));
        verificationToken.setUser(user);
        return verificationToken;
    }

    /// ////////////////////////////Generating  Password Reset Token //////////////////////////////
    ///
    private PasswordResetToken createPasswordResetToken(LocalUser user){
        Optional<PasswordResetToken> opToken =passwordResetTokenRepository.findFirstByUserOrderByExpiryDateDesc(user);
        if(opToken.isPresent()){
            PasswordResetToken passToken = opToken.get();
            if(passToken.getExpiryDate().isAfter(Instant.now()))
                //we return null because there is a valid token
                // (should wait for the expiry to retry)
                return null;
            passwordResetTokenRepository.delete(passToken);
        }
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(jwtService.generatePasswordResetJWT(user));
        passwordResetToken.setExpiryDate(Instant.now().plusSeconds(15 * 60));
        passwordResetToken.setUser(user);
        passwordResetTokenRepository.save(passwordResetToken);
        return passwordResetToken;

    }

    /// //////////////////////////forgot Password/////////////////////////////////
    public void forgotPassword(String email)
            throws UserNotVerified, UserNotFoundException, EmailAlreadySentException {

        LocalUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new); // Throw if user doesn't exist

        if(user.getStatus()==Status.BANNED)
            throw new RequestRejectedException("The user is banned from the platform!");

        if (!user.getIsVerified()) {
            throw new UserNotVerified(); // Ensure user is verified
        }

        PasswordResetToken token = createPasswordResetToken(user);
        if (token == null) {
            throw new EmailAlreadySentException(); // Token already exists and is valid
        }

        emailService.sendPasswordResetEmailForUser(token);
    }



    /// ///////////////////// Verify User/////////////////////
    //Annotated with this because we are modifying the data
    @Transactional
    public boolean verifyAccount(String token){

        //We check if the token exist in the DB
        Optional<VerificationToken>  optionalToken = verificationTokenRepository.findByToken(token);
        if(optionalToken.isPresent()){
            //We get the VerificationToken and the user associated with it
            VerificationToken verificationToken = optionalToken.get();
            String role=jwtService.getRoleFromToken(token);
            if ("ROLE_USER".equals(role)){
                LocalUser user = verificationToken.getUser();
                //We check if the user is already verified
                if(!user.getIsVerified()){
                    user.setIsVerified(true);
                    user.setStatus(Status.VERIFIED);
                    userRepository.save(user);
                    verificationTokenRepository.deleteByUser(user);
                    return true;
                }
            } else if ("ROLE_RESTAURANT".equals(role)) {
                Restaurant restaurant = verificationToken.getRestaurant();
                //We check if the user is already verified
                if(!restaurant.getIsVerified()){
                    restaurant.setIsVerified(true);
                    restaurant.setStatus(Status.VERIFIED);
                    restaurantRepository.save(restaurant);
                    verificationTokenRepository.deleteByRestaurant(restaurant);
                    return true;
                }
            }


        }
        return false;
    }


    /////////////////////////////////////////////LOGIN///////////////////////////////////////////


    public String Login(LoginBody loginBody)
            throws BadCredentialsException, UserNotFoundException, UserNotVerified {
        Optional<LocalUser> userOp = userRepository.findByEmailIgnoreCase(loginBody.getEmail());
        //if the User exists
        if (userOp.isPresent()) {
            //store the user
            LocalUser user = userOp.get();
            if(user.getStatus()==Status.BANNED)
                throw new RequestRejectedException("The user is banned from the platform!");
            //verify the password
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                //Verify if the user has verified his email
                if(user.getIsVerified())
                    //We generate the JWT
                    return jwtService.generateToken(user);
                else{
                    //We get all the VerificationTokens associated with this user
                    List<VerificationToken> verificationTokens = user.getVerificationTokens();
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
                        VerificationToken verificationToken = createVerificationToken(user);
                        verificationTokenRepository.save(verificationToken);
                        // resend an emailMessage with the VerificationToken to the user
                        emailService.sendVerificationEmail(verificationToken);
                    }
                    throw new UserNotVerified();
                }
            }throw new PasswordIncorrectException();
        }throw new UserNotFoundException();


    }

    ////////////////////////////Updating Data/////////////////////////////////////////////

    public ResponseEntity<Object> update(LocalUser user, UserDetailsBody userDetailsBody){


        user.setProfileImg(userDetailsBody.getProfileImg());
        user.setLastName(userDetailsBody.getLastName());
        user.setFirstName(userDetailsBody.getFirstName());
        user.setPhone(userDetailsBody.getPhone());

        // Check if the new email is already used by someone else
        Optional<LocalUser> opUser = userRepository.findByEmail(userDetailsBody.getEmail());

        // If a user with this email exists, and it's not the current user -> throw exception
        if (opUser.isPresent() && !opUser.get().getId().equals(user.getId())) {
            throw new UserAlreadyExistsException();
        }

        // It's either the same email or not used by anyone else, so we can safely set it
        user.setEmail(userDetailsBody.getEmail());




        if(!userDetailsBody.getOldPassword().isBlank() && !userDetailsBody.getNewPassword().isBlank()){

            if(!encryptionService.verifyPassword(userDetailsBody.getOldPassword(),user.getPassword()))
                throw new PasswordIncorrectException();

            user.setPassword(encryptionService.encryptPassword(userDetailsBody.getNewPassword()));

        }

        userRepository.save(user);

        return ResponseEntity.ok("Your Account Details Have Been Updated Successfully");
    }

    /// ////////////////////////////////////////////////////////////////
    ///
    public LocalUser getManagedUser(LocalUser user){
        Optional<LocalUser> optionalUser=userRepository.findLocalUserById(user.getId());
        if(optionalUser.isPresent())
            return optionalUser.get();
        //TODO idk
        throw new UserNotFoundException();
    }


    /// /////////////////////Stripe Setup for users/////////////////////////////////////////////////



}
