package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.component.ViewToolbar;
import com.example.app.base.ui.view.MainLayout;
import com.example.app.shared.domain.Person;
import com.example.app.shared.domain.UserPreferences;
import com.example.app.taskmanagement.service.DataExportService;
import com.example.app.taskmanagement.service.FileStorageService;
import com.example.app.taskmanagement.service.LoginService;
import com.example.app.taskmanagement.service.SettingsService;
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
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Route(value = "settings", layout = MainLayout.class)
@PageTitle("Settings - My App")
@PermitAll
public class SettingsView extends Main implements BeforeEnterObserver {

    private final LoginService loginService;
    private final SettingsService settingsService;
    private final DataExportService dataExportService;
    private final FileStorageService fileStorageService;
    
    private Person currentUser;
    private UserPreferences userPreferences;

    // Tabs
    private Tabs tabs;
    private Map<Tab, VerticalLayout> tabContent = new HashMap<>();

    // Personal Information
    private Avatar profileAvatar;
    private Upload profileImageUpload;
    private TextField nameField;
    private EmailField emailField;
    private TextField phoneField;
    private TextArea bioField;

    // Notification Settings
    private Checkbox enablePushNotifications;
    private Checkbox enableDesktopAlerts;
    private Checkbox notifyTaskUpdates;
    private Checkbox notifyUserMentions;
    private Checkbox notifySystemAlerts;

    // Theme Settings
    private RadioButtonGroup<String> themeSelection;
    private ComboBox<String> customColorScheme;
    private Checkbox enableAnimations;

    // Data Management
    private ComboBox<String> exportFormat;
    private Checkbox includePersonalData;
    private Checkbox includeTaskData;
    private ComboBox<String> backupFrequency;
    private Checkbox autoBackupEnabled;

    // Import/Export
    private ComboBox<String> importFormat;
    private Upload importDataUpload;
    private ComboBox<String> syncFrequency;
    private Checkbox autoSyncEnabled;

    public SettingsView(LoginService loginService, SettingsService settingsService, 
                       DataExportService dataExportService, FileStorageService fileStorageService) {
        this.loginService = loginService;
        this.settingsService = settingsService;
        this.dataExportService = dataExportService;
        this.fileStorageService = fileStorageService;
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
        loadUserSettings();
    }

    private void createUI() {
        setSizeFull();
        addClassName("settings-view");

        // Create tabs
        createTabs();

        // Create content for each tab
        createPersonalInformationTab();
        createNotificationTab();
        createThemeTab();
        createDataManagementTab();
        createImportExportTab();

        // Main layout
        VerticalLayout content = new VerticalLayout();
        content.setMaxWidth("900px");
        content.setMargin(true);
        content.setSpacing(true);

        // Show first tab content by default
        Div contentContainer = new Div();
        contentContainer.addClassName("tab-content-container");
        contentContainer.add(tabContent.get(tabs.getTabAt(0)));

        // Tab selection listener
        tabs.addSelectedChangeListener(event -> {
            contentContainer.removeAll();
            contentContainer.add(tabContent.get(event.getSelectedTab()));
        });

        content.add(tabs, contentContainer);
        
        add(new ViewToolbar("Settings"));
        add(content);
    }

    private void createTabs() {
        Tab personalTab = new Tab("Personal Information");
        Tab notificationTab = new Tab("Notifications");
        Tab themeTab = new Tab("Appearance");
        Tab dataTab = new Tab("Data Management");
        Tab importExportTab = new Tab("Import/Export");

        tabs = new Tabs(personalTab, notificationTab, themeTab, dataTab, importExportTab);
        tabs.setWidthFull();
    }

    private void createPersonalInformationTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        H3 header = new H3("Personal Information");
        Paragraph description = new Paragraph("Manage your personal details and profile picture.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Profile Picture Section
        Div profileSection = new Div();
        profileSection.addClassName("profile-picture-section");
        profileSection.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-l)")
            .set("margin-bottom", "var(--lumo-space-l)");

        H4 profileHeader = new H4("Profile Picture");
        profileAvatar = new Avatar();
        profileAvatar.addThemeVariants(AvatarVariant.LUMO_XLARGE);
        profileAvatar.setColorIndex(5);

        MemoryBuffer profileBuffer = new MemoryBuffer();
        profileImageUpload = new Upload(profileBuffer);
        profileImageUpload.setAcceptedFileTypes("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp");
        profileImageUpload.setMaxFileSize(10 * 1024 * 1024); // 10MB
        profileImageUpload.setDropLabel(new Span("Drop image here or click to upload"));
        profileImageUpload.setUploadButton(new Button("Change Picture"));

        // Add file validation
        profileImageUpload.addFileRejectedListener(event -> {
            showErrorNotification("File rejected: " + event.getErrorMessage());
        });

        profileImageUpload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                String mimeType = event.getMIMEType();
                
                // Validate it's actually an image
                if (!mimeType.startsWith("image/")) {
                    showErrorNotification("Please upload an image file only");
                    return;
                }
                
                // Store the file
                String storedFileName = fileStorageService.storeFile(
                    new ByteArrayInputStream(profileBuffer.getInputStream().readAllBytes()),
                    fileName
                );
                
                // Update user preferences with new image path
                settingsService.updatePersonalInformation(currentUser, 
                    bioField.getValue(), storedFileName);
                
                // Update the avatar display immediately
                updateAvatarDisplay(storedFileName);
                
                showSuccessNotification("Profile picture updated successfully!");
                
            } catch (Exception e) {
                showErrorNotification("Failed to upload profile picture: " + e.getMessage());
            }
        });

        profileImageUpload.addFailedListener(event -> {
            showErrorNotification("Upload failed: " + event.getReason().getMessage());
        });

        HorizontalLayout profileLayout = new HorizontalLayout(profileAvatar, profileImageUpload);
        profileLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
        profileLayout.setSpacing(true);

        profileSection.add(profileHeader, profileLayout);

        // Personal Details Form
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        nameField = new TextField("Full Name");
        nameField.setPlaceholder("Enter your full name");

        emailField = new EmailField("Email Address");
        emailField.setPlaceholder("your.email@example.com");

        phoneField = new TextField("Phone Number");
        phoneField.setPlaceholder("+1 (555) 123-4567");

        bioField = new TextArea("Bio");
        bioField.setPlaceholder("Tell us about yourself...");
        bioField.setMaxLength(500);
        bioField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        bioField.addValueChangeListener(e -> {
            int remaining = 500 - e.getValue().length();
            bioField.setHelperText(remaining + " characters remaining");
        });

        form.add(nameField, emailField);
        form.add(phoneField);
        form.setColspan(bioField, 2);
        form.add(bioField);

        Button savePersonalButton = new Button("Save Changes", e -> savePersonalInformation());
        savePersonalButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(header, description, profileSection, form, savePersonalButton);
        tabContent.put(tabs.getTabAt(0), layout);
    }

    private void createNotificationTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        H3 header = new H3("Notification Settings");
        Paragraph description = new Paragraph("Configure how and when you receive notifications.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Push Notifications Section
        Div pushSection = createSettingsSection("Push Notifications", 
            "Receive notifications on your device even when the app is closed.");

        enablePushNotifications = new Checkbox("Enable push notifications");
        enableDesktopAlerts = new Checkbox("Enable desktop alerts");
        enableDesktopAlerts.setHelperText("Show notifications on your desktop");

        pushSection.add(enablePushNotifications, enableDesktopAlerts);

        // App Notifications Section
        Div appSection = createSettingsSection("In-App Notifications", 
            "Control which events trigger notifications while using the app.");

        notifyTaskUpdates = new Checkbox("Task updates and changes");
        notifyUserMentions = new Checkbox("When someone mentions you");
        notifySystemAlerts = new Checkbox("System alerts and maintenance");

        appSection.add(notifyTaskUpdates, notifyUserMentions, notifySystemAlerts);

        Button saveNotificationButton = new Button("Save Notification Settings", e -> saveNotificationSettings());
        saveNotificationButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(header, description, pushSection, appSection, saveNotificationButton);
        tabContent.put(tabs.getTabAt(1), layout);
    }

    private void createThemeTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        H3 header = new H3("Appearance Settings");
        Paragraph description = new Paragraph("Customize the look and feel of your application.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Theme Selection Section
        Div themeSection = createSettingsSection("Theme Selection", 
            "Choose between light and dark themes or let the system decide.");

        themeSelection = new RadioButtonGroup<>();
        themeSelection.setLabel("Color Theme");
        themeSelection.setItems("Light", "Dark", "System Default");
        themeSelection.setValue("System Default");

        // Custom Color Scheme
        customColorScheme = new ComboBox<>("Color Scheme");
        customColorScheme.setItems("Blue (Default)", "Green", "Purple", "Red", "Orange");
        customColorScheme.setValue("Blue (Default)");
        customColorScheme.setHelperText("Accent color for buttons and highlights");

        // Animation Settings
        enableAnimations = new Checkbox("Enable animations and transitions");
        enableAnimations.setValue(true);
        enableAnimations.setHelperText("Smooth transitions and visual effects");

        themeSection.add(themeSelection, customColorScheme, enableAnimations);

        Button saveThemeButton = new Button("Apply Theme Settings", e -> saveThemeSettings());
        saveThemeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(header, description, themeSection, saveThemeButton);
        tabContent.put(tabs.getTabAt(2), layout);
    }

    private void createDataManagementTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        H3 header = new H3("Data Management");
        Paragraph description = new Paragraph("Export your data and configure backup settings.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Export Data Section
        Div exportSection = createSettingsSection("Export Data", 
            "Download your data in various formats for backup or migration.");

        exportFormat = new ComboBox<>("Export Format");
        exportFormat.setItems("JSON", "CSV", "Excel (XLSX)", "PDF Report");
        exportFormat.setValue("JSON");

        includePersonalData = new Checkbox("Include personal information");
        includePersonalData.setValue(true);

        includeTaskData = new Checkbox("Include task and project data");
        includeTaskData.setValue(true);

        Button exportButton = new Button("Export Data", e -> exportData());
        exportButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        exportSection.add(exportFormat, includePersonalData, includeTaskData, exportButton);

        // Backup Settings Section
        Div backupSection = createSettingsSection("Backup Settings", 
            "Configure automatic backups of your data.");

        autoBackupEnabled = new Checkbox("Enable automatic backups");
        autoBackupEnabled.setValue(false);

        backupFrequency = new ComboBox<>("Backup Frequency");
        backupFrequency.setItems("Daily", "Weekly", "Monthly");
        backupFrequency.setValue("Weekly");
        backupFrequency.setEnabled(false);

        // Enable/disable backup frequency based on auto backup checkbox
        autoBackupEnabled.addValueChangeListener(e -> 
            backupFrequency.setEnabled(e.getValue()));

        backupSection.add(autoBackupEnabled, backupFrequency);

        Button saveDataButton = new Button("Save Data Settings", e -> saveDataSettings());
        saveDataButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(header, description, exportSection, backupSection, saveDataButton);
        tabContent.put(tabs.getTabAt(3), layout);
    }

    private void createImportExportTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        H3 header = new H3("Import & Export");
        Paragraph description = new Paragraph("Import data from other systems and configure synchronization.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Import Data Section
        Div importSection = createSettingsSection("Import Data", 
            "Upload data from other applications or previous exports.");

        importFormat = new ComboBox<>("Import Format");
        importFormat.setItems("JSON", "CSV", "Excel (XLSX)");
        importFormat.setValue("JSON");

        MemoryBuffer importBuffer = new MemoryBuffer();
        importDataUpload = new Upload(importBuffer);
        importDataUpload.setAcceptedFileTypes(".json", ".csv", ".xlsx");
        importDataUpload.setMaxFileSize(10 * 1024 * 1024); // 10MB
        importDataUpload.setUploadButton(new Button("Choose File"));

        importDataUpload.addSucceededListener(event -> {
            try {
                // Process the uploaded file
                String fileName = event.getFileName();
                String contentType = event.getMIMEType();
                
                if (fileStorageService.isValidDataFile(fileName)) {
                    // Store the file temporarily
                    String storedFileName = fileStorageService.storeFile(
                        new ByteArrayInputStream(importBuffer.getInputStream().readAllBytes()),
                        fileName
                    );
                    
                    showSuccessNotification("Data file uploaded successfully! Processing import...");
                    
                    // In a real implementation, you'd process the file content here
                    // For now, just clean up the temp file
                    fileStorageService.deleteFile(storedFileName);
                    
                } else {
                    showErrorNotification("Invalid file format. Please upload JSON, CSV, or Excel files only.");
                }
                
            } catch (Exception e) {
                showErrorNotification("Failed to process import file: " + e.getMessage());
            }
        });

        importSection.add(importFormat, importDataUpload);

        // Sync Settings Section
        Div syncSection = createSettingsSection("Synchronization", 
            "Configure automatic synchronization with external systems.");

        autoSyncEnabled = new Checkbox("Enable automatic synchronization");
        autoSyncEnabled.setValue(false);

        syncFrequency = new ComboBox<>("Sync Frequency");
        syncFrequency.setItems("Every 15 minutes", "Hourly", "Daily", "Manual only");
        syncFrequency.setValue("Manual only");
        syncFrequency.setEnabled(false);

        // Enable/disable sync frequency based on auto sync checkbox
        autoSyncEnabled.addValueChangeListener(e -> 
            syncFrequency.setEnabled(e.getValue()));

        Button manualSyncButton = new Button("Sync Now", e -> performManualSync());
        manualSyncButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        syncSection.add(autoSyncEnabled, syncFrequency, manualSyncButton);

        Button saveSyncButton = new Button("Save Sync Settings", e -> saveSyncSettings());
        saveSyncButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(header, description, importSection, syncSection, saveSyncButton);
        tabContent.put(tabs.getTabAt(4), layout);
    }

    private Div createSettingsSection(String title, String description) {
        Div section = new Div();
        section.addClassName("settings-section");
        section.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-l)")
            .set("padding", "var(--lumo-space-l)")
            .set("margin-bottom", "var(--lumo-space-l)");

        H4 sectionHeader = new H4(title);
        sectionHeader.getStyle().set("margin-top", "0");

        Paragraph sectionDescription = new Paragraph(description);
        sectionDescription.getStyle().set("color", "var(--lumo-secondary-text-color)");

        section.add(sectionHeader, sectionDescription);
        return section;
    }

    private void loadUserSettings() {
        if (currentUser == null || userPreferences == null) return;

        // Load personal information
        nameField.setValue(currentUser.getName() != null ? currentUser.getName() : "");
        emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        phoneField.setValue(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
        bioField.setValue(userPreferences.getBio() != null ? userPreferences.getBio() : "");
        
        // Set avatar - either from uploaded image or name
        if (userPreferences.getProfileImagePath() != null && 
            fileStorageService.fileExists(userPreferences.getProfileImagePath())) {
            updateAvatarDisplay(userPreferences.getProfileImagePath());
        } else {
            String displayName = currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername();
            profileAvatar.setName(displayName);
            profileAvatar.setImage(null); // Clear any existing image
        }

        // Load notification settings
        enablePushNotifications.setValue(userPreferences.isEnablePushNotifications());
        enableDesktopAlerts.setValue(userPreferences.isEnableDesktopAlerts());
        notifyTaskUpdates.setValue(userPreferences.isNotifyTaskUpdates());
        notifyUserMentions.setValue(userPreferences.isNotifyUserMentions());
        notifySystemAlerts.setValue(userPreferences.isNotifySystemAlerts());

        // Load theme settings
        themeSelection.setValue(userPreferences.getTheme());
        customColorScheme.setValue(userPreferences.getColorScheme());
        enableAnimations.setValue(userPreferences.isEnableAnimations());

        // Load data management settings
        exportFormat.setValue(userPreferences.getExportFormat());
        includePersonalData.setValue(userPreferences.isIncludePersonalData());
        includeTaskData.setValue(userPreferences.isIncludeTaskData());
        autoBackupEnabled.setValue(userPreferences.isAutoBackupEnabled());
        backupFrequency.setValue(userPreferences.getBackupFrequency());
        backupFrequency.setEnabled(userPreferences.isAutoBackupEnabled());

        // Load import/export settings
        importFormat.setValue(userPreferences.getImportFormat());
        autoSyncEnabled.setValue(userPreferences.isAutoSyncEnabled());
        syncFrequency.setValue(userPreferences.getSyncFrequency());
        syncFrequency.setEnabled(userPreferences.isAutoSyncEnabled());
    }

    private void updateAvatarDisplay(String imagePath) {
        if (imagePath != null && fileStorageService.fileExists(imagePath)) {
            try {
                // Create a StreamResource for the uploaded image
                StreamResource imageResource = new StreamResource(
                    "profile-image", 
                    () -> {
                        try {
                            return Files.newInputStream(fileStorageService.getFilePath(imagePath));
                        } catch (Exception e) {
                            return new ByteArrayInputStream(new byte[0]);
                        }
                    }
                );
                
                // Set the image on the avatar
                profileAvatar.setImageResource(imageResource);
                
                // Also update the name for fallback
                String displayName = currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername();
                profileAvatar.setName(displayName);
                
            } catch (Exception e) {
                // Fallback to name-based avatar if image loading fails
                String displayName = currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername();
                profileAvatar.setName(displayName);
                profileAvatar.setImage(null);
            }
        }
    }

    // Save methods
    private void savePersonalInformation() {
        try {
            // Update Person entity
            currentUser.setName(nameField.getValue().trim());
            currentUser.setEmail(emailField.getValue().trim());
            currentUser.setPhoneNumber(phoneField.getValue().trim());
            
            // Update bio in preferences
            settingsService.updatePersonalInformation(currentUser, bioField.getValue().trim(), null);

            // Save person changes
            loginService.save(currentUser);
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
            settingsService.updateThemeSettings(
                currentUser,
                themeSelection.getValue(),
                customColorScheme.getValue(),
                enableAnimations.getValue()
            );
            
            // Apply theme change to current session
            applyThemeChange(themeSelection.getValue());
            
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
            showSuccessNotification("Synchronization settings saved successfully!");
        } catch (Exception e) {
            showErrorNotification("Failed to save sync settings: " + e.getMessage());
        }
    }

    private void exportData() {
        try {
            String format = exportFormat.getValue();
            boolean includePersonal = includePersonalData.getValue();
            boolean includeTasks = includeTaskData.getValue();
            
            byte[] exportedData = dataExportService.exportUserData(
                currentUser, format, includePersonal, includeTasks
            );
            
            // Create download
            String fileName = "user_data_export_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                dataExportService.getFileExtension(format);
            
            StreamResource resource = new StreamResource(fileName, 
                () -> new ByteArrayInputStream(exportedData));
            resource.setContentType(dataExportService.getContentType(format));
            
            // Create download link
            UI.getCurrent().getPage().executeJs(
                "const link = document.createElement('a');" +
                "link.href = $0;" +
                "link.download = $1;" +
                "link.click();",
                resource,
                fileName
            );
            
            showSuccessNotification("Data export ready for download!");
            
        } catch (Exception e) {
            showErrorNotification("Failed to export data: " + e.getMessage());
        }
    }

    private void performManualSync() {
        try {
            // In a real implementation, this would sync with external systems
            showInfoNotification("Starting manual synchronization...");
            
            // Simulate sync process
            UI.getCurrent().access(() -> {
                try {
                    Thread.sleep(2000); // Simulate processing time
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

    private void applyThemeChange(String theme) {
        // Apply theme to current UI session
        UI current = UI.getCurrent();
        if (current != null) {
            switch (theme) {
                case "Dark" -> current.getElement().setAttribute("theme", "dark");
                case "Light" -> current.getElement().removeAttribute("theme");
                case "System Default" -> {
                    // Remove explicit theme to use system default
                    current.getElement().removeAttribute("theme");
                }
            }
        }
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