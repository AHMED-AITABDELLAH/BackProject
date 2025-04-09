package com.savorybites.backend.service;
import com.savorybites.backend.api.model.LoginBody;
import com.savorybites.backend.exception.*;
import com.savorybites.backend.model.Role;
import com.savorybites.backend.repository.VerificationRepository.PasswordResetTokenRepository;
import com.savorybites.backend.model.verificationModels.PasswordResetToken;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import com.savorybites.backend.repository.RestaurantRepository;
import com.savorybites.backend.repository.VerificationRepository.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import com.savorybites.backend.api.model.RestaurantRegistrationBody;
import com.savorybites.backend.model.Restaurant;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {


    private final RestaurantRepository restaurantRepository;
    private final EncryptionService encryptionService;
    private final EmailService emailService;
    private final JWTService jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public RestaurantService(
            RestaurantRepository restaurantRepository,
            EncryptionService encryptionService,
            EmailService emailService,
            JWTService jwtService,
            VerificationTokenRepository verificationTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.encryptionService = encryptionService;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }



    public void registerRestaurant(RestaurantRegistrationBody body)
            throws UserAlreadyExistsException {

        if (restaurantRepository.findByEmailIgnoreCase(body.getEmail()).isPresent())
            throw new UserAlreadyExistsException();

        Restaurant restaurant = new Restaurant();
        restaurant.setRole(Role.ROLE_RESTAURANT);
        restaurant.setTitle(body.getRestaurantName());
        restaurant.setEmail(body.getEmail());
        restaurant.setPhone(body.getPhone());
        restaurant.setContactEmail(body.getContactEmail());
        restaurant.setPaypalEmail(body.getPaypalEmail());
        restaurant.setPassword(encryptionService.encryptPassword(body.getPassword()));
        restaurant.setAddress(body.getAddress());
        restaurant.setIsApproved(false);
        restaurant.setIsVerified(false);

        restaurantRepository.save(restaurant);
        emailService.sendAccountIsWaitingApproval(restaurant);
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

        if (!encryptionService.verifyPassword(loginBody.getPassword(), restaurant.getPassword())) {
            throw new PasswordIncorrectException();
        }

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
        }else
            throw new UserNotApproved();

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





}
