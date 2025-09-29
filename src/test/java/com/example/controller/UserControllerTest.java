package com.example.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.example.dto.AuthResponse;
import com.example.dto.UserResponse;

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
class UserControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void getAndUpdateProfile() {
        var regReq = new HttpEntity<>(
                Map.of("email", "user1@example.com", "password", "Password1", "fullName", "User One"), jsonHeaders());
        restTemplate.postForEntity("/api/auth/register", regReq, Object.class);
        var loginReq = new HttpEntity<>(Map.of("email", "user1@example.com", "password", "Password1"), jsonHeaders());
        var loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, AuthResponse.class);
        assertThat(loginResp.getBody()).isNotNull();
        String token = java.util.Objects.requireNonNull(loginResp.getBody()).getToken();
        assertThat(token).isNotNull();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var meResp = restTemplate.exchange("/api/auth/me", HttpMethod.GET, new HttpEntity<>(headers),
                UserResponse.class);
        assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
