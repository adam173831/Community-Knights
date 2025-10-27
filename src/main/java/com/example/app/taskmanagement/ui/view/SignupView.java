package com.example.app.taskmanagement.ui.view;

import com.example.app.shared.domain.Person;
import com.example.app.taskmanagement.service.LoginService;
import com.example.app.taskmanagement.service.SignupService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

@Route(value = "signup", layout = com.example.app.base.ui.view.MainLayout.class)
@PageTitle("Sign Up - My App")
public class SignupView extends VerticalLayout {

    private final SignupService signupService;
    private final LoginService loginService;

    public SignupView(SignupService signupService, LoginService loginService) {
        this.signupService = signupService;
        this.loginService = loginService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Create My App Admin Account");

        TextField nameField = new TextField("Full Name");
        TextField emailField = new TextField("Email");
        TextField phoneField = new TextField("Phone Number");
        TextField usernameField = new TextField("Username");
        PasswordField passwordField = new PasswordField("Password");
        PasswordField confirmPasswordField = new PasswordField("Confirm Password");

        Button signupButton = new Button("Sign Up", e -> {
            String fullName = nameField.getValue() != null ? nameField.getValue().trim() : "";
            if (fullName.isEmpty()) {
                Notification.show("Full name is required");
                return;
            }

            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
                Notification.show("Passwords do not match");
                return;
            }

            try {
                Person newAdmin = new Person();
                newAdmin.setName(fullName);
                newAdmin.setEmail(emailField.getValue());
                newAdmin.setPhoneNumber(phoneField.getValue());
                newAdmin.setUsername(usernameField.getValue());

                // Hash password before saving
                newAdmin.setPassword(loginService.encodePassword(passwordField.getValue()));

                newAdmin.setBirthday(LocalDate.of(2000, 1, 1));
                newAdmin.setStartDate(LocalDate.now());
                newAdmin.setAdmin(true); // first account is admin

                signupService.save(newAdmin);
                Notification.show("Account created successfully!");
                UI.getCurrent().navigate("login");
            } catch (Exception ex) {
                Notification.show("Failed to create account: " + ex.getMessage());
            }
        });

        FormLayout form = new FormLayout(
            nameField,
            emailField,
            phoneField,
            usernameField,
            passwordField,
            confirmPasswordField
        );
        form.setWidth("400px");

        add(title, form, signupButton);
    }
}
