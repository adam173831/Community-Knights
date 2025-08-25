package com.example.app.base.ui.view;

import com.example.app.base.ui.component.ViewToolbar;
import com.example.app.shared.domain.Person;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Route("home")
@PermitAll
public final class MainView extends Main implements BeforeEnterObserver {

    public MainView() {
        addClassName(Padding.MEDIUM);

        Person person = (Person) VaadinSession.getCurrent().getAttribute(Person.class);
        String displayName = (person != null) ? person.getUsername() : "Guest";

        H1 welcome = new H1("Welcome, " + displayName + "!");
        welcome.addClassNames(Margin.Bottom.LARGE);

        add(new ViewToolbar("Home"), welcome, new Div("Please select a view from the menu on the left."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (VaadinSession.getCurrent().getAttribute(Person.class) == null) {
            event.forwardTo("login");
        }
    }
}
