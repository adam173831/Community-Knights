package com.example.app.taskmanagement.service;

import com.example.app.shared.domain.Person;
import com.example.app.shared.domain.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SignupService {

    private final PersonRepository personRepository;

    public SignupService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public boolean usernameExists(String username) {
        return !personRepository.findByUsername(username).isEmpty();
    }

    public void save(Person person) {
        personRepository.save(person);
    }
}
