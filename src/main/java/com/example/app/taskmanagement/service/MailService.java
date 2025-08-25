package com.example.app.taskmanagement.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:knights.noreply@gmail.com}")
    private String fromEmail;
    
    @Value("${app.name:My App}")
    private String appName;

    @Autowired
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Send a simple text email
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            if (!isValidEmail(to)) {
                logger.error("Invalid email address: {}", to);
                throw new IllegalArgumentException("Invalid email address");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail);
            
            mailSender.send(message);
            logger.info("Simple email sent successfully to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send an HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            if (!isValidEmail(to)) {
                logger.error("Invalid email address: {}", to);
                throw new IllegalArgumentException("Invalid email address");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            
            try {
                helper.setFrom(fromEmail, appName);
            } catch (UnsupportedEncodingException e) {
                // Fallback to just email address if encoding fails
                helper.setFrom(fromEmail);
                logger.warn("Could not set display name for email, using address only", e);
            }
            
            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            logger.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send a password reset email with styled HTML
     */
    public void sendPasswordResetEmail(String to, String username, String resetLink) {
        try {
            String subject = "Reset Your " + appName + " Password";
            String htmlBody = buildPasswordResetHtmlBody(username, resetLink);
            
            sendHtmlEmail(to, subject, htmlBody);
            logger.info("Password reset email sent to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send account verification email
     */
    public void sendAccountVerificationEmail(String to, String username, String verificationLink) {
        try {
            String subject = "Verify Your " + appName + " Account";
            String htmlBody = buildAccountVerificationHtmlBody(username, verificationLink);
            
            sendHtmlEmail(to, subject, htmlBody);
            logger.info("Account verification email sent to: {}", to);
            
        } catch (Exception e) {
            logger.error("Failed to send account verification email to: {}", to, e);
            throw new RuntimeException("Failed to send account verification email", e);
        }
    }

    private String buildPasswordResetHtmlBody(String username, String resetLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; padding: 20px 0; border-bottom: 2px solid #8B0000; }
                    .header h1 { color: #8B0000; margin: 0; }
                    .content { padding: 20px 0; }
                    .button { display: inline-block; background-color: #8B0000; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px 0; border-top: 1px solid #eee; color: #666; font-size: 12px; }
                    .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>You've requested to reset your password for your %s account.</p>
                        <p>Click the button below to reset your password:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset My Password</a>
                        </p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 3px;">%s</p>
                        <div class="warning">
                            <strong>⚠️ Important:</strong>
                            <ul>
                                <li>This link will expire in 30 minutes for security reasons</li>
                                <li>If you didn't request this password reset, please ignore this email</li>
                                <li>Your password will remain unchanged unless you click the link above</li>
                            </ul>
                        </div>
                    </div>
                    <div class="footer">
                        <p>This email was sent from %s. Please do not reply to this email.</p>
                        <p>If you're having trouble with the button above, copy and paste the URL into your web browser.</p>
                    </div>
                </div>
            </body>
            </html>
            """, appName, appName, username, appName, resetLink, resetLink, appName);
    }

    private String buildAccountVerificationHtmlBody(String username, String verificationLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Account Verification - %s</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; margin: 0; padding: 20px; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; padding: 20px 0; border-bottom: 2px solid #8B0000; }
                    .header h1 { color: #8B0000; margin: 0; }
                    .content { padding: 20px 0; }
                    .button { display: inline-block; background-color: #8B0000; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px 0; border-top: 1px solid #eee; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to %s</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for registering with %s! Please verify your email address to complete your account setup.</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Verify My Account</a>
                        </p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; background-color: #f8f9fa; padding: 10px; border-radius: 3px;">%s</p>
                    </div>
                    <div class="footer">
                        <p>This email was sent from %s. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, appName, appName, username, appName, verificationLink, verificationLink, appName);
    }

    private boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && email.contains("@") && email.contains(".");
    }
}