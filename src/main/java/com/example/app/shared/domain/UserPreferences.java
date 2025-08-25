package com.example.app.shared.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Person user;

    // Personal Information
    @Column(length = 1000)
    private String bio;

    @Column(length = 255)
    private String profileImagePath;

    // Notification Settings
    @Column(nullable = false)
    private boolean enablePushNotifications = true;

    @Column(nullable = false)
    private boolean enableDesktopAlerts = false;

    @Column(nullable = false)
    private boolean notifyTaskUpdates = true;

    @Column(nullable = false)
    private boolean notifyUserMentions = true;

    @Column(nullable = false)
    private boolean notifySystemAlerts = false;

    // Theme Settings
    @Column(length = 50, nullable = false)
    private String theme = "System Default"; // Light, Dark, System Default

    @Column(length = 50, nullable = false)
    private String colorScheme = "Blue (Default)";

    @Column(nullable = false)
    private boolean enableAnimations = true;

    // Data Management Settings
    @Column(length = 50, nullable = false)
    private String exportFormat = "JSON";

    @Column(nullable = false)
    private boolean includePersonalData = true;

    @Column(nullable = false)
    private boolean includeTaskData = true;

    @Column(nullable = false)
    private boolean autoBackupEnabled = false;

    @Column(length = 50, nullable = false)
    private String backupFrequency = "Weekly";

    // Import/Export Settings
    @Column(length = 50, nullable = false)
    private String importFormat = "JSON";

    @Column(nullable = false)
    private boolean autoSyncEnabled = false;

    @Column(length = 50, nullable = false)
    private String syncFrequency = "Manual only";

    // Constructors
    public UserPreferences() {}

    public UserPreferences(Person user) {
        this.user = user;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Person getUser() { return user; }
    public void setUser(Person user) { this.user = user; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImagePath() { return profileImagePath; }
    public void setProfileImagePath(String profileImagePath) { this.profileImagePath = profileImagePath; }

    public boolean isEnablePushNotifications() { return enablePushNotifications; }
    public void setEnablePushNotifications(boolean enablePushNotifications) { this.enablePushNotifications = enablePushNotifications; }

    public boolean isEnableDesktopAlerts() { return enableDesktopAlerts; }
    public void setEnableDesktopAlerts(boolean enableDesktopAlerts) { this.enableDesktopAlerts = enableDesktopAlerts; }

    public boolean isNotifyTaskUpdates() { return notifyTaskUpdates; }
    public void setNotifyTaskUpdates(boolean notifyTaskUpdates) { this.notifyTaskUpdates = notifyTaskUpdates; }

    public boolean isNotifyUserMentions() { return notifyUserMentions; }
    public void setNotifyUserMentions(boolean notifyUserMentions) { this.notifyUserMentions = notifyUserMentions; }

    public boolean isNotifySystemAlerts() { return notifySystemAlerts; }
    public void setNotifySystemAlerts(boolean notifySystemAlerts) { this.notifySystemAlerts = notifySystemAlerts; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getColorScheme() { return colorScheme; }
    public void setColorScheme(String colorScheme) { this.colorScheme = colorScheme; }

    public boolean isEnableAnimations() { return enableAnimations; }
    public void setEnableAnimations(boolean enableAnimations) { this.enableAnimations = enableAnimations; }

    public String getExportFormat() { return exportFormat; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }

    public boolean isIncludePersonalData() { return includePersonalData; }
    public void setIncludePersonalData(boolean includePersonalData) { this.includePersonalData = includePersonalData; }

    public boolean isIncludeTaskData() { return includeTaskData; }
    public void setIncludeTaskData(boolean includeTaskData) { this.includeTaskData = includeTaskData; }

    public boolean isAutoBackupEnabled() { return autoBackupEnabled; }
    public void setAutoBackupEnabled(boolean autoBackupEnabled) { this.autoBackupEnabled = autoBackupEnabled; }

    public String getBackupFrequency() { return backupFrequency; }
    public void setBackupFrequency(String backupFrequency) { this.backupFrequency = backupFrequency; }

    public String getImportFormat() { return importFormat; }
    public void setImportFormat(String importFormat) { this.importFormat = importFormat; }

    public boolean isAutoSyncEnabled() { return autoSyncEnabled; }
    public void setAutoSyncEnabled(boolean autoSyncEnabled) { this.autoSyncEnabled = autoSyncEnabled; }

    public String getSyncFrequency() { return syncFrequency; }
    public void setSyncFrequency(String syncFrequency) { this.syncFrequency = syncFrequency; }
}