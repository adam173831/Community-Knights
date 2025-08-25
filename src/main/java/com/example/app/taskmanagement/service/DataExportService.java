package com.example.app.taskmanagement.service;

import com.example.app.shared.domain.Person;
import com.example.app.shared.domain.UserPreferences;
import com.example.app.taskmanagement.domain.Task;
import com.example.app.taskmanagement.domain.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataExportService {

    private static final Logger logger = LoggerFactory.getLogger(DataExportService.class);

    private final TaskRepository taskRepository;
    private final SettingsService settingsService;
    private final ObjectMapper objectMapper;

    public DataExportService(TaskRepository taskRepository, SettingsService settingsService) {
        this.taskRepository = taskRepository;
        this.settingsService = settingsService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public byte[] exportUserData(Person user, String format, boolean includePersonal, boolean includeTasks) {
        try {
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("exportDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            exportData.put("exportedBy", user.getUsername());

            if (includePersonal) {
                exportData.put("personalData", buildPersonalData(user));
            }

            if (includeTasks) {
                exportData.put("taskData", buildTaskData(user));
            }

            return switch (format.toLowerCase()) {
                case "json" -> exportAsJson(exportData);
                case "csv" -> exportAsCsv(exportData, includePersonal, includeTasks);
                case "excel (xlsx)" -> exportAsExcel(exportData);
                case "pdf report" -> exportAsPdf(exportData);
                default -> exportAsJson(exportData);
            };

        } catch (Exception e) {
            logger.error("Error exporting data for user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to export data: " + e.getMessage());
        }
    }

    private Map<String, Object> buildPersonalData(Person user) {
        Map<String, Object> personalData = new HashMap<>();
        personalData.put("id", user.getId());
        personalData.put("name", user.getName());
        personalData.put("username", user.getUsername());
        personalData.put("email", user.getEmail());
        personalData.put("phoneNumber", user.getPhoneNumber());
        personalData.put("birthday", user.getBirthday());
        personalData.put("startDate", user.getStartDate());

        // Include preferences if available
        UserPreferences preferences = settingsService.getUserPreferences(user);
        if (preferences != null) {
            Map<String, Object> prefsData = new HashMap<>();
            prefsData.put("bio", preferences.getBio());
            prefsData.put("theme", preferences.getTheme());
            prefsData.put("colorScheme", preferences.getColorScheme());
            prefsData.put("notificationSettings", Map.of(
                "pushNotifications", preferences.isEnablePushNotifications(),
                "desktopAlerts", preferences.isEnableDesktopAlerts(),
                "taskUpdates", preferences.isNotifyTaskUpdates(),
                "userMentions", preferences.isNotifyUserMentions(),
                "systemAlerts", preferences.isNotifySystemAlerts()
            ));
            personalData.put("preferences", prefsData);
        }

        return personalData;
    }

    private Map<String, Object> buildTaskData(Person user) {
        Map<String, Object> taskData = new HashMap<>();
        
        // Get all tasks - in a real app, you'd filter by user
        List<Task> tasks = taskRepository.findAll();
        taskData.put("totalTasks", tasks.size());
        taskData.put("tasks", tasks);

        return taskData;
    }

    private byte[] exportAsJson(Map<String, Object> data) throws IOException {
        return objectMapper.writeValueAsBytes(data);
    }

    private byte[] exportAsCsv(Map<String, Object> data, boolean includePersonal, boolean includeTasks) {
        StringBuilder csv = new StringBuilder();
        
        // CSV Header
        csv.append("Export Date,").append(data.get("exportDate")).append("\n");
        csv.append("Exported By,").append(data.get("exportedBy")).append("\n\n");

        if (includePersonal && data.containsKey("personalData")) {
            csv.append("PERSONAL DATA\n");
            @SuppressWarnings("unchecked")
            Map<String, Object> personalData = (Map<String, Object>) data.get("personalData");
            
            personalData.forEach((key, value) -> {
                if (!"preferences".equals(key)) {
                    csv.append(key).append(",").append(value != null ? value.toString() : "").append("\n");
                }
            });
            csv.append("\n");
        }

        if (includeTasks && data.containsKey("taskData")) {
            csv.append("TASK DATA\n");
            csv.append("Description,Creation Date,Due Date\n");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> taskData = (Map<String, Object>) data.get("taskData");
            @SuppressWarnings("unchecked")
            List<Task> tasks = (List<Task>) taskData.get("tasks");
            
            for (Task task : tasks) {
                csv.append("\"").append(task.getDescription()).append("\",")
                   .append(task.getCreationDate()).append(",")
                   .append(task.getDueDate() != null ? task.getDueDate().toString() : "")
                   .append("\n");
            }
        }

        return csv.toString().getBytes();
    }

    private byte[] exportAsExcel(Map<String, Object> data) {
        // For demo purposes, return CSV-like content
        // In a real implementation, you'd use Apache POI to create actual Excel files
        String excelContent = "Excel export not fully implemented - this would be an actual XLSX file\n\n" +
                             new String(exportAsCsv(data, true, true));
        return excelContent.getBytes();
    }

    private byte[] exportAsPdf(Map<String, Object> data) {
        // For demo purposes, return text content
        // In a real implementation, you'd use libraries like iText or Flying Saucer
        StringBuilder pdf = new StringBuilder();
        pdf.append("PDF EXPORT REPORT\n");
        pdf.append("==================\n\n");
        pdf.append("Export Date: ").append(data.get("exportDate")).append("\n");
        pdf.append("Exported By: ").append(data.get("exportedBy")).append("\n\n");
        
        if (data.containsKey("personalData")) {
            pdf.append("PERSONAL INFORMATION\n");
            pdf.append("--------------------\n");
            @SuppressWarnings("unchecked")
            Map<String, Object> personalData = (Map<String, Object>) data.get("personalData");
            personalData.forEach((key, value) -> {
                if (!"preferences".equals(key)) {
                    pdf.append(key).append(": ").append(value != null ? value.toString() : "N/A").append("\n");
                }
            });
            pdf.append("\n");
        }

        if (data.containsKey("taskData")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> taskData = (Map<String, Object>) data.get("taskData");
            pdf.append("TASK SUMMARY\n");
            pdf.append("------------\n");
            pdf.append("Total Tasks: ").append(taskData.get("totalTasks")).append("\n\n");
            
            pdf.append("Note: This is a demo PDF export. In a real implementation, ");
            pdf.append("this would be a properly formatted PDF document with tables and styling.\n");
        }

        return pdf.toString().getBytes();
    }

    public String getFileExtension(String format) {
        return switch (format.toLowerCase()) {
            case "csv" -> ".csv";
            case "excel (xlsx)" -> ".xlsx";
            case "pdf report" -> ".pdf";
            default -> ".json";
        };
    }

    public String getContentType(String format) {
        return switch (format.toLowerCase()) {
            case "csv" -> "text/csv";
            case "excel (xlsx)" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pdf report" -> "application/pdf";
            default -> "application/json";
        };
    }
}