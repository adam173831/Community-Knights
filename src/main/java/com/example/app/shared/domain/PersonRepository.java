package com.example.app.shared.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByUsername(String username);
    Optional<Person> findByEmail(String email);
    Optional<Person> findByResetToken(String token);

}