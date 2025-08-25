package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.view.MainLayout;
import com.example.app.shared.domain.Person;
import com.example.app.taskmanagement.service.LoginService;
import com.example.app.taskmanagement.service.MailService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Route(value = "forgot-password", layout = MainLayout.class)
@PageTitle("Forgot Password - My App")
public class ForgotPasswordView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordView.class);

    private final LoginService loginService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

//    public ForgotPasswordView(LoginService loginService) {
//        this.loginService = loginService;

    public ForgotPasswordView(LoginService loginService) {
        this.loginService = loginService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#f5f5f5");

        createForgotPasswordForm();
    }

    private void createForgotPasswordForm() {
        // Main container with styling
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setSpacing(true);
        formContainer.setPadding(true);
        formContainer.setAlignItems(Alignment.CENTER);
        formContainer.setMaxWidth("400px");
        formContainer.getStyle()
            .set("padding", "30px")
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");

        // Title
        H2 title = new H2("Reset Your Password");
        title.getStyle().set("color", "#8B0000").set("margin-bottom", "0.5em");

        // Instructions
        Paragraph instructions = new Paragraph(
            "Enter your email address and we'll send you a link to reset your password."
        );
        instructions.getStyle().set("text-align", "center").set("color", "#666");

        // Email field with validation
        EmailField emailField = new EmailField("Email Address");
        emailField.setWidth("100%");
        emailField.setRequired(true);
        emailField.setErrorMessage("Please enter a valid email address");
        emailField.setPlaceholder("Enter your email address");

        // Submit button
        Button resetButton = new Button("Send Reset Link", e -> {
            if (emailField.isInvalid() || emailField.isEmpty()) {
                showErrorNotification("Please enter a valid email address");
                return;
            }
            handlePasswordReset(emailField.getValue());
        });
        
        resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        resetButton.getStyle()
            .set("background-color", "#8B0000")
            .set("width", "100%")
            .set("margin-top", "10px");

        // Back to login link
        Button backToLoginButton = new Button("Back to Login", 
            e -> UI.getCurrent().navigate("login"));
        backToLoginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        formContainer.add(title, instructions, emailField, resetButton, backToLoginButton);
        add(formContainer);
    }

    private void handlePasswordReset(String email) {
        try {
            boolean success = loginService.initiatePasswordReset(email);
            
            if (success) {
                showSuccessNotification("If an account with that email exists, you'll receive a password reset link shortly.");
            } else {
                showErrorNotification("Unable to process password reset request. Please try again later.");
            }
            
            // Always navigate back to login for security
            UI.getCurrent().navigate("login");
            
        } catch (Exception ex) {
            logger.error("Error handling password reset for email: {}", email, ex);
            showErrorNotification("Unable to send reset email. Please try again later.");
        }
    }

    private String buildResetPasswordUrl(String token) {
        return baseUrl + "/reset-password/" + token;
    }

    private String buildEmailBody(String username, String resetLink) {
        return String.format("""
            Hi %s,
            
            You've requested to reset your password for your My App account.
            
            Click the link below to reset your password (valid for 30 minutes):
            %s
            
            If you didn't request this password reset, you can safely ignore this email.
            Your password will remain unchanged.
            
            For security reasons, this link will expire in 30 minutes.
            
            Best regards,
            My App Team
            """, username, resetLink);
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}