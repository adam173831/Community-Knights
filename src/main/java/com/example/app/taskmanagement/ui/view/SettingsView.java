package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.component.ViewToolbar;
import com.example.app.base.ui.view.MainLayout;
import com.example.app.shared.domain.Person;
import com.example.app.shared.domain.UserPreferences;
import com.example.app.taskmanagement.service.DataExportService;
import com.example.app.taskmanagement.service.FileStorageService;
import com.example.app.taskmanagement.service.LoginService;
import com.example.app.taskmanagement.service.SettingsService;
import com.example.app.taskmanagement.ui.util.ThemeUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.nio.file.Files;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings - My App")
@PermitAll
public class SettingsView extends Main implements BeforeEnterObserver {

    private final SettingsService settingsService;
    private final LoginService loginService;
    private final FileStorageService fileStorageService;
    private final DataExportService dataExportService;

    private Person currentUser;
    private UserPreferences userPreferences;

    // UI Components
    private Avatar profileAvatar;
    private TextField nameField;
    private EmailField emailField;
    private TextField phoneField;
    private TextArea bioField;

    private Checkbox enablePushNotifications;
    private Checkbox enableDesktopAlerts;
    private Checkbox notifyTaskUpdates;
    private Checkbox notifyUserMentions;
    private Checkbox notifySystemAlerts;

    private RadioButtonGroup<String> themeSelection;
    private ComboBox<String> customColorScheme;
    private Checkbox enableAnimations;

    private ComboBox<String> exportFormat;
    private Checkbox includePersonalData;
    private Checkbox includeTaskData;
    private Checkbox autoBackupEnabled;
    private ComboBox<String> backupFrequency;

    private ComboBox<String> importFormat;
    private Checkbox autoSyncEnabled;
    private ComboBox<String> syncFrequency;

    private Button savePersonalInfoButton;
    private Button saveNotificationSettingsButton;
    private Button saveThemeSettingsButton;
    private Button saveDataSettingsButton;
    private Button exportNowButton;
    private Button performSyncButton;
    private Button saveSyncSettingsButton;

    @SuppressWarnings("unused")
    private final LoginService unusedLoginServiceForUiAccess; // if needed elsewhere

    public SettingsView(
            SettingsService settingsService,
            LoginService loginService,
            FileStorageService fileStorageService,
            DataExportService dataExportService
    ) {
        this.settingsService = settingsService;
        this.loginService = loginService;
        this.fileStorageService = fileStorageService;
        this.dataExportService = dataExportService;
        this.unusedLoginServiceForUiAccess = loginService;
        createUI();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        currentUser = (Person) VaadinSession.getCurrent().getAttribute(Person.class);
        if (currentUser == null) {
            event.forwardTo("login");
            return;
        }

        userPreferences = settingsService.getUserPreferences(currentUser);
        loadUserPreferencesIntoUI();
    }

    private void createUI() {
        addClassName("settings-view");

        ViewToolbar toolbar = new ViewToolbar("Settings");

        Tabs topTabs = new Tabs();
        Tab profileTab = new Tab("Profile");
        Tab notificationsTab = new Tab("Notifications");
        Tab appearanceTab = new Tab("Appearance");
        Tab dataTab = new Tab("Data & Privacy");
        Tab syncTab = new Tab("Import / Sync");

        topTabs.add(profileTab, notificationsTab, appearanceTab, dataTab, syncTab);

        Div profileContent = buildProfileSection();
        Div notificationsContent = buildNotificationsSection();
        Div appearanceContent = buildAppearanceSection();
        Div dataContent = buildDataSection();
        Div syncContent = buildSyncSection();

        Div pages = new Div(profileContent, notificationsContent, appearanceContent, dataContent, syncContent);
        pages.setSizeFull();

        topTabs.addSelectedChangeListener(event -> {
            profileContent.setVisible(event.getSelectedTab() == profileTab);
            notificationsContent.setVisible(event.getSelectedTab() == notificationsTab);
            appearanceContent.setVisible(event.getSelectedTab() == appearanceTab);
            dataContent.setVisible(event.getSelectedTab() == dataTab);
            syncContent.setVisible(event.getSelectedTab() == syncTab);
        });

        // default visible
        profileContent.setVisible(true);
        notificationsContent.setVisible(false);
        appearanceContent.setVisible(false);
        dataContent.setVisible(false);
        syncContent.setVisible(false);

        add(toolbar, topTabs, pages);
    }

    private Div buildProfileSection() {
        Div wrapper = new Div();
        wrapper.addClassName("settings-section");

        H2 header = new H2("Profile");
        Paragraph desc = new Paragraph("Your personal info and account identity.");

        profileAvatar = new Avatar();
        profileAvatar.addThemeVariants(AvatarVariant.LUMO_LARGE);

        nameField = new TextField("Full Name");
        emailField = new EmailField("Email");
        phoneField = new TextField("Phone Number");
        bioField = new TextArea("About You / Bio");

        savePersonalInfoButton = new Button("Save Personal Info", e -> savePersonalInfo());
        savePersonalInfoButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        FormLayout form = new FormLayout(
            nameField,
            emailField,
            phoneField,
            bioField,
            savePersonalInfoButton
        );
        form.setColspan(bioField, 2);
        form.setColspan(savePersonalInfoButton, 2);

        wrapper.add(header, desc, profileAvatar, form);
        return wrapper;
    }

    private Div buildNotificationsSection() {
        Div wrapper = new Div();
        wrapper.addClassName("settings-section");

        H2 header = new H2("Notifications");
        Paragraph desc = new Paragraph("How you get alerts and updates.");

        enablePushNotifications = new Checkbox("Enable push notifications");
        enableDesktopAlerts = new Checkbox("Enable desktop alerts");
        notifyTaskUpdates = new Checkbox("Notify about task updates");
        notifyUserMentions = new Checkbox("Notify when someone mentions me");
        notifySystemAlerts = new Checkbox("Critical system alerts");

        saveNotificationSettingsButton = new Button("Save Notification Settings", e -> saveNotificationSettings());
        saveNotificationSettingsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        FormLayout form = new FormLayout(
            enablePushNotifications,
            enableDesktopAlerts,
            notifyTaskUpdates,
            notifyUserMentions,
            notifySystemAlerts,
            saveNotificationSettingsButton
        );
        form.setColspan(saveNotificationSettingsButton, 2);

        wrapper.add(header, desc, form);
        return wrapper;
    }

    private Div buildAppearanceSection() {
        Div wrapper = new Div();
        wrapper.addClassName("settings-section");

        H2 header = new H2("Appearance");
        Paragraph desc = new Paragraph("Theme, colors, and motion.");

        themeSelection = new RadioButtonGroup<>();
        themeSelection.setLabel("Theme Mode");
        themeSelection.setItems("System Default", "Light", "Dark");

        customColorScheme = new ComboBox<>("Accent Color");
        customColorScheme.setItems(
            "Blue (Default)",
            "Green",
            "Purple",
            "Red",
            "Orange"
        );

        enableAnimations = new Checkbox("Enable subtle animations");

        saveThemeSettingsButton = new Button("Save Theme Settings", e -> saveThemeSettings());
        saveThemeSettingsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        FormLayout form = new FormLayout(
            themeSelection,
            customColorScheme,
            enableAnimations,
            saveThemeSettingsButton
        );
        form.setColspan(saveThemeSettingsButton, 2);

        wrapper.add(header, desc, form);
        return wrapper;
    }

    private Div buildDataSection() {
        Div wrapper = new Div();
        wrapper.addClassName("settings-section");

        H2 header = new H2("Data & Privacy");
        Paragraph desc = new Paragraph("Export data, backups, and retention.");

        exportFormat = new ComboBox<>("Export Format");
        exportFormat.setItems("JSON", "CSV", "PDF");

        includePersonalData = new Checkbox("Include personal profile data");
        includeTaskData = new Checkbox("Include task / activity data");

        autoBackupEnabled = new Checkbox("Enable automatic backups");

        backupFrequency = new ComboBox<>("Backup Frequency");
        backupFrequency.setItems("Daily", "Weekly", "Monthly");

        autoBackupEnabled.addValueChangeListener(e ->
            backupFrequency.setEnabled(e.getValue() != null && e.getValue())
        );

        saveDataSettingsButton = new Button("Save Data Settings", e -> saveDataSettings());
        saveDataSettingsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        exportNowButton = new Button("Export My Data", e -> exportDataNow());
        exportNowButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(
            exportFormat,
            includePersonalData,
            includeTaskData,
            autoBackupEnabled,
            backupFrequency,
            saveDataSettingsButton,
            exportNowButton
        );
        form.setColspan(saveDataSettingsButton, 1);
        form.setColspan(exportNowButton, 1);

        wrapper.add(header, desc, form);
        return wrapper;
    }

    private Div buildSyncSection() {
        Div wrapper = new Div();
        wrapper.addClassName("settings-section");

        H2 header = new H2("Import / Sync");
        Paragraph desc = new Paragraph("Bring in data from other systems, or sync updates.");

        importFormat = new ComboBox<>("Preferred Import Format");
        importFormat.setItems("CSV", "JSON", "XML");

        autoSyncEnabled = new Checkbox("Enable automatic sync");

        syncFrequency = new ComboBox<>("Sync Frequency");
        syncFrequency.setItems("Hourly", "Daily", "Weekly");

        autoSyncEnabled.addValueChangeListener(e ->
            syncFrequency.setEnabled(e.getValue() != null && e.getValue())
        );

        saveSyncSettingsButton = new Button("Save Sync Settings", e -> saveSyncSettings());
        saveSyncSettingsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        performSyncButton = new Button("Sync Now", e -> performManualSync());
        performSyncButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        FormLayout form = new FormLayout(
            importFormat,
            autoSyncEnabled,
            syncFrequency,
            saveSyncSettingsButton,
            performSyncButton
        );
        form.setColspan(saveSyncSettingsButton, 1);
        form.setColspan(performSyncButton, 1);

        wrapper.add(header, desc, form);
        return wrapper;
    }

    private void loadUserPreferencesIntoUI() {
        if (currentUser == null || userPreferences == null) {
            return;
        }

        // Load personal information
        nameField.setValue(currentUser.getName() != null ? currentUser.getName() : "");
        emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        phoneField.setValue(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
        bioField.setValue(userPreferences.getBio() != null ? userPreferences.getBio() : "");

        // Set avatar (either from uploaded profile image or fallback to initials)
        if (userPreferences.getProfileImagePath() != null &&
            fileStorageService.fileExists(userPreferences.getProfileImagePath())) {

            updateAvatarDisplay(userPreferences.getProfileImagePath());
        } else {
            String displayName = currentUser.getName() != null
                    ? currentUser.getName()
                    : currentUser.getUsername();
            profileAvatar.setName(displayName);
            profileAvatar.setImage(null);
        }

        // Notification settings
        enablePushNotifications.setValue(userPreferences.isEnablePushNotifications());
        enableDesktopAlerts.setValue(userPreferences.isEnableDesktopAlerts());
        notifyTaskUpdates.setValue(userPreferences.isNotifyTaskUpdates());
        notifyUserMentions.setValue(userPreferences.isNotifyUserMentions());
        notifySystemAlerts.setValue(userPreferences.isNotifySystemAlerts());

        // Theme settings
        themeSelection.setValue(
            userPreferences.getTheme() != null
                ? userPreferences.getTheme()
                : "System Default"
        );
        customColorScheme.setValue(
            userPreferences.getColorScheme() != null
                ? userPreferences.getColorScheme()
                : "Blue (Default)"
        );
        enableAnimations.setValue(userPreferences.isEnableAnimations());

        ThemeUtil.applyTheme(UI.getCurrent(), userPreferences.getTheme());
        ThemeUtil.applyColorScheme(UI.getCurrent(), userPreferences.getColorScheme());

        // Data settings
        exportFormat.setValue(userPreferences.getExportFormat());
        includePersonalData.setValue(userPreferences.isIncludePersonalData());
        includeTaskData.setValue(userPreferences.isIncludeTaskData());
        autoBackupEnabled.setValue(userPreferences.isAutoBackupEnabled());
        backupFrequency.setValue(userPreferences.getBackupFrequency());
        backupFrequency.setEnabled(userPreferences.isAutoBackupEnabled());

        // Import / sync settings
        importFormat.setValue(userPreferences.getImportFormat());
        autoSyncEnabled.setValue(userPreferences.isAutoSyncEnabled());
        syncFrequency.setValue(userPreferences.getSyncFrequency());
        syncFrequency.setEnabled(userPreferences.isAutoSyncEnabled());
    }

    private void updateAvatarDisplay(String imagePath) {
        if (imagePath != null && fileStorageService.fileExists(imagePath)) {
            try {
                StreamResource imageResource = new StreamResource(
                    "profile-image",
                    () -> {
                        try {
                            return Files.newInputStream(
                                fileStorageService.getFilePath(imagePath)
                            );
                        } catch (Exception e) {
                            return null;
                        }
                    }
                );
                profileAvatar.setImageResource(imageResource);
                profileAvatar.setName(null);
            } catch (Exception e) {
                profileAvatar.setImage(null);
            }
        }
    }

    private void savePersonalInfo() {
        try {
            currentUser.setName(nameField.getValue());
            currentUser.setEmail(emailField.getValue());
            currentUser.setPhoneNumber(phoneField.getValue());

            userPreferences.setBio(bioField.getValue());

            settingsService.updatePersonalInfo(currentUser, userPreferences);

            VaadinSession.getCurrent().setAttribute(Person.class, currentUser);

            showSuccessNotification("Personal information saved successfully!");
        } catch (Exception e) {
            showErrorNotification("Failed to save personal information: " + e.getMessage());
        }
    }

    private void saveNotificationSettings() {
        try {
            settingsService.updateNotificationSettings(
                currentUser,
                enablePushNotifications.getValue(),
                enableDesktopAlerts.getValue(),
                notifyTaskUpdates.getValue(),
                notifyUserMentions.getValue(),
                notifySystemAlerts.getValue()
            );
            showSuccessNotification("Notification settings saved successfully!");
        } catch (Exception e) {
            showErrorNotification("Failed to save notification settings: " + e.getMessage());
        }
    }

    private void saveThemeSettings() {
        try {
            String selectedTheme = themeSelection.getValue() != null
                ? themeSelection.getValue()
                : "System Default";
            String selectedScheme = customColorScheme.getValue() != null
                ? customColorScheme.getValue()
                : "Blue (Default)";

            settingsService.updateThemeSettings(
                currentUser,
                selectedTheme,
                selectedScheme,
                enableAnimations.getValue()
            );

            ThemeUtil.applyTheme(UI.getCurrent(), selectedTheme);
            ThemeUtil.applyColorScheme(UI.getCurrent(), selectedScheme);

            showSuccessNotification("Theme settings applied successfully!");
        } catch (Exception e) {
            showErrorNotification("Failed to save theme settings: " + e.getMessage());
        }
    }

    private void saveDataSettings() {
        try {
            settingsService.updateDataSettings(
                currentUser,
                exportFormat.getValue(),
                includePersonalData.getValue(),
                includeTaskData.getValue(),
                autoBackupEnabled.getValue(),
                backupFrequency.getValue()
            );
            showSuccessNotification("Data management settings saved successfully!");
        } catch (Exception e) {
            showErrorNotification("Failed to save data settings: " + e.getMessage());
        }
    }

    private void saveSyncSettings() {
        try {
            settingsService.updateSyncSettings(
                currentUser,
                importFormat.getValue(),
                autoSyncEnabled.getValue(),
                syncFrequency.getValue()
            );
            showSuccessNotification("Sync settings saved successfully!");
        } catch (Exception e) {
            showErrorNotification("Failed to save sync settings: " + e.getMessage());
        }
    }

    private void exportDataNow() {
        try {
            byte[] exported = dataExportService.exportUserData(
                currentUser,
                exportFormat.getValue(),
                includePersonalData.getValue(),
                includeTaskData.getValue()
            );

            // TODO: stream `exported` to the browser as a download
            showInfoNotification("Your data export is ready.");

        } catch (Exception e) {
            showErrorNotification("Failed to export data: " + e.getMessage());
        }
    }

    private void performManualSync() {
        try {
            showInfoNotification("Starting manual synchronization...");

            UI.getCurrent().access(() -> {
                try {
                    Thread.sleep(2000);
                    showSuccessNotification("Synchronization completed successfully!");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showErrorNotification("Synchronization was interrupted");
                }
            });

        } catch (Exception e) {
            showErrorNotification("Synchronization failed: " + e.getMessage());
        }
    }

    private void showSuccessNotification(String message) {
        Notification notification =
            Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification =
            Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showInfoNotification(String message) {
        Notification notification =
            Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }
}
