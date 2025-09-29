package com.example.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.example.dto.AuthResponse;
import com.example.dto.UserResponse;
import com.example.repository.UserRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    public void beforeAll() {
        // Only delete users with test emails
        userRepository.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().endsWith("@example.com"))
                .forEach(u -> userRepository.deleteById(u.getId()));
    }

    @Test
    public void registerLoginAndMe_happyPath() {
        Map<String, String> reg = Map.of("email", "inttest@example.com", "password", "Password1", "fullName",
                "Int Test");
        HttpHeaders headers = jsonHeaders();
        HttpEntity<Map<String, String>> regReq = new HttpEntity<>(reg, headers);
        ResponseEntity<Object> regResp = restTemplate.postForEntity("/api/auth/register", regReq, Object.class);
        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpEntity<Map<String, String>> loginReq = new HttpEntity<>(
                Map.of("email", "inttest@example.com", "password", "Password1"), headers);
        ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity("/api/auth/login", loginReq,
                AuthResponse.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse body = loginResp.getBody();
        assertThat(body).isNotNull();
        String token = body != null ? body.getToken() : null;
        assertThat(token).isNotEmpty();

        HttpHeaders auth = new HttpHeaders();
        if (token != null) {
            auth.setBearerAuth(token);
        }
        HttpEntity<Void> meReq = new HttpEntity<>(auth);
        ResponseEntity<UserResponse> meResp = restTemplate.exchange("/api/auth/me", HttpMethod.GET, meReq,
                UserResponse.class);
        assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse user = meResp.getBody();
        assertThat(user).isNotNull();
        assertThat(user != null ? user.getEmail() : null).isEqualTo("inttest@example.com");
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
