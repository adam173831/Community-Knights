package com.example.app.taskmanagement.service;

import com.example.app.shared.domain.Person;
import com.example.app.shared.domain.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    private final PersonRepository personRepository;
    private final MailService mailService;
    
    @Value("${app.password-reset.token-validity-minutes:30}")
    private int tokenValidityMinutes;
    
    @Value("${app.password-reset.max-attempts-per-day:3}")
    private int maxAttemptsPerDay;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public LoginService(PersonRepository personRepository, MailService mailService) {
        this.personRepository = personRepository;
        this.mailService = mailService;
    }

    /**
     * Authenticate user with username and password
     */
    public Person authenticate(String username, String password) {
        try {
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                logger.warn("Authentication attempt with empty credentials");
                return null;
            }

            List<Person> personList = personRepository.findByUsername(username.trim());
            if (!personList.isEmpty()) {
                Person person = personList.get(0);
                
                // Check if account is active (you can add more checks here)
                if (!isAccountActive(person)) {
                    logger.warn("Authentication attempt for inactive account: {}", username);
                    return null;
                }
                
                if (BCrypt.checkpw(password, person.getPassword())) {
                    logger.info("Successful authentication for user: {}", username);
                    // Clear any failed login attempts here if you implement them
                    return person;
                } else {
                    logger.warn("Failed authentication for user: {} - incorrect password", username);
                    // You could implement failed login attempt tracking here
                }
            } else {
                logger.warn("Authentication attempt for non-existent user: {}", username);
            }
            
        } catch (Exception e) {
            logger.error("Error during authentication for user: {}", username, e);
        }
        
        return null;
    }

    /**
     * Find user by email address
     */
    public Optional<Person> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }
        return personRepository.findByEmail(email.trim().toLowerCase());
    }

    /**
     * Find user by reset token
     */
    public Optional<Person> findByResetToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Optional.empty();
        }
        return personRepository.findByResetToken(token.trim());
    }

    /**
     * Save or update person
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

    /**
     * Initiate password reset process
     */
    public boolean initiatePasswordReset(String email) {
        try {
            Optional<Person> personOpt = findByEmail(email);
            
            if (personOpt.isEmpty()) {
                logger.warn("Password reset requested for non-existent email: {}", email);
                // For security, we return true even if email doesn't exist
                // This prevents email enumeration attacks
                return true;
            }

            Person person = personOpt.get();
            
            // Check if user can request password reset (rate limiting)
            if (!canRequestPasswordReset(person)) {
                logger.warn("Too many password reset attempts for user: {}", person.getUsername());
                return false;
            }

            // Generate secure token
            String token = generateSecureToken();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(tokenValidityMinutes);

            // Save token to user
            person.setResetToken(token);
            person.setResetTokenExpiry(expiry);
            save(person);

            // Send email (this should be async in production)
            try {
                mailService.sendPasswordResetEmail(person.getEmail(), person.getUsername(), 
                    buildPasswordResetUrl(token));
                logger.info("Password reset email sent to: {}", email);
            } catch (Exception emailException) {
                logger.error("Failed to send password reset email to: {}", email, emailException);
                // Clean up the token if email failed
                person.setResetToken(null);
                person.setResetTokenExpiry(null);
                save(person);
                return false;
            }

            return true;
            
        } catch (Exception e) {
            logger.error("Error initiating password reset for email: {}", email, e);
            return false;
        }
    }

    /**
     * Reset password using token
     */
    public boolean resetPassword(String token, String newPassword) {
        try {
            if (token == null || token.trim().isEmpty() || 
                newPassword == null || newPassword.trim().isEmpty()) {
                return false;
            }

            Optional<Person> personOpt = findByResetToken(token.trim());
            
            if (personOpt.isEmpty()) {
                logger.warn("Password reset attempt with invalid token");
                return false;
            }

            Person person = personOpt.get();
            
            // Check if token has expired
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
            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            person.setPassword(hashedPassword);
            
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
     * Validate password strength
     */
    public boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // Check for at least one uppercase, one lowercase, and one digit
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUpper && hasLower && hasDigit;
    }

    /**
     * Clean up expired reset tokens (should be called periodically)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            List<Person> usersWithTokens = personRepository.findAll().stream()
                .filter(person -> person.getResetToken() != null && 
                         person.getResetTokenExpiry() != null &&
                         person.getResetTokenExpiry().isBefore(LocalDateTime.now()))
                .toList();

            for (Person person : usersWithTokens) {
                person.setResetToken(null);
                person.setResetTokenExpiry(null);
                save(person);
            }

            if (!usersWithTokens.isEmpty()) {
                logger.info("Cleaned up {} expired reset tokens", usersWithTokens.size());
            }
            
        } catch (Exception e) {
            logger.error("Error cleaning up expired tokens", e);
        }
    }

    private boolean isAccountActive(Person person) {
        // Add your account status checks here
        // For example: return person.isActive() && !person.isLocked();
        return true; // For now, assume all accounts are active
    }

    private boolean canRequestPasswordReset(Person person) {
        // Implement rate limiting logic here
        // For example, check if user has made too many reset requests in the last 24 hours
        // This is a simplified implementation
        return true;
    }

    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildPasswordResetUrl(String token) {
        return baseUrl + "/reset-password/" + token;
    }
}