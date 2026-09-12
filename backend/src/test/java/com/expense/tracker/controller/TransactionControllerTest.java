package com.expense.tracker.controller;

import com.expense.tracker.dto.TransactionRequest;
import com.expense.tracker.dto.TransactionResponse;
import com.expense.tracker.enums.TransactionType;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.security.CustomUserDetailsService;
import com.expense.tracker.security.JwtService;
import com.expense.tracker.security.UserPrincipal;
import com.expense.tracker.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-layer tests for the transaction endpoints: authentication rule,
 * Bean Validation, error responses and the centralized exception handler.
 * The production JWT/SecurityConfig wiring is covered by the end-to-end
 * verification and the service unit tests.
 */
@WebMvcTest(TransactionController.class)
@Import(TransactionControllerTest.TestSecurity.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    // The JwtAuthenticationFilter (a Filter) is component-scanned by @WebMvcTest;
    // its collaborators are not, so they are mocked here.
    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void create_withToken_returns201() throws Exception {
        TransactionRequest request = new TransactionRequest(
                TransactionType.EXPENSE,
                new BigDecimal("99.99"),
                1L,
                LocalDate.of(2026, 9, 10),
                "Groceries");

        when(transactionService.create(eq(1L), any(TransactionRequest.class)))
                .thenReturn(new TransactionResponse(
                        5L, TransactionType.EXPENSE, new BigDecimal("99.99"),
                        "Groceries", LocalDate.of(2026, 9, 10), null,
                        new com.expense.tracker.dto.CategoryResponse(1L, "Food", TransactionType.EXPENSE, "#EF4444")));

        mockMvc.perform(post("/api/transactions")
                        .with(authenticatedAs(1L, "user@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.category.name").value("Food"));
    }

    @Test
    void create_withNegativeAmount_returns400WithFieldError() throws Exception {
        String body = """
                {
                  "type": "EXPENSE",
                  "amount": -5,
                  "categoryId": 1,
                  "transactionDate": "2026-09-10",
                  "description": "x"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .with(authenticatedAs(1L, "user@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message.amount").exists());
    }

    @Test
    void create_withInvalidTypeValue_returns400() throws Exception {
        String body = """
                {
                  "type": "CASH",
                  "amount": 10,
                  "categoryId": 1,
                  "transactionDate": "2026-09-10",
                  "description": "x"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .with(authenticatedAs(1L, "user@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withoutAuthentication_returns401() throws Exception {
        String body = """
                {
                  "type": "EXPENSE",
                  "amount": 10,
                  "categoryId": 1,
                  "transactionDate": "2026-09-10",
                  "description": "x"
                }
                """;

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_foreignTransaction_returns404JsonError() throws Exception {
        when(transactionService.findById(eq(1L), eq(99L)))
                .thenThrow(new ResourceNotFoundException("Transaction not found with id: 99"));

        mockMvc.perform(get("/api/transactions/99").with(authenticatedAs(1L, "user@example.com")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    /** Authenticates a UserPrincipal exactly like a valid JWT would. */
    private RequestPostProcessor authenticatedAs(long userId, String email) {
        com.expense.tracker.entity.User user = new com.expense.tracker.entity.User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("$2a$10$test");

        UserPrincipal principal = new UserPrincipal(user);
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    /** Minimal deterministic security chain (CSRF disabled, requires authentication). */
    @TestConfiguration
    static class TestSecurity {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .httpBasic(Customizer.withDefaults())
                    .build();
        }
    }
}