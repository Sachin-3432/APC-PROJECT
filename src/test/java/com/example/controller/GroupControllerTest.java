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
class GroupControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createGroup() {
        var regReq = new HttpEntity<>(
                Map.of("email", "group1@example.com", "password", "Password1", "fullName", "Group User"),
                jsonHeaders());
        restTemplate.postForEntity("/api/auth/register", regReq, Object.class);
        var loginReq = new HttpEntity<>(Map.of("email", "group1@example.com", "password", "Password1"), jsonHeaders());
        var loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, AuthResponse.class);
        assertThat(loginResp.getBody()).isNotNull();
        String token = java.util.Objects.requireNonNull(loginResp.getBody()).getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var groupReq = new HttpEntity<>(Map.of("name", "Test Group"), headers);
        var createResp = restTemplate.postForEntity("/api/groups", groupReq, String.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
