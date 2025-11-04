package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.view.MainLayout;
import com.example.app.shared.domain.Person;
import com.example.app.taskmanagement.service.LoginService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@Route(value = "login", layout = MainLayout.class)
@PageTitle("Login - My App")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginService loginService;
    private final LoginForm loginForm = new LoginForm();

    public LoginView(LoginService loginService) {
        this.loginService = loginService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background-color", "#f5f5f5");

        loginForm.setI18n(createCustomI18n());
        // If you’re using Spring Security’s VaadinLogin, keep the action.
        // Otherwise, it’s fine to leave; your listener handles auth.
        loginForm.setAction("login");
        loginForm.addLoginListener(event -> authenticate(event.getUsername(), event.getPassword()));

        Button forgotPasswordButton = new Button("FORGOT PASSWORD?",
            e -> UI.getCurrent().navigate("forgot-password"));
        forgotPasswordButton.getStyle()
            .set("background-color", "#8B0000")
            .set("color", "white")
            .set("border-radius", "8px")
            .set("font-weight", "bold")
            .set("box-shadow", "0px 4px 10px rgba(0,0,0,0.1)");

        VerticalLayout loginBox = new VerticalLayout();
        loginBox.setSpacing(false);
        loginBox.setPadding(false);
        loginBox.setAlignItems(Alignment.CENTER);
        loginBox.getStyle()
            .set("padding", "30px")
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");

        H1 title = new H1("My App Login");
        title.getStyle().set("margin-bottom", "0.5em");

        loginBox.add(title, loginForm, forgotPasswordButton);
        add(loginBox);
    }

    /**
     * Authenticate the user and route to "home" on success.
     * Handles Optional<Person> correctly and guards against null VaadinSession.
     */
    private void authenticate(String username, String password) {
        loginForm.setError(false);

        loginService.authenticate(username, password).ifPresentOrElse(person -> {
            // LoginService already stores the user in session (if a session exists),
            // but this guard ensures no NPE if called off the UI thread.
            if (VaadinSession.getCurrent() != null) {
                VaadinSession.getCurrent().setAttribute(Person.class, person);
            }
            UI.getCurrent().navigate("home");
        }, () -> {
            // Invalid credentials: show error state on the LoginForm
            loginForm.setError(true);
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Guard VaadinSession to avoid NPEs in non-UI contexts
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && session.getAttribute(Person.class) != null) {
            event.forwardTo("home");
        }
    }

    private LoginI18n createCustomI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("Login");
        i18n.getForm().setUsername("Username");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setSubmit("Sign in");
        i18n.getForm().setForgotPassword(null);
        i18n.getErrorMessage().setTitle("Login failed");
        i18n.getErrorMessage().setMessage("Invalid username or password");
        return i18n;
    }
}
