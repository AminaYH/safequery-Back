package com.safequery.back.projectdemo.service;
import com.safequery.back.projectdemo.Model.UserDAO;
import com.safequery.back.projectdemo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    public boolean validateUser(String username, String rawPassword) {
        UserDAO optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            UserDAO user = optionalUser.get();
            return passwordEncoder.matches(rawPassword, user.getPassword());
        }
        return false;
    }
    }