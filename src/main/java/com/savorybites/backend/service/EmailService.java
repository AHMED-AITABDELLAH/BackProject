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

import java.util.List;

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

    @Async
    public void sendOrderProgressEmail(String fullName, String email, long orderId, String currentStatus) throws EmailFailureException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("Update on Your Order #" + orderId);

            // Define the steps in order
            List<String> steps = List.of("Accepted", "Preparing", "Out for Delivery", "Delivered");

            // Find the current step index
            int currentStepIndex = steps.indexOf(currentStatus);
            if (currentStepIndex == -1) {
                currentStepIndex = steps.size() - 1; // default to last step if status not found
            }

            // Build the timeline HTML
            StringBuilder timelineHtml = new StringBuilder();
            for (int i = 0; i < steps.size(); i++) {
                String step = steps.get(i);
                boolean isCompleted = i < currentStepIndex;
                boolean isCurrent = i == currentStepIndex;

                String circleColor = isCompleted ? "#FD4C2A" : (isCurrent ? "#FD4C2A" : "#CCCCCC");
                String textColor = isCompleted ? "#FD4C2A" : (isCurrent ? "#FD4C2A" : "#999999");
                String fontWeight = isCurrent ? "font-weight: 600;" : "";
                String circleContent = isCompleted ? "✓" : String.valueOf((i + 1)); // Show checkmark for completed steps

                timelineHtml.append(String.format("""
                <td align="center" style="text-align: center; vertical-align: top; padding: 0 10px;">
                    <div style="width: 24px; height: 24px; border-radius: 50%%; background-color: %s; 
                         margin: 0 auto 8px; display: flex; align-items: center; justify-content: center;
                         color: white; font-size: 12px;">
                        %s
                    </div>
                    <p style="font-size: 14px; color: %s; margin: 0; %s">%s</p>
                </td>
            """,
                        circleColor,
                        circleContent,
                        textColor,
                        fontWeight,
                        step));

                // Add connector line between steps (except after last step)
                if (i < steps.size() - 1) {
                    String lineColor = isCompleted ? "#FD4C2A" : "#CCCCCC";
                    timelineHtml.append(String.format("""
                    <td style="width: 40px; position: relative;">
                        <div style="position: absolute; top: 12px; left: 0; right: 0; height: 2px; background-color: %s;"></div>
                    </td>
                """, lineColor));
                }
            }

            // Full HTML email content
            String content = """
            <html>
            <head>
                <style>
                    @media only screen and (max-width: 600px) {
                        .timeline-table {
                            width: 100%% !important;
                        }
                        .timeline-step {
                            padding: 0 5px !important;
                        }
                    }
                </style>
            </head>
            <body style="font-family: 'Arial', sans-serif; background-color: #f7f7f7; margin: 0; padding: 20px;">
                <table width="100%%" cellspacing="0" cellpadding="0" style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05);">
                    <tr>
                        <td style="padding: 30px;">
                            <h1 style="color: #333333; font-size: 22px; margin-top: 0; text-align: center;">Order Status Update</h1>
                            
                            <p style="color: #666666; font-size: 16px; line-height: 1.5;">
                                Hello %s,<br><br>
                                Your order <strong style="color: #333333;">#%d</strong> is currently:
                            </p>
                            
                            <div style="background-color: #FFF9F8; border-left: 4px solid #FD4C2A; padding: 12px 16px; margin: 20px 0; border-radius: 0 4px 4px 0;">
                                <p style="color: #FD4C2A; font-size: 18px; font-weight: 600; margin: 0;">%s</p>
                            </div>
                            
                            <p style="color: #666666; font-size: 15px; margin-bottom: 20px;">Here's your order progress:</p>
                            
                            <table cellspacing="0" cellpadding="0" style="width: 100%%; margin: 30px 0 40px;">
                                <tr>
                                    %s
                                </tr>
                            </table>
                            
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s/client/orders/track?orderId=%d" style="display: inline-block; background-color: #FD4C2A; color: #ffffff; text-decoration: none; padding: 12px 24px; border-radius: 4px; font-size: 16px; font-weight: 600;">Track Your Order</a>
                            </div>
                            
                            <p style="color: #999999; font-size: 14px; text-align: center; border-top: 1px solid #eeeeee; padding-top: 20px; margin-bottom: 0;">
                                Thank you for shopping with us!<br>
                                If you have any questions, please contact our support team.
                            </p>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
        """.formatted(fullName, orderId, currentStatus, timelineHtml.toString(), url, orderId);

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailFailureException();
        }
    }

    @Async
    public void sendOrderCancelledByUserEmail(String fullName, String email, long orderId) throws EmailFailureException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("Order Cancelled");

            String content = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                <h2 style="color: #FD4C2A;">Hello %s, your order has been cancelled</h2>
                <p>You have successfully cancelled your order <strong>#%s</strong>. If this was a mistake, feel free to reorder at any time.</p>
                <br>
                <a href="%s/menu" style="background-color: #FD4C2A; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px;">
                    Browse Menu
                </a>
                <br><br>
                <p>We hope to serve you again soon! 🍽️<br><br>Regards,<br>The Foodie Team</p>
            </body>
            </html>
        """.formatted(fullName, orderId, url);

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailFailureException();
        }
    }

    @Async
    public void sendOrderRejectedByRestaurantEmail(String fullName, String email, long orderId, String restaurantName) throws EmailFailureException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(email);
            helper.setSubject("Order Rejected by Restaurant");

            String content = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                <h2 style="color: #FD4C2A;">Hello %s, your order was rejected</h2>
                <p>We regret to inform you that <strong>%s</strong> has rejected your order <strong>#%s</strong>.</p>
                <p>This may be due to availability issues or other unexpected circumstances.</p>
                <br>
                <a href="%s/restaurants" style="background-color: #FD4C2A; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px;">
                    Choose Another Restaurant
                </a>
                <br><br>
                <p>We apologize for the inconvenience. We’re here to help you find a new favorite meal! 🍽️<br><br>Regards,<br>The Foodie Team</p>
            </body>
            </html>
        """.formatted(fullName, restaurantName, orderId, url);

            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailFailureException();
        }
    }

}
