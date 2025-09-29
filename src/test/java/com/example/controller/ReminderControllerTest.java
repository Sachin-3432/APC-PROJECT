
package com.example.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;

import com.example.dto.AuthResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReminderControllerTest {
        @Autowired
        private TestRestTemplate restTemplate;

        @Test
        void createAndGetReminder() {
                // Register and login user
                var regReq = new HttpEntity<>(
                                Map.of("email", "rem1@example.com", "password", "Password1", "fullName", "Rem User"),
                                jsonHeaders());
                restTemplate.postForEntity("/api/auth/register", regReq, Object.class);
                var loginReq = new HttpEntity<>(Map.of("email", "rem1@example.com", "password", "Password1"),
                                jsonHeaders());
                var loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, AuthResponse.class);
                AuthResponse loginBody = Optional.ofNullable(loginResp.getBody())
                                .orElseThrow(() -> new AssertionError("Login response body is null"));
                String token = loginBody.getToken();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);

                // Get user id for debtorId (self-reminder for test)
                var meResp = restTemplate.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers),
                                Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> meBody = Optional.ofNullable((Map<String, Object>) meResp.getBody())
                                .orElseThrow(() -> new AssertionError("/api/auth/me response body is null"));
                String userId = (String) meBody.get("id");

                // Create reminder (user is both sender and receiver)
                var reminderBody = Map.of(
                                "debtorId", userId,
                                "message", "Test Reminder",
                                "method", "email");
                var remReq = new HttpEntity<>(reminderBody, headers);
                var createResp = restTemplate.postForEntity("/api/reminders", remReq, String.class);
                assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);

                // Get reminders (should contain the created reminder)
                var getResp = restTemplate.exchange("/api/reminders", HttpMethod.GET, new HttpEntity<>(headers),
                                String.class);
                assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(getResp.getBody()).contains("Test Reminder");
        }

        private HttpHeaders jsonHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
        }
}
