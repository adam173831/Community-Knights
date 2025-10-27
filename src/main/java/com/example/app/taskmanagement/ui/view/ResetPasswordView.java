package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.view.MainLayout;
import com.example.app.shared.domain.Person;
import com.example.app.taskmanagement.service.LoginService;
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
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String token) {
        this.token = token;
        removeAll();

        if (token == null || token.isBlank()) {
            add(invalidView("Missing password reset token."));
            return;
        }

        Optional<Person> personOpt = loginService.findByResetToken(token);

        if (personOpt.isEmpty()) {
            add(invalidView("Invalid or expired password reset link."));
            return;
        }

        Person person = personOpt.get();

        if (person.getResetTokenExpiry() == null ||
            person.getResetTokenExpiry().isBefore(LocalDateTime.now())) {

            // token expired
            person.setResetToken(null);
            person.setResetTokenExpiry(null);
            loginService.save(person);

            add(invalidView("This reset link has expired. Please request a new reset email."));
            return;
        }

        this.validatedPerson = person;
        add(validForm(person));
    }

    private Div invalidView(String message) {
        Div wrapper = new Div();
        wrapper.getStyle().set("max-width", "400px");

        H2 header = new H2("Reset Password");
        Paragraph p = new Paragraph(message);

        Button backToLogin = new Button("Back to Login", e -> UI.getCurrent().navigate("login"));
        backToLogin.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        wrapper.add(header, p, backToLogin);
        return wrapper;
    }

    private Div validForm(Person person) {
        Div wrapper = new Div();
        wrapper.getStyle().set("max-width", "400px");

        H2 header = new H2("Set a New Password");
        Paragraph p = new Paragraph("Account: " + person.getEmail());

        PasswordField newPasswordField = new PasswordField("New Password");
        PasswordField confirmPasswordField = new PasswordField("Confirm New Password");

        Button submit = new Button("Update Password", e -> {
            if (!validatePasswords(newPasswordField, confirmPasswordField)) {
                return;
            }

            handlePasswordReset(newPasswordField.getValue());
        });
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        wrapper.add(header, p, newPasswordField, confirmPasswordField, submit);
        return wrapper;
    }

    private boolean validatePasswords(PasswordField newPasswordField, PasswordField confirmPasswordField) {

        String newPassword = newPasswordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        newPasswordField.setInvalid(false);
        confirmPasswordField.setInvalid(false);

        if (newPassword == null || newPassword.isBlank()) {
            newPasswordField.setInvalid(true);
            showErrorNotification("Please enter a new password");
            return false;
        }

        if (newPassword.length() < 8) {
            newPasswordField.setInvalid(true);
            showErrorNotification("Password must be at least 8 characters long");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordField.setInvalid(true);
            showErrorNotification("Passwords do not match");
            return false;
        }

        if (!isPasswordStrong(newPassword)) {
            newPasswordField.setInvalid(true);
            showErrorNotification("Password must contain at least one uppercase letter, one lowercase letter, and one number");
            return false;
        }

        return true;
    }

    private boolean isPasswordStrong(String password) {
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasUpper && hasLower && hasDigit;
    }

    private void handlePasswordReset(String newPassword) {
        try {
            // Hash and save new password
            validatedPerson.setPassword(loginService.encodePassword(newPassword));

            // Clear the reset token to prevent reuse
            validatedPerson.setResetToken(null);
            validatedPerson.setResetTokenExpiry(null);

            // Persist changes
            loginService.save(validatedPerson);

            showSuccessNotification("Password reset successfully! You can now log in with your new password.");

            // Redirect to login
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
