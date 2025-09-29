package com.example.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import com.example.dto.AuthResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerAndLogin() {
        var regReq = new HttpEntity<>(
                Map.of("email", "auth1@example.com", "password", "Password1", "fullName", "Auth User"), jsonHeaders());
        var regResp = restTemplate.postForEntity("/api/auth/register", regReq, Object.class);
        if (!regResp.getStatusCode().equals(HttpStatus.OK)) {
            System.out.println("Register error: " + regResp.getBody());
        }
        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        var loginReq = new HttpEntity<>(Map.of("email", "auth1@example.com", "password", "Password1"), jsonHeaders());
        var loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, AuthResponse.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
