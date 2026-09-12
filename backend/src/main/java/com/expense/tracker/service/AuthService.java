package com.expense.tracker.service;

import com.expense.tracker.dto.auth.AuthResponse;
import com.expense.tracker.dto.auth.LoginRequest;
import com.expense.tracker.dto.auth.RegisterRequest;
import com.expense.tracker.dto.auth.UserDTO;
import com.expense.tracker.entity.User;
import com.expense.tracker.exception.DuplicateEmailException;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CategoryService categoryService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("An account with email " + email + " already exists.");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));

        user = userRepository.save(user);
        categoryService.seedDefaultCategories(user); // usable immediately after signup

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponse.of(token, new UserDTO(user.getId(), user.getName(), user.getEmail()));
    }
}