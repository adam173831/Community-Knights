package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.component.ViewToolbar;
import com.example.app.base.ui.view.MainLayout;
import com.example.app.shared.domain.Person;
import com.example.app.taskmanagement.service.LoginService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.util.Optional;

@Route(value = "account-settings", layout = MainLayout.class)
@PageTitle("Account Settings - My App")
@PermitAll
public class AccountSettingsView extends Main implements BeforeEnterObserver {

    private final LoginService loginService;
    private Person currentUser;

    // Change Password Form
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Button changePasswordButton;
    private Button cancelButton;

    public AccountSettingsView(LoginService loginService) {
        this.loginService = loginService;
        createUI();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        currentUser = (Person) VaadinSession.getCurrent().getAttribute(Person.class);
        if (currentUser == null) {
            event.forwardTo("login");
        }
    }

    private void createUI() {
        addClassName("account-settings-view");

        ViewToolbar toolbar = new ViewToolbar("Account Settings");

        // Section: Password Change
        VerticalLayout passwordSection = new VerticalLayout();
        passwordSection.setPadding(true);
        passwordSection.setSpacing(true);

        H3 passwordHeader = new H3("Change Password");
        Paragraph passwordDescription = new Paragraph(
            "Update your password. Make sure it's something secure and only you know."
        );

        currentPasswordField = new PasswordField("Current Password");
        newPasswordField = new PasswordField("New Password");
        confirmPasswordField = new PasswordField("Confirm New Password");

        changePasswordButton = new Button("Change Password", e -> changePassword());
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton = new Button("Cancel", e -> clearPasswordForm());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonRow = new HorizontalLayout(changePasswordButton, cancelButton);

        FormLayout passwordForm = new FormLayout(
            currentPasswordField,
            newPasswordField,
            confirmPasswordField
        );
        passwordForm.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );
        passwordForm.setColspan(currentPasswordField, 2);
        passwordForm.setColspan(newPasswordField, 1);
        passwordForm.setColspan(confirmPasswordField, 1);

        passwordSection.add(
            passwordHeader,
            passwordDescription,
            passwordForm,
            buttonRow
        );

        // Section: Account management links
        VerticalLayout linksSection = buildLinksSection();

        add(toolbar, passwordSection, linksSection);
    }

    private VerticalLayout buildLinksSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(false);

        H3 header = new H3("More Settings");
        Paragraph p = new Paragraph("Manage other aspects of your account.");

        HorizontalLayout links = new HorizontalLayout();
        links.setSpacing(true);
        links.setPadding(false);

        Button profileButton = new Button("Edit Profile", e -> UI.getCurrent().navigate("profile"));
        profileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        profileButton.setWidth("200px");

        Button securityButton = new Button("Security Settings",
            e -> showInfoNotification("Security settings coming soon!")
        );
        securityButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        securityButton.setWidth("200px");
        securityButton.setEnabled(false);

        Button privacyButton = new Button("Privacy Settings",
            e -> showInfoNotification("Privacy settings coming soon!")
        );
        privacyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        privacyButton.setWidth("200px");
        privacyButton.setEnabled(false);

        links.add(profileButton, securityButton, privacyButton);

        section.add(header, p, links);
        return section;
    }

    private void changePassword() {
        changePasswordButton.setEnabled(false);
        try {
            String currentPassword = currentPasswordField.getValue();
            String newPassword = newPasswordField.getValue();
            String confirmPassword = confirmPasswordField.getValue();

            if (currentUser == null || currentUser.getId() == null) {
                showErrorNotification("Your session has expired. Please log in again.");
                return;
            }

            Optional<Person> refreshedUser = loginService.findById(currentUser.getId());
            if (refreshedUser.isEmpty()) {
                showErrorNotification("Unable to load your account details. Please log in again.");
                return;
            }

            currentUser = refreshedUser.get();

            // Validate current password
            if (currentPassword.isEmpty()) {
                showErrorNotification("Please enter your current password");
                currentPasswordField.focus();
                return;
            }

            // Verify current password
            if (!loginService.passwordMatches(currentUser, currentPassword)) {
                showErrorNotification("Current password is incorrect");
                currentPasswordField.focus();
                return;
            }

            // Validate new password
            if (newPassword.length() < 8) {
                showErrorNotification("New password must be at least 8 characters long");
                newPasswordField.focus();
                return;
            }

            if (!isPasswordStrong(newPassword)) {
                showErrorNotification("Password must contain uppercase, lowercase, and numbers");
                newPasswordField.focus();
                return;
            }

            // Validate password confirmation
            if (!newPassword.equals(confirmPassword)) {
                showErrorNotification("New passwords do not match");
                confirmPasswordField.focus();
                return;
            }

            // Check if new password is different from current
            if (loginService.passwordMatches(currentUser, newPassword)) {
                showErrorNotification("New password must be different from current password");
                newPasswordField.focus();
                return;
            }

            // Hash and save new password
            String hashedPassword = loginService.encodePassword(newPassword);
            currentUser.setPassword(hashedPassword);
            loginService.save(currentUser);

            // Update session
            VaadinSession.getCurrent().setAttribute(Person.class, currentUser);

            // Clear form and show success
            clearPasswordForm();
            showSuccessNotification("Password changed successfully!");

        } catch (Exception e) {
            showErrorNotification("Failed to change password: " + e.getMessage());
        } finally {
            changePasswordButton.setEnabled(true);
        }
    }

    private void clearPasswordForm() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        confirmPasswordField.setInvalid(false);
    }

    private boolean isPasswordStrong(String password) {
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasUpper && hasLower && hasDigit;
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showInfoNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }
}
