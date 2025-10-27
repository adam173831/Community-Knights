package com.example.app.base.ui.view;

import com.example.app.shared.domain.Person;
import com.example.app.shared.domain.UserPreferences;
import com.example.app.taskmanagement.service.FileStorageService;
import com.example.app.taskmanagement.service.SettingsService;
import com.example.app.taskmanagement.ui.util.ThemeUtil;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import jakarta.annotation.security.PermitAll;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
@PermitAll
public final class MainLayout extends AppLayout {

    private static final Logger logger = LoggerFactory.getLogger(MainLayout.class);

    private final SettingsService settingsService;
    private final FileStorageService fileStorageService;

    public MainLayout(SettingsService settingsService, FileStorageService fileStorageService) {
        this.settingsService = settingsService;
        this.fileStorageService = fileStorageService;
        setPrimarySection(Section.DRAWER);
        applyThemeFromPreferences();
        addToDrawer(createHeader(), new Scroller(createSideNav()), createProfileMenu());
    }

    private Div createHeader() {
        var appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);

        var appName = new Span("My App");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);

        var header = new Div(appLogo, appName);
        header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
        return header;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(Margin.Horizontal.MEDIUM);

        // Add default menu entries from MenuConfiguration
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));

        // Add Settings as a dedicated menu item for easy access
        Person currentUser = (Person) VaadinSession.getCurrent().getAttribute(Person.class);
        if (currentUser != null) {
            nav.addItem(new SideNavItem("Settings", "settings", VaadinIcon.COG.create()));
        }

        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }

    private Component createProfileMenu() {
        Person loggedInUser = (Person) VaadinSession.getCurrent().getAttribute(Person.class);

        if (loggedInUser == null) {
            // Show login prompt if not logged in
            return createLoginPrompt();
        }

        String displayName = loggedInUser.getName() != null
                ? loggedInUser.getName()
                : loggedInUser.getUsername();

        var avatar = new Avatar(displayName);
        avatar.addThemeVariants(AvatarVariant.LUMO_SMALL);
        avatar.addClassNames(Margin.Right.SMALL);

        // Try to load profile image if available
        try {
            UserPreferences preferences = settingsService.getUserPreferences(loggedInUser);
            if (preferences.getProfileImagePath() != null &&
                fileStorageService.fileExists(preferences.getProfileImagePath())) {

                StreamResource imageResource = new StreamResource(
                    "profile-image",
                    () -> {
                        try {
                            return Files.newInputStream(
                                fileStorageService.getFilePath(preferences.getProfileImagePath())
                            );
                        } catch (Exception e) {
                            return new ByteArrayInputStream(new byte[0]);
                        }
                    }
                );
                avatar.setImageResource(imageResource);
                avatar.setName(null);
            }
        } catch (Exception ignored) {
        }

        var profileMenu = new MenuBar();
        profileMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        profileMenu.addClassNames(Margin.MEDIUM, Padding.MEDIUM);

        var profileMenuItem = profileMenu.addItem(avatar);

        // Profile Information Section
        profileMenuItem.getSubMenu().addItem(createProfileHeader(loggedInUser)).setEnabled(false);
        profileMenuItem.getSubMenu().add(new Hr());

        // Action Items
        profileMenuItem.getSubMenu().addItem("View Profile", e -> UI.getCurrent().navigate("profile"))
            .add(VaadinIcon.USER.create());

        profileMenuItem.getSubMenu().addItem("Settings", e -> UI.getCurrent().navigate("settings"))
            .add(VaadinIcon.COG.create());

        profileMenuItem.getSubMenu().addItem("Account Settings", e -> UI.getCurrent().navigate("account-settings"))
            .add(VaadinIcon.TOOLS.create());

        profileMenuItem.getSubMenu().add(new Hr());

        // Logout
        var logoutItem = profileMenuItem.getSubMenu().addItem("Logout", e -> logout());
        logoutItem.add(VaadinIcon.SIGN_OUT.create());
        logoutItem.getStyle().set("color", "var(--lumo-error-text-color)");

        return profileMenu;
    }

    private void applyThemeFromPreferences() {
        Person currentUser = (Person) VaadinSession.getCurrent().getAttribute(Person.class);
        if (currentUser == null) {
            return;
        }

        try {
            UserPreferences preferences = settingsService.getUserPreferences(currentUser);
            ThemeUtil.applyTheme(UI.getCurrent(), preferences.getTheme());
            ThemeUtil.applyColorScheme(UI.getCurrent(), preferences.getColorScheme());
        } catch (Exception e) {
            logger.debug("Unable to apply saved theme for user {}", currentUser.getUsername(), e);
        }
    }

    private Component createProfileHeader(Person user) {
        Div profileInfo = new Div();
        profileInfo.addClassNames(Padding.SMALL);
        profileInfo.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius)")
            .set("margin", "var(--lumo-space-xs)")
            .set("white-space", "nowrap")
            .set("max-width", "250px");

        // Name
        Span nameSpan = new Span(user.getName() != null ? user.getName() : "No Name Set");
        nameSpan.addClassNames(FontWeight.BOLD, FontSize.SMALL);
        nameSpan.getStyle().set("display", "block");

        // Username
        Span usernameSpan = new Span("@" + user.getUsername());
        usernameSpan.addClassNames(TextColor.SECONDARY, FontSize.SMALL);
        usernameSpan.getStyle().set("display", "block");

        // Email
        Span emailSpan = new Span(user.getEmail() != null ? user.getEmail() : "No email");
        emailSpan.addClassNames(TextColor.SECONDARY, FontSize.SMALL);
        emailSpan.getStyle().set("display", "block");

        profileInfo.add(nameSpan, usernameSpan, emailSpan);
        return profileInfo;
    }

    private Component createLoginPrompt() {
        // Fallback UI when not logged in
        var loginWrapper = new Div();
        loginWrapper.addClassNames(Padding.MEDIUM);

        var prompt = new Span("Not signed in");
        prompt.addClassNames(FontWeight.SEMIBOLD, FontSize.SMALL);

        var loginButton = VaadinIcon.SIGN_IN.create();
        loginButton.addClickListener(e -> UI.getCurrent().navigate("login"));

        loginWrapper.add(prompt, loginButton);
        return loginWrapper;
    }

    private void logout() {
        VaadinSession.getCurrent().getSession().invalidate();
        VaadinSession.getCurrent().close();
        UI.getCurrent().navigate("login");
    }
}
