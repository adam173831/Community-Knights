package com.example.app.taskmanagement.service;

import com.example.app.shared.domain.Person;
import com.example.app.shared.domain.UserPreferences;
import com.example.app.shared.domain.UserPreferencesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class SettingsService {

    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

    private final UserPreferencesRepository preferencesRepository;

    public SettingsService(UserPreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }

    public UserPreferences getUserPreferences(Person user) {
        return preferencesRepository.findByUser(user)
                .orElseGet(() -> createDefaultPreferences(user));
    }

    public UserPreferences getUserPreferences(Long userId) {
        return preferencesRepository.findByUserId(userId)
                .orElse(null);
    }

    private UserPreferences createDefaultPreferences(Person user) {
        UserPreferences preferences = new UserPreferences(user);
        UserPreferences saved = preferencesRepository.save(preferences);
        logger.info("Created default preferences for user: {}", user.getUsername());
        return saved;
    }

    public UserPreferences savePreferences(UserPreferences preferences) {
        UserPreferences saved = preferencesRepository.save(preferences);
        logger.info("Saved preferences for user: {}", preferences.getUser().getUsername());
        return saved;
    }

    public void updatePersonalInformation(Person user, String bio, String profileImagePath) {
        UserPreferences preferences = getUserPreferences(user);
        preferences.setBio(bio);
        if (profileImagePath != null) {
            preferences.setProfileImagePath(profileImagePath);
        }
        savePreferences(preferences);
    }

    public void updateNotificationSettings(Person user, boolean pushNotifications, boolean desktopAlerts,
                                         boolean taskUpdates, boolean userMentions, boolean systemAlerts) {
        UserPreferences preferences = getUserPreferences(user);
        preferences.setEnablePushNotifications(pushNotifications);
        preferences.setEnableDesktopAlerts(desktopAlerts);
        preferences.setNotifyTaskUpdates(taskUpdates);
        preferences.setNotifyUserMentions(userMentions);
        preferences.setNotifySystemAlerts(systemAlerts);
        savePreferences(preferences);
    }

    public void updateThemeSettings(Person user, String theme, String colorScheme, boolean enableAnimations) {
        UserPreferences preferences = getUserPreferences(user);
        preferences.setTheme(theme);
        preferences.setColorScheme(colorScheme);
        preferences.setEnableAnimations(enableAnimations);
        savePreferences(preferences);
    }

    public void updateDataSettings(Person user, String exportFormat, boolean includePersonal, 
                                 boolean includeTask, boolean autoBackup, String backupFrequency) {
        UserPreferences preferences = getUserPreferences(user);
        preferences.setExportFormat(exportFormat);
        preferences.setIncludePersonalData(includePersonal);
        preferences.setIncludeTaskData(includeTask);
        preferences.setAutoBackupEnabled(autoBackup);
        preferences.setBackupFrequency(backupFrequency);
        savePreferences(preferences);
    }

    public void updateSyncSettings(Person user, String importFormat, boolean autoSync, String syncFrequency) {
        UserPreferences preferences = getUserPreferences(user);
        preferences.setImportFormat(importFormat);
        preferences.setAutoSyncEnabled(autoSync);
        preferences.setSyncFrequency(syncFrequency);
        savePreferences(preferences);
    }

    public void deleteUserPreferences(Long userId) {
        try {
            preferencesRepository.deleteByUserId(userId);
            logger.info("Deleted preferences for user ID: {}", userId);
        } catch (Exception e) {
            logger.error("Error deleting preferences for user ID: {}", userId, e);
        }
    }
}