package com.savorybites.backend.service;

import com.savorybites.backend.api.model.LoginBody;
import com.savorybites.backend.api.model.RegistrationBody;
import com.savorybites.backend.exception.*;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.model.PasswordResetToken;
import com.savorybites.backend.model.VerificationToken;
import com.savorybites.backend.repository.PasswordResetTokenRepository;
import com.savorybites.backend.repository.UserRepository;
import com.savorybites.backend.repository.VerificationTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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

    public UserService(UserRepository userRepository,
                       EncryptionService encryptionService,
                       JWTService jwtService,
                       EmailService emailService,
                       VerificationTokenRepository verificationTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    /// ////////////////////////////////////////////SignUP-Registration//////////////////////////////////////
    public void Registration(RegistrationBody registrationBody)
            throws UserAlreadyExistsException {

        if (
                (userRepository
                        .findByEmailIgnoreCase(registrationBody.getEmail()))
                        .isPresent()
        )
            throw new UserAlreadyExistsException();


        LocalUser newUser = new LocalUser();
        newUser.setUsername(registrationBody.getUsername());
        newUser.setEmail(registrationBody.getEmail());
        //Crypting the password before storing it
        newUser.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        //Here we delelte old verification token
        verificationTokenRepository.deleteByCreationTimestampBefore(Timestamp.from(Instant.now()));
        //here we send a verification email to the user to confirm it
        VerificationToken verificationToken = createVerificationToken(newUser);
        emailService.sendVerificationEmail(verificationToken);


        userRepository.save(newUser);
        verificationTokenRepository.save(verificationToken);
    }
    /// //////////Generating  verification JWT upon Registering/Login////////////////////////
    private VerificationToken createVerificationToken(LocalUser user){
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreationTimestamp(new Timestamp(System.currentTimeMillis()));
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
    /// //////////////////////////forgot Passowrd/////////////////////////////////
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

        emailService.sendPasswordResetEmail(token);
    }



    /// ///////////////////// Verify User/////////////////////
    //Anotated with this because we are modifying the data
    @Transactional
    public boolean verifyUser(String token){

        //We check if the token exist in the DB
        Optional<VerificationToken>  optionalToken = verificationTokenRepository.findByToken(token);
        if(optionalToken.isPresent()){
            //We get the VerificationToken and the user associated with it
            VerificationToken verificationToken = optionalToken.get();
            LocalUser user = verificationToken.getUser();
            //We check if the user is already verified
            if(!user.getIsVerified()){
                user.setIsVerified(true);
                 userRepository.save(user);
                verificationTokenRepository.deleteByUser(user);
                return true;
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
                    //We get all the Verificationtokens associated with this user
                    List<VerificationToken> verificationTokens = user.getVerificationTokens();
                    //We check if the user has zero verification tokens or expired one
                    if(
                            verificationTokens.isEmpty()
                                    ||
                            verificationTokens.get(verificationTokens.size()-1)
                                    .getCreationTimestamp()
                                    .before(new Timestamp(System.currentTimeMillis() - 3600*1000))
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



}
