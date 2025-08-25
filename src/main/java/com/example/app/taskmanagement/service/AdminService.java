package com.example.app.taskmanagement.service;

import com.example.app.taskmanagement.domain.Admin;
import com.example.app.taskmanagement.domain.AdminRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    // Registration: hash password & save
    public Admin register(Admin admin) {
        String hashed = BCrypt.hashpw(admin.getPassword(), BCrypt.gensalt());
        admin.setPassword(hashed);
        return adminRepository.save(admin);
    }

    // Authentication: check username + password
    public Optional<Admin> authenticate(String username, String plainPassword) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (BCrypt.checkpw(plainPassword, admin.getPassword())) {
                return Optional.of(admin);
            }
        }
        return Optional.empty();
    }

    public boolean usernameExists(String username) {
        return adminRepository.findByUsername(username).isPresent();
    }
}
