package com.splitsmart.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.frontend-url:https://splitsmart.vercel.app}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "?token=" + token;

        try {
            if (fromEmail == null || fromEmail.isBlank()) {
                log.warn("SMTP not configured. Password reset link: {}", resetUrl);
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("SplitSmart - Reset Your Password");
            message.setText(
                "Hello,\n\n" +
                "You requested a password reset for your SplitSmart account.\n\n" +
                "Click the link below to reset your password (valid for 30 minutes):\n" +
                resetUrl + "\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "— SplitSmart Team"
            );

            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}. Reset link: {}", toEmail, resetUrl, e);
        }
    }
}
