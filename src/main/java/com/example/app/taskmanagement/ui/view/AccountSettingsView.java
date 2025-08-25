package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.component.ViewToolbar;
import com.example.app.base.ui.view.MainLayout;
import com.example.app.shared.domain.Person;
import com.example.app.taskmanagement.service.LoginService;
import org.springframework.security.crypto.bcrypt.BCrypt;
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
        setSizeFull();
        addClassName("account-settings-view");

        VerticalLayout content = new VerticalLayout();
        content.setMaxWidth("600px");
        content.setMargin(true);
        content.setSpacing(true);

        // Change Password Section
        Div changePasswordSection = createChangePasswordSection();
        
        // Account Information Section
        Div accountInfoSection = createAccountInfoSection();

        content.add(changePasswordSection, accountInfoSection);
        
        add(new ViewToolbar("Account Settings"));
        add(content);
    }

    private Div createChangePasswordSection() {
        Div section = new Div();
        section.addClassName("change-password-section");
        section.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-l)")
            .set("margin-bottom", "var(--lumo-space-l)");

        H3 header = new H3("Change Password");
        header.getStyle().set("margin-top", "0");

        Paragraph description = new Paragraph("Enter your current password and choose a new one. Your new password should be at least 8 characters long with uppercase, lowercase, and numbers.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        FormLayout form = new FormLayout();
        
        currentPasswordField = new PasswordField("Current Password");
        currentPasswordField.setRequired(true);
        currentPasswordField.setWidth("100%");

        newPasswordField = new PasswordField("New Password");
        newPasswordField.setRequired(true);
        newPasswordField.setWidth("100%");
        newPasswordField.setHelperText("At least 8 characters with uppercase, lowercase, and numbers");

        confirmPasswordField = new PasswordField("Confirm New Password");
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setWidth("100%");

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

        form.add(currentPasswordField, newPasswordField, confirmPasswordField);

        // Buttons
        changePasswordButton = new Button("Change Password", e -> changePassword());
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton = new Button("Cancel", e -> clearPasswordForm());

        HorizontalLayout buttonLayout = new HorizontalLayout(changePasswordButton, cancelButton);
        buttonLayout.setSpacing(true);

        section.add(header, description, form, buttonLayout);
        return section;
    }

    private Div createAccountInfoSection() {
        Div section = new Div();
        section.addClassName("account-info-section");
        section.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-l)");

        H3 header = new H3("Account Information");
        header.getStyle().set("margin-top", "0");

        // Quick links to other account-related pages
        VerticalLayout links = new VerticalLayout();
        links.setSpacing(true);
        links.setPadding(false);

        Button profileButton = new Button("Edit Profile", e -> UI.getCurrent().navigate("profile"));
        profileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        profileButton.setWidth("200px");

        Button securityButton = new Button("Security Settings", e -> 
            showInfoNotification("Security settings coming soon!"));
        securityButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        securityButton.setWidth("200px");
        securityButton.setEnabled(false); // Disabled for now

        Button privacyButton = new Button("Privacy Settings", e -> 
            showInfoNotification("Privacy settings coming soon!"));
        privacyButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        privacyButton.setWidth("200px");
        privacyButton.setEnabled(false); // Disabled for now

        links.add(profileButton, securityButton, privacyButton);

        section.add(header, links);
        return section;
    }

    private void changePassword() {
        try {
            String currentPassword = currentPasswordField.getValue();
            String newPassword = newPasswordField.getValue();
            String confirmPassword = confirmPasswordField.getValue();

            // Validate current password
            if (currentPassword.isEmpty()) {
                showErrorNotification("Please enter your current password");
                currentPasswordField.focus();
                return;
            }

            // Verify current password
            if (!BCrypt.checkpw(currentPassword, currentUser.getPassword())) {
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
            if (BCrypt.checkpw(newPassword, currentUser.getPassword())) {
                showErrorNotification("New password must be different from current password");
                newPasswordField.focus();
                return;
            }

            // Hash and save new password
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            currentUser.setPassword(hashedPassword);
            loginService.save(currentUser);

            // Update session
            VaadinSession.getCurrent().setAttribute(Person.class, currentUser);

            // Clear form and show success
            clearPasswordForm();
            showSuccessNotification("Password changed successfully!");

        } catch (Exception e) {
            showErrorNotification("Failed to change password: " + e.getMessage());
        }
    }

    private void clearPasswordForm() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        confirmPasswordField.setInvalid(false);
    }

    private boolean isPasswordStrong(String password) {
        // Check for at least one uppercase, one lowercase, and one digit
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