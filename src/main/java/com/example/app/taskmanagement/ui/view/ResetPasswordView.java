package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.view.MainLayout;
import com.example.app.shared.domain.Person;
import com.example.app.taskmanagement.service.LoginService;
import org.springframework.security.crypto.bcrypt.BCrypt;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Route(value = "reset-password", layout = MainLayout.class)
@PageTitle("Reset Password - My App")
public class ResetPasswordView extends VerticalLayout implements HasUrlParameter<String> {

    private final LoginService loginService;
    private String token;
    private Person validatedPerson;

    public ResetPasswordView(LoginService loginService) {
        this.loginService = loginService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#f5f5f5");
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String token) {
        this.token = token;

        // Clear any existing content
        removeAll();

        if (token == null || token.trim().isEmpty()) {
            showInvalidTokenPage("Invalid reset link. Please request a new password reset.");
            return;
        }

        // Validate token
        Optional<Person> personOpt = loginService.findByResetToken(token.trim());
        
        if (personOpt.isEmpty()) {
            showInvalidTokenPage("This reset link is invalid. Please request a new password reset.");
            return;
        }

        Person person = personOpt.get();
        
        // Check if token has expired
        if (person.getResetTokenExpiry() == null || 
            person.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            
            // Clean up expired token
            person.setResetToken(null);
            person.setResetTokenExpiry(null);
            loginService.save(person);
            
            showInvalidTokenPage("This reset link has expired. Please request a new password reset.");
            return;
        }

        // Token is valid, store the person and show reset form
        this.validatedPerson = person;
        showResetPasswordForm();
    }

    private void showInvalidTokenPage(String message) {
        VerticalLayout container = createStyledContainer();
        
        H2 title = new H2("Invalid Reset Link");
        title.getStyle().set("color", "#d32f2f");
        
        Paragraph errorMessage = new Paragraph(message);
        errorMessage.getStyle().set("color", "#666").set("text-align", "center");
        
        Button requestNewResetButton = new Button("Request New Password Reset", 
            e -> UI.getCurrent().navigate("forgot-password"));
        requestNewResetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        requestNewResetButton.getStyle().set("background-color", "#8B0000");
        
        Button backToLoginButton = new Button("Back to Login", 
            e -> UI.getCurrent().navigate("login"));
        backToLoginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        container.add(title, errorMessage, requestNewResetButton, backToLoginButton);
        add(container);
    }

    private void showResetPasswordForm() {
        VerticalLayout container = createStyledContainer();
        
        H2 title = new H2("Reset Your Password");
        title.getStyle().set("color", "#8B0000").set("margin-bottom", "0.5em");
        
        Paragraph instructions = new Paragraph("Enter your new password below.");
        instructions.getStyle().set("color", "#666").set("text-align", "center");
        
        // Password fields with validation
        PasswordField newPasswordField = new PasswordField("New Password");
        newPasswordField.setWidth("100%");
        newPasswordField.setRequired(true);
        newPasswordField.setMinLength(8);
        newPasswordField.setHelperText("Password must be at least 8 characters long");
        newPasswordField.setErrorMessage("Password must be at least 8 characters long");
        
        PasswordField confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setWidth("100%");
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setErrorMessage("Passwords must match");
        
        // Real-time password matching validation
        confirmPasswordField.addValueChangeListener(e -> {
            if (!confirmPasswordField.getValue().isEmpty()) {
                boolean matches = newPasswordField.getValue().equals(confirmPasswordField.getValue());
                confirmPasswordField.setInvalid(!matches);
                if (!matches) {
                    confirmPasswordField.setErrorMessage("Passwords do not match");
                }
            }
        });
        
        Button resetButton = new Button("Reset Password", e -> {
            if (validateAndResetPassword(newPasswordField, confirmPasswordField)) {
                handlePasswordReset(newPasswordField.getValue());
            }
        });
        
        resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        resetButton.getStyle()
            .set("background-color", "#8B0000")
            .set("width", "100%")
            .set("margin-top", "10px");
        
        Button cancelButton = new Button("Cancel", e -> UI.getCurrent().navigate("login"));
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        container.add(title, instructions, newPasswordField, confirmPasswordField, resetButton, cancelButton);
        add(container);
    }

    private VerticalLayout createStyledContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setSpacing(true);
        container.setPadding(true);
        container.setAlignItems(Alignment.CENTER);
        container.setMaxWidth("400px");
        container.getStyle()
            .set("padding", "30px")
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");
        return container;
    }

    private boolean validateAndResetPassword(PasswordField newPasswordField, PasswordField confirmPasswordField) {
        String newPassword = newPasswordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();
        
        // Validate password length
        if (newPassword.length() < 8) {
            newPasswordField.setInvalid(true);
            showErrorNotification("Password must be at least 8 characters long");
            return false;
        }
        
        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordField.setInvalid(true);
            showErrorNotification("Passwords do not match");
            return false;
        }
        
        // Additional password strength validation could be added here
        if (!isPasswordStrong(newPassword)) {
            newPasswordField.setInvalid(true);
            showErrorNotification("Password must contain at least one uppercase letter, one lowercase letter, and one number");
            return false;
        }
        
        return true;
    }

    private boolean isPasswordStrong(String password) {
        // Check for at least one uppercase, one lowercase, and one digit
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUpper && hasLower && hasDigit;
    }

    private void handlePasswordReset(String newPassword) {
        try {
            // Hash the new password
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            
            // Update the user's password
            validatedPerson.setPassword(hashedPassword);
            
            // Clear the reset token to prevent reuse
            validatedPerson.setResetToken(null);
            validatedPerson.setResetTokenExpiry(null);
            
            // Save the changes
            loginService.save(validatedPerson);
            
            showSuccessNotification("Password reset successfully! You can now log in with your new password.");
            
            // Redirect to login after a short delay
            UI.getCurrent().navigate("login");
            
        } catch (Exception ex) {
            showErrorNotification("Failed to reset password. Please try again.");
        }
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