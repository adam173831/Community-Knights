package com.example.app.taskmanagement.service;

import com.example.app.shared.domain.Person;
import com.example.app.shared.domain.PersonRepository;
import com.vaadin.flow.server.VaadinSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
@Transactional
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final PersonRepository personRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.reset-token.valid-minutes:30}")
    private int tokenValidityMinutes;

    public LoginService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    /**
     * Authenticate by username and password.
     */
    public Optional<Person> authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        Optional<Person> userOpt = personRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        Person user = userOpt.get();
        if (passwordMatches(user, password)) {
            if (VaadinSession.getCurrent() != null) {
                VaadinSession.getCurrent().setAttribute(Person.class, user);
            }
            return Optional.of(user);
        }

        return Optional.empty();
    }


    /**
     * Find user by email.
     */
    public Optional<Person> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return personRepository.findByEmail(email.trim().toLowerCase());
    }

    /**
     * Find user by reset token.
     */
    public Optional<Person> findByResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return personRepository.findByResetToken(token.trim());
    }

    /**
     * Save or update person.
     */
    public Person save(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person cannot be null");
        }

        try {
            return personRepository.save(person);
        } catch (Exception e) {
            logger.error("Error saving person: {}", person.getUsername(), e);
            throw new RuntimeException("Failed to save user data", e);
        }
    }

    public Optional<Person> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return personRepository.findById(id);
    }

    public boolean passwordMatches(Person person, String rawPassword) {
        if (person == null || rawPassword == null) {
            return false;
        }

        String storedPassword = person.getPassword();
        if (storedPassword == null || storedPassword.isEmpty()) {
            return false;
        }

        try {
            return BCrypt.checkpw(rawPassword, storedPassword);
        } catch (IllegalArgumentException invalidHash) {
            if (storedPassword.startsWith("{bcrypt}")) {
                String strippedHash = storedPassword.substring("{bcrypt}".length());
                try {
                    return BCrypt.checkpw(rawPassword, strippedHash);
                } catch (IllegalArgumentException ignored) {
                    // fall through to plain-text comparison below
                }
            }

            return storedPassword.equals(rawPassword);
        }
    }

    public String encodePassword(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * Initiate password reset process.
     */
    public boolean initiatePasswordReset(String email) {
        try {
            Optional<Person> personOpt = findByEmail(email);

            if (personOpt.isEmpty()) {
                logger.warn("Password reset requested for non-existent email: {}", email);
                // Return true to prevent email enumeration
                return true;
            }

            Person person = personOpt.get();

            // Check rate limiting / cooldown (not shown in your diff, but presumably exists)
            if (!canRequestPasswordReset(person)) {
                logger.warn("Too many password reset attempts for user: {}", person.getUsername());
                return false;
            }

            // Generate secure token
            String token = generateSecureToken();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(tokenValidityMinutes);

            person.setResetToken(token);
            person.setResetTokenExpiry(expiry);
            save(person);

            // In real app, send token via email here

            logger.info("Password reset token generated for user: {}", person.getUsername());
            return true;
        } catch (Exception e) {
            logger.error("Error initiating password reset for email: {}", email, e);
            return false;
        }
    }

    /**
     * Complete password reset using token.
     */
    public boolean resetPassword(String token, String newPassword) {
        try {
            Optional<Person> personOpt = findByResetToken(token);
            if (personOpt.isEmpty()) {
                logger.warn("Password reset attempt with invalid token");
                return false;
            }

            Person person = personOpt.get();

            // Check token expiry
            if (person.getResetTokenExpiry() == null ||
                person.getResetTokenExpiry().isBefore(LocalDateTime.now())) {

                logger.warn("Password reset attempt with expired token for user: {}", person.getUsername());

                // Clean up expired token
                person.setResetToken(null);
                person.setResetTokenExpiry(null);
                save(person);

                return false;
            }

            // Validate password strength
            if (!isPasswordValid(newPassword)) {
                logger.warn("Password reset attempt with weak password for user: {}", person.getUsername());
                return false;
            }

            // Hash and save new password
            person.setPassword(encodePassword(newPassword));

            // Clear reset token
            person.setResetToken(null);
            person.setResetTokenExpiry(null);

            save(person);

            logger.info("Password successfully reset for user: {}", person.getUsername());
            return true;

        } catch (Exception e) {
            logger.error("Error resetting password with token: {}", token, e);
            return false;
        }
    }

    /**
     * Validate password strength.
     */
    public boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // At least one uppercase, one lowercase, and one digit
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        return hasUpper && hasLower && hasDigit;
    }

    /* ---------- helper methods for reset tokens ---------- */

    private boolean canRequestPasswordReset(Person person) {
        // Placeholder rate limiting policy:
        // allow if there's no token or token is expired
        if (person.getResetToken() == null || person.getResetTokenExpiry() == null) {
            return true;
        }
        return person.getResetTokenExpiry().isBefore(LocalDateTime.now());
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    @Transactional
    public int cleanupExpiredTokens() {
        try {
            int cleared = personRepository.clearExpiredResetTokens(LocalDateTime.now());
            logger.info("Expired password reset tokens cleared: {}", cleared);
            return cleared;
        } catch (Exception e) {
            logger.error("Failed to clear expired password reset tokens", e);
            return 0;
        }
    }
}
