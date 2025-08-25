package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.component.ViewToolbar;
import com.example.app.base.ui.view.MainLayout;
import com.example.app.shared.domain.Person;
import com.example.app.taskmanagement.service.LoginService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("My Profile - My App")
@PermitAll
public class ProfileView extends Main implements BeforeEnterObserver {

    private final LoginService loginService;
    private Person currentUser;

    // Form fields
    private TextField nameField;
    private TextField usernameField;
    private EmailField emailField;
    private TextField phoneField;
    private DatePicker birthdayField;
    private DatePicker startDateField;

    private Button saveButton;
    private Button cancelButton;
    private Button changePasswordButton;

    public ProfileView(LoginService loginService) {
        this.loginService = loginService;
        createUI();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        currentUser = (Person) VaadinSession.getCurrent().getAttribute(Person.class);
        if (currentUser == null) {
            event.forwardTo("login");
            return;
        }
        populateForm();
    }

    private void createUI() {
        setSizeFull();
        addClassName("profile-view");

        // Header with avatar and basic info
        Div profileHeader = createProfileHeader();
        
        // Profile form
        FormLayout profileForm = createProfileForm();
        
        // Action buttons
        HorizontalLayout buttonLayout = createButtonLayout();

        // Main content layout
        VerticalLayout content = new VerticalLayout();
        content.setMaxWidth("800px");
        content.setMargin(true);
        content.setSpacing(true);
        content.add(profileHeader, profileForm, buttonLayout);

        add(new ViewToolbar("My Profile"));
        add(content);
    }

    private Div createProfileHeader() {
        Div header = new Div();
        header.addClassName("profile-header");
        header.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-l)")
            .set("margin-bottom", "var(--lumo-space-m)");

        // Avatar (larger for profile page)
        Avatar avatar = new Avatar();
        avatar.addThemeVariants(AvatarVariant.LUMO_XLARGE);
        avatar.setColorIndex(5);

        // User info
        H2 nameHeader = new H2();
        nameHeader.getStyle().set("margin", "0");
        
        Span usernameSpan = new Span();
        usernameSpan.addClassName("username");
        usernameSpan.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-l)");

        Span memberSinceSpan = new Span();
        memberSinceSpan.addClassName("member-since");
        memberSinceSpan.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout userInfo = new VerticalLayout(nameHeader, usernameSpan, memberSinceSpan);
        userInfo.setSpacing(false);
        userInfo.setPadding(false);

        HorizontalLayout headerLayout = new HorizontalLayout(avatar, userInfo);
        headerLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
        headerLayout.setSpacing(true);

        header.add(headerLayout);
        return header;
    }

    private FormLayout createProfileForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setMaxWidth("600px");

        H3 personalInfoHeader = new H3("Personal Information");
        personalInfoHeader.getStyle().set("margin-top", "var(--lumo-space-l)");

        // Form fields
        nameField = new TextField("Full Name");
        nameField.setPlaceholder("Enter your full name");
        nameField.setRequired(true);

        usernameField = new TextField("Username");
        usernameField.setPlaceholder("Choose a username");
        usernameField.setRequired(true);
        usernameField.setHelperText("This will be used for login");

        emailField = new EmailField("Email Address");
        emailField.setPlaceholder("your.email@example.com");
        emailField.setRequired(true);

        phoneField = new TextField("Phone Number");
        phoneField.setPlaceholder("+1 (555) 123-4567");

        birthdayField = new DatePicker("Birthday");
        birthdayField.setPlaceholder("Select your birthday");

        startDateField = new DatePicker("Start Date");
        startDateField.setPlaceholder("When did you join?");
        startDateField.setReadOnly(true);
        startDateField.setHelperText("Your account creation date");

        // Layout the form nicely
        formLayout.add(personalInfoHeader);
        formLayout.setColspan(personalInfoHeader, 2);
        
        formLayout.add(nameField, usernameField);
        formLayout.add(emailField, phoneField);
        formLayout.add(birthdayField, startDateField);

        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        saveButton = new Button("Save Changes", e -> saveProfile());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancelButton = new Button("Cancel", e -> cancelChanges());

        changePasswordButton = new Button("Change Password", e -> navigateToChangePassword());
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton, changePasswordButton);
        buttonLayout.setSpacing(true);
        buttonLayout.getStyle().set("margin-top", "var(--lumo-space-l)");

        return buttonLayout;
    }

    private void populateForm() {
        if (currentUser == null) return;

        // Update header
        updateProfileHeader();

        // Populate form fields
        nameField.setValue(currentUser.getName() != null ? currentUser.getName() : "");
        usernameField.setValue(currentUser.getUsername() != null ? currentUser.getUsername() : "");
        emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        phoneField.setValue(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
        birthdayField.setValue(currentUser.getBirthday());
        startDateField.setValue(currentUser.getStartDate());
    }

    private void updateProfileHeader() {
        // Find and update the header elements
        String displayName = currentUser.getName() != null && !currentUser.getName().trim().isEmpty() 
            ? currentUser.getName() 
            : currentUser.getUsername();

        // Update avatar
        getChildren()
            .filter(component -> component.getClass().equals(VerticalLayout.class))
            .map(VerticalLayout.class::cast)
            .flatMap(layout -> layout.getChildren())
            .filter(component -> component.getClass().equals(Div.class))
            .map(Div.class::cast)
            .findFirst()
            .ifPresent(header -> {
                header.getChildren()
                    .filter(component -> component.getClass().equals(HorizontalLayout.class))
                    .map(HorizontalLayout.class::cast)
                    .findFirst()
                    .ifPresent(headerLayout -> {
                        // Update avatar name
                        headerLayout.getChildren()
                            .filter(component -> component.getClass().equals(Avatar.class))
                            .map(Avatar.class::cast)
                            .findFirst()
                            .ifPresent(avatar -> avatar.setName(displayName));

                        // Update text info
                        headerLayout.getChildren()
                            .filter(component -> component.getClass().equals(VerticalLayout.class))
                            .map(VerticalLayout.class::cast)
                            .findFirst()
                            .ifPresent(infoLayout -> {
                                // Update name
                                infoLayout.getChildren()
                                    .filter(component -> component.getClass().equals(H2.class))
                                    .map(H2.class::cast)
                                    .findFirst()
                                    .ifPresent(nameHeader -> nameHeader.setText(displayName));

                                // Update username
                                infoLayout.getChildren()
                                    .filter(component -> component.hasClassName("username"))
                                    .map(Span.class::cast)
                                    .findFirst()
                                    .ifPresent(usernameSpan -> usernameSpan.setText("@" + currentUser.getUsername()));

                                // Update member since
                                String memberSince = currentUser.getStartDate() != null 
                                    ? "Member since " + currentUser.getStartDate().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
                                    : "Member since unknown";
                                    
                                infoLayout.getChildren()
                                    .filter(component -> component.hasClassName("member-since"))
                                    .map(Span.class::cast)
                                    .findFirst()
                                    .ifPresent(memberSpan -> memberSpan.setText(memberSince));
                            });
                    });
            });
    }

    private void saveProfile() {
        try {
            // Validate required fields
            if (nameField.getValue().trim().isEmpty()) {
                showErrorNotification("Name is required");
                nameField.focus();
                return;
            }

            if (usernameField.getValue().trim().isEmpty()) {
                showErrorNotification("Username is required");
                usernameField.focus();
                return;
            }

            if (emailField.getValue().trim().isEmpty()) {
                showErrorNotification("Email is required");
                emailField.focus();
                return;
            }

            // Update user object
            currentUser.setName(nameField.getValue().trim());
            currentUser.setUsername(usernameField.getValue().trim());
            currentUser.setEmail(emailField.getValue().trim());
            currentUser.setPhoneNumber(phoneField.getValue().trim());
            currentUser.setBirthday(birthdayField.getValue());

            // Save to database
            loginService.save(currentUser);

            // Update session
            VaadinSession.getCurrent().setAttribute(Person.class, currentUser);

            // Update header display
            updateProfileHeader();

            showSuccessNotification("Profile updated successfully!");

        } catch (Exception e) {
            showErrorNotification("Failed to save profile: " + e.getMessage());
        }
    }

    private void cancelChanges() {
        populateForm(); // Reset form to original values
        showInfoNotification("Changes cancelled");
    }

    private void navigateToChangePassword() {
        UI.getCurrent().navigate("change-password");
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