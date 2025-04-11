package com.savorybites.backend.service;

import com.savorybites.backend.exception.user.EmailFailureException;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.verificationModels.PasswordResetToken;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${email.from}")
    private String fromAddress;
    @Value("${app.frontend.url}")
    private String url;
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender){
        this.javaMailSender=javaMailSender;
    }


    @Async
    public void sendVerificationEmail(VerificationToken verificationToken) throws EmailFailureException {
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(verificationToken.getUser().getEmail());
        mailMessage.setSubject("Verify your email");
        mailMessage.setText("Please follow the link bellow to activate your account on the platform. \n"
                + url + "/verifyAccount?token="+verificationToken.getToken());
        javaMailSender.send(mailMessage);
    }


    @Async
    public void sendRestaurantVerificationEmail(VerificationToken verificationToken) throws EmailFailureException {
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(verificationToken.getRestaurant().getEmail());
        mailMessage.setSubject("Verify your email");
        mailMessage.setText("Your Account is Approved by Admins ." +
                "\nPlease follow the link bellow to activate your account on the platform. \n"
                + url + "/verifyAccount?token="+verificationToken.getToken());
        javaMailSender.send(mailMessage);
    }

    @Async
    public void sendAccountIsWaitingApproval(Restaurant restaurant) throws EmailFailureException{
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(restaurant.getEmail());
        mailMessage.setSubject("Registration Form is waiting approval");
        mailMessage.setText("""
                Your Account is registered successfully and waiting the admins approval.\
                
                Thank you for your patience your verification mail will be sent to you as soon as the admins approved your form.\
                
                Regards.""");
        javaMailSender.send(mailMessage);
    }

    @Async
    public void sendAccountIsNotApproved(Restaurant restaurant) throws EmailFailureException{
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(restaurant.getEmail());
        mailMessage.setSubject("Registration Form is declined");
        mailMessage.setText("""
                We are sorry to announce that your account is declined by the admins for Some of the following reasons :\
                *\
                *\
                *\
                *\
                if there is any bad or wrong judging on your Registration forms please inform us by reaching with these emails below:\
                *\
                *\
                *\
                Regards.""");
        javaMailSender.send(mailMessage);
    }



    @Async
    public void sendPasswordResetEmailForUser(PasswordResetToken passwordResetToken) throws EmailFailureException {
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(passwordResetToken.getUser().getEmail());
        mailMessage.setSubject("Password Reset");
        mailMessage.setText("Please follow the link bellow to reset your account password on the platform. \n"
                + url + "/auth/resetPassword?token="+ passwordResetToken.getToken());
        javaMailSender.send(mailMessage);
    }

    @Async
    public void sendPasswordResetEmailForRestaurant(PasswordResetToken passwordResetToken) throws EmailFailureException {
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(passwordResetToken.getRestaurant().getEmail());
        mailMessage.setSubject("Password Reset");
        mailMessage.setText("Please follow the link bellow to reset your account password on the platform. \n"
                + url + "/auth/resetPassword?token="+ passwordResetToken.getToken());
        javaMailSender.send(mailMessage);
    }


}
