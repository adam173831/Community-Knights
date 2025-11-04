package com.example.app.shared.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByUsername(String username);
    Optional<Person> findByEmail(String email);
    Optional<Person> findByResetToken(String token);

    @Modifying
    @Query("update Person p set p.resetToken = null, p.resetTokenExpiry = null " +
           "where p.resetToken is not null and p.resetTokenExpiry is not null and p.resetTokenExpiry < :now")
    int clearExpiredResetTokens(@Param("now") LocalDateTime now);
}
