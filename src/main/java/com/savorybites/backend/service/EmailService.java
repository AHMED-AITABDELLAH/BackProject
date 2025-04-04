package com.savorybites.backend.service;

import com.savorybites.backend.exception.EmailFailureException;
import com.savorybites.backend.model.PasswordResetToken;
import com.savorybites.backend.model.VerificationToken;
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
                + url + "/auth/verify?token="+verificationToken.getToken());
        javaMailSender.send(mailMessage);
    }

    @Async
    public void sendPasswordResetEmail(PasswordResetToken passwordResetToken) throws EmailFailureException {
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(passwordResetToken.getUser().getEmail());
        mailMessage.setSubject("Password Reset");
        mailMessage.setText("Please follow the link bellow to reset your account password on the platform. \n"
                + url + "/auth/resetPassword?token="+ passwordResetToken.getToken());
        javaMailSender.send(mailMessage);
    }


}
