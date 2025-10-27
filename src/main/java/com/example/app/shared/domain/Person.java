package com.example.app.shared.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;
    private String phoneNumber;
    private String username;
    private String password;
    private boolean isAdmin;
    private LocalDate birthday;
    private LocalDate startDate;
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
        if (name == null || name.trim().isEmpty()) {
            this.firstName = null;
            this.lastName = null;
            return;
        }

        String trimmedName = name.trim();
        String[] parts = trimmedName.split("\\s+", 2);
        this.firstName = parts[0];
        this.lastName = parts.length > 1 ? parts[1] : null;
    }

    public String getFirstName() { return firstName; }

    public void setFirstName(String firstName) {
        this.firstName = normalizeNamePart(firstName);
        updateFullNameFromParts();
    }

    public String getLastName() { return lastName; }

    public void setLastName(String lastName) {
        this.lastName = normalizeNamePart(lastName);
        updateFullNameFromParts();
    }

    private String normalizeNamePart(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void updateFullNameFromParts() {
        StringBuilder builder = new StringBuilder();

        if (firstName != null && !firstName.isEmpty()) {
            builder.append(firstName);
        }

        if (lastName != null && !lastName.isEmpty()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(lastName);
        }

        this.name = builder.length() > 0 ? builder.toString() : null;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
}
