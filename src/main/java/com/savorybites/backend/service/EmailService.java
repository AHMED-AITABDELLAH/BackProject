package com.savorybites.backend.service;

import com.savorybites.backend.exception.user.EmailFailureException;
import com.savorybites.backend.model.Restaurant;
import com.savorybites.backend.model.verificationModels.PasswordResetToken;
import com.savorybites.backend.model.verificationModels.VerificationToken;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
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


    private String buildHtmlEmail(String title, String message, String buttonText, String buttonLink) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
            <h2 style="color: #FD4C2A;">%s</h2>
            <p>%s</p>
            %s
            <br><br>
            <p>Regards,<br>The Team</p>
        </body>
        </html>
        """.formatted(
                title,
                message.replace("\n", "<br>"),
                (buttonLink != null) ? """
                <p style="margin: 20px 0;">
                    <a href="%s" style="background-color: #FD4C2A; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px;">
                        %s
                    </a>
                </p>
                """.formatted(buttonLink, buttonText) : ""
        );
    }

    @Async
    public void sendVerificationEmail(VerificationToken verificationToken) throws EmailFailureException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String email = verificationToken.getUser().getEmail();
            String link = url + "/client/verifyAccount?token=" + verificationToken.getToken();

            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("Verify Your Email");

            String content = buildHtmlEmail(
                    "Verify Your Account",
                    "Please click the button below to verify your email address and activate your account on our platform.",
                    "Verify Email",
                    link
            );

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailFailureException();
        }
    }

    @Async
    public void sendRestaurantVerificationEmail(VerificationToken verificationToken) throws EmailFailureException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String email = verificationToken.getRestaurant().getEmail();
            String link = url + "/restaurant/verifyAccountRestaurant?token=" + verificationToken.getToken();

            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("Verify Your Email");

            String content = buildHtmlEmail(
                    "Your Account is Approved!",
                    "Your account has been approved by the admin team. Please verify your email to activate your account.",
                    "Verify Email",
                    link
            );

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailFailureException();
        }
    }

    @Async
    public void sendAccountIsWaitingApproval(Restaurant restaurant) throws EmailFailureException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(restaurant.getEmail());
            helper.setSubject("Registration Waiting for Approval");

            String content = buildHtmlEmail(
                    "Account Registered Successfully!",
                    """
                    Your account is registered and is now waiting for admin approval.<br>
                    You will receive a verification email once the admins approve your form.<br>
                    Thank you for your patience!
                    """,
                    null,
                    null
            );

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailFailureException();
        }
    }

    @Async
    public void sendAccountIsNotApproved(Restaurant restaurant) throws EmailFailureException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(restaurant.getEmail());
            helper.setSubject("Registration Form Declined");

            String content = buildHtmlEmail(
                    "We're Sorry!",
                    """
                    Your account has been declined by the admins for one or more of the following reasons:
                    <ul>
                        <li>Incomplete form</li>
                        <li>Invalid documents</li>
                        <li>Does not meet platform standards</li>
                        <li>Other internal reasons</li>
                    </ul>
                    If you believe this is a mistake, please contact us at:
                    <ul>
                        <li>support@example.com</li>
                        <li>appeals@example.com</li>
                        <li>admin@example.com</li>
                    </ul>
                    """,
                    null,
                    null
            );

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailFailureException();
        }
    }

    @Async
    public void sendPasswordResetEmailForUser(PasswordResetToken token) throws EmailFailureException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String link = url + "/auth/resetPassword?token=" + token.getToken();

            helper.setFrom(fromAddress);
            helper.setTo(token.getUser().getEmail());
            helper.setSubject("Reset Your Password");

            String content = buildHtmlEmail(
                    "Password Reset Request",
                    "To reset your password, please click the button below:",
                    "Reset Password",
                    link
            );

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailFailureException();
        }
    }

    @Async
    public void sendPasswordResetEmailForRestaurant(PasswordResetToken token) throws EmailFailureException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String link = url + "/auth/resetPassword?token=" + token.getToken();

            helper.setFrom(fromAddress);
            helper.setTo(token.getRestaurant().getEmail());
            helper.setSubject("Reset Your Password");

            String content = buildHtmlEmail(
                    "Password Reset Request",
                    "To reset your password, please click the button below:",
                    "Reset Password",
                    link
            );

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailFailureException();
        }
    }


}
