package com.expense.tracker.service;

import com.expense.tracker.dto.auth.AuthResponse;
import com.expense.tracker.dto.auth.LoginRequest;
import com.expense.tracker.dto.auth.RegisterRequest;
import com.expense.tracker.entity.User;
import com.expense.tracker.exception.DuplicateEmailException;
import com.expense.tracker.repository.UserRepository;
import com.expense.tracker.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_withNewEmail_encodesPasswordAndSeedsCategories() {
        RegisterRequest request = new RegisterRequest("Alice", "Alice@Example.com", "secret123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$10$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(7L);
            return u;
        });
        when(jwtService.generateToken(7L, "alice@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("alice@example.com"); // lowercased
        assertThat(response.user().name()).isEqualTo("Alice");
        verify(passwordEncoder).encode("secret123");                 // password is hashed
        verify(categoryService).seedDefaultCategories(any(User.class)); // defaults created
    }

    @Test
    void register_withDuplicateEmail_throwsDuplicateEmailException() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "secret123");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        LoginRequest request = new LoginRequest("alice@example.com", "secret123");
        User user = com.expense.tracker.TestData.user(7L, "alice@example.com");
        user.setName("Alice");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(mock(Authentication.class));
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(7L, "alice@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().name()).isEqualTo("Alice");
    }

    @Test
    void login_withWrongPassword_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("alice@example.com", "wrong");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad creds"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        verify(userRepository, never()).findByEmail(any());
    }
}