package com.example.app.taskmanagement.ui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Welcome - My App")
public class LoginSignupSelectView extends VerticalLayout {

    public LoginSignupSelectView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Welcome to My App Portal");

        Button loginButton = new Button("Login", e -> UI.getCurrent().navigate("login"));
        Button signupButton = new Button("Sign Up", e -> UI.getCurrent().navigate("signup"));

        loginButton.setWidth("150px");
        signupButton.setWidth("150px");

        add(title, loginButton, signupButton);
    }
}
