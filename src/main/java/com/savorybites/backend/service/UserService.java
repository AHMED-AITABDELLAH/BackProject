package com.savorybites.backend.service;

import com.savorybites.backend.api.dto.auth.LoginBody;
import com.savorybites.backend.api.dto.user.UserDetailsBody;
import com.savorybites.backend.api.dto.auth.UserRegistrationBody;
import com.savorybites.backend.exception.user.*;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.Role;
import com.savorybites.backend.model.verificationModels.PasswordResetToken;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import com.savorybites.backend.repository.VerificationRepository.PasswordResetTokenRepository;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.repository.VerificationRepository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
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

    public UserService(UserRepository userRepository,
                       EncryptionService encryptionService,
                       JWTService jwtService,
                       EmailService emailService,
                       VerificationTokenRepository verificationTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       RestaurantRepository restaurantRepository,
                       CartService cartService) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.restaurantRepository = restaurantRepository;
        this.cartService = cartService;
    }

    /// ////////////////////////////////////////////SignUP-Registration//////////////////////////////////////
    public void Registration(UserRegistrationBody userRegistrationBody)
            throws UserAlreadyExistsException {

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
    /// //////////Generating  verification JWT upon Registering/Login////////////////////////
    private VerificationToken createVerificationToken(LocalUser user){
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setExpiryDate(Instant.now().plusSeconds(3600));
        verificationToken.setUser(user);
        return verificationToken;
    }

    /// ////////////////////////////Generating  Password Reset Token //////////////////////////////
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
                    userRepository.save(user);
                    verificationTokenRepository.deleteByUser(user);
                    return true;
                }
            } else if ("ROLE_RESTAURANT".equals(role)) {
                Restaurant restaurant = verificationToken.getRestaurant();
                //We check if the user is already verified
                if(!restaurant.getIsVerified()){
                    restaurant.setIsVerified(true);
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
            throws BadCredentialsException, UserNotFoundException , UserNotVerified {
        Optional<LocalUser> userOp = userRepository.findByEmailIgnoreCase(loginBody.getEmail());
        //if the User exist
        if (userOp.isPresent()) {
            //store the user
            LocalUser user = userOp.get();
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
/// ////////////////////////////Updating Data/////////////////////////////////////////////

    public ResponseEntity<Object> update(LocalUser user, UserDetailsBody userDetailsBody){


        user.setProfileImg(userDetailsBody.getProfileImg());
        user.setLastName(userDetailsBody.getLastName());
        user.setFirstName(userDetailsBody.getFirstName());
        user.setPhone(userDetailsBody.getPhone());
        Optional<LocalUser> opUser =userRepository.findByEmailIgnoreCase(user.getEmail());

        if(opUser.isPresent() && !Objects.equals(opUser.get().getEmail(), userDetailsBody.getEmail()))
            throw new UserAlreadyExistsException();

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
    public LocalUser getManagedUser(LocalUser user){
        Optional<LocalUser> optionalUser=userRepository.findLocalUserById(user.getId());
        if(optionalUser.isPresent())
            return optionalUser.get();
        //TODO idk
        throw new UserNotFoundException();
    }


}
