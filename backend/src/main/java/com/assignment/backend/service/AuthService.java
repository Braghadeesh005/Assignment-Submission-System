package com.assignment.backend.service;

import com.assignment.backend.dto.AuthRequest;
import com.assignment.backend.dto.AuthResponse;
import com.assignment.backend.dto.RegisterRequest;
import com.assignment.backend.exception.ConflictException;
import com.assignment.backend.model.UserAccount;
import com.assignment.backend.repository.UserRepository;
import com.assignment.backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        UserAccount savedUser = userRepository.save(user);
        String token = jwtService.generateToken(User.withUsername(savedUser.getUsername())
                .password(savedUser.getPassword())
                .authorities("ROLE_" + savedUser.getRole().name())
                .build());
        log.info("event=register_success username={} role={}", savedUser.getUsername(), savedUser.getRole());
        return new AuthResponse(token, "Bearer", savedUser.getUsername(), savedUser.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserAccount user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        String token = jwtService.generateToken(User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build());
        log.info("event=login_success username={} role={}", user.getUsername(), user.getRole());
        return new AuthResponse(token, "Bearer", user.getUsername(), user.getRole());
    }
}
