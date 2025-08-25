package com.example.app.shared.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    
    Optional<UserPreferences> findByUserId(Long userId);
    
    Optional<UserPreferences> findByUser(Person user);
    
    void deleteByUserId(Long userId);
}