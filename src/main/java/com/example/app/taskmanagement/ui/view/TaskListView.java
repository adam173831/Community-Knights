package com.example.app.taskmanagement.ui.view;

import com.example.app.base.ui.component.ViewToolbar;
import com.example.app.taskmanagement.domain.Task;
import com.example.app.taskmanagement.service.TaskService;
import com.example.app.taskmanagement.service.EmailJobService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.PermitAll;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.toSpringPageRequest;

@Route("task-list")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
@PermitAll // When security is enabled, allow all authenticated users
public class TaskListView extends Main {

    private final TaskService taskService;
    private final EmailJobService emailJobService;

    // Existing fields
    final TextField description;
    final DatePicker dueDate;
    final Button createBtn;
    final Grid<Task> taskGrid;

    // Email test controls
    private String emailJobId = null;

    public TaskListView(TaskService taskService, Clock clock, EmailJobService emailJobService) {
        this.taskService = taskService;
        this.emailJobService = emailJobService;

        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");

        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withZone(clock.getZone())
                .withLocale(getLocale());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(getLocale());

        taskGrid = new Grid<>();
        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.addColumn(Task::getDescription).setHeader("Description");
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate())
                .map(dateFormatter::format)
                .orElse("Never")).setHeader("Due Date");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate()))
                .setHeader("Creation Date");
        taskGrid.setSizeFull();

        setSizeFull();
        addClassNames(
                LumoUtility.BoxSizing.BORDER,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Gap.SMALL
        );

        // Top toolbar for tasks
        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn)));

        // Email test bar (Start/Stop every 10s)
        add(buildEmailTestBar());

        // Main grid
        add(taskGrid);
    }

    private void createTask() {
        taskService.createTask(description.getValue(), dueDate.getValue());
        taskGrid.getDataProvider().refreshAll();
        description.clear();
        dueDate.clear();
        Notification.show("Task added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /** Small bar with recipient field + Start/Stop buttons for the 10s email test. */
    private HorizontalLayout buildEmailTestBar() {
        EmailField toField = new EmailField("Email test recipient");
        toField.setPlaceholder("someone@example.com");
        toField.setClearButtonVisible(true);
        toField.setWidth("320px");

        Button start = new Button("Start emails every 10s", e -> {
            if (emailJobId != null) {
                Notification.show("Already running. Stop first.");
                return;
            }
            String to = toField.getValue();
            if (to == null || to.isBlank()) {
                Notification.show("Enter a recipient email");
                return;
            }
            emailJobId = emailJobService.startEvery10Seconds(to);
            Notification.show("Started job " + emailJobId);
        });
        start.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button stop = new Button("Stop", e -> {
            if (emailJobId == null) {
                Notification.show("Nothing running");
                return;
            }
            boolean ok = emailJobService.stop(emailJobId);
            emailJobId = null;
            Notification.show(ok ? "Stopped" : "Stop failed");
        });

        HorizontalLayout bar = new HorizontalLayout(toField, start, stop);
        bar.setAlignItems(Alignment.END);

        // Auto-stop when navigating away from this view
        addDetachListener(evt -> {
            if (emailJobId != null) {
                emailJobService.stop(emailJobId);
                emailJobId = null;
            }
        });

        return bar;
    }
}
