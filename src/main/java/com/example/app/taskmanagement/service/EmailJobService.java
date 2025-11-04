package com.example.app.taskmanagement.service;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class EmailJobService {

    private final MailService mailService;
    private final TaskScheduler scheduler;

    // Track running jobs by id
    private final Map<String, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();

    public EmailJobService(MailService mailService, TaskScheduler scheduler) {
        this.mailService = mailService;
        this.scheduler = scheduler;
    }

    /** Start a repeating job that emails "Hello" every 10 seconds. Returns a job id. */
    public String startEvery10Seconds(String to) {
        String id = UUID.randomUUID().toString();
        ScheduledFuture<?> f = scheduler.scheduleAtFixedRate(
                () -> mailService.sendSimpleEmail(to, "Test", "Hello"),
                Duration.ofSeconds(10)
        );
        jobs.put(id, f);
        return id;
    }

    /** Cancel a running job by id. */
    public boolean stop(String jobId) {
        ScheduledFuture<?> f = jobs.remove(jobId);
        return f != null && f.cancel(true);
    }

    /** Optional helper if you want to check from the UI. */
    public boolean isRunning(String jobId) {
        ScheduledFuture<?> f = jobs.get(jobId);
        return f != null && !f.isCancelled();
    }
}
