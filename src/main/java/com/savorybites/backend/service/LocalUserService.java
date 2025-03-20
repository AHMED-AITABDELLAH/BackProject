package com.savorybites.backend.service;

import com.savorybites.backend.api.model.LoginBody;
import com.savorybites.backend.api.model.RegistrationBody;
import com.savorybites.backend.exception.PasswordIncorrectException;
import com.savorybites.backend.exception.UserAlreadyExistsException;
import com.savorybites.backend.exception.UserNotFoundException;
import com.savorybites.backend.model.LocalUser;
import com.savorybites.backend.repository.LocalUserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocalUserService {

    private final LocalUserRepository localUserRepository;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;

    public LocalUserService(LocalUserRepository localUserRepository, EncryptionService encryptionService, JWTService jwtService) {
        this.localUserRepository = localUserRepository;
        this.encryptionService = encryptionService;
        this.jwtService = jwtService;
    }

    /// ////////////////////////////////////////////Sign UP-Registration//////////////////////////////////////
    public void Registration(RegistrationBody registrationBody)
            throws UserAlreadyExistsException {

        if (
                (localUserRepository
                        .findByEmailIgnoreCase(registrationBody.getEmail()))
                        .isPresent()
        )
            throw new UserAlreadyExistsException();


        LocalUser newUser = new LocalUser();
        newUser.setFirstName(registrationBody.getFirstName());
        newUser.setLastName(registrationBody.getLastName());
        newUser.setEmail(registrationBody.getEmail());
        //Crypting the password before storing it
        newUser.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        localUserRepository.save(newUser);
    }


    /////////////////////////////////////////////LOGIN///////////////////////////////////////////


    public String Login(LoginBody loginBody)
            throws BadCredentialsException, UserNotFoundException {
        Optional<LocalUser> userOp = localUserRepository.findByEmailIgnoreCase(loginBody.getEmail());
        //if the User exist
        if (userOp.isPresent()) {
            //store the user
            LocalUser user = userOp.get();
            //verify the password
            if (encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword())) {
                System.out.println("User Found");
                return jwtService.generateToken(user);
            }
            throw new PasswordIncorrectException();
        }
        throw new UserNotFoundException();
    }



}
