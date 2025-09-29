package com.example.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UnauthorizedAccessTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void cannotAccessProtectedEndpointWithoutAuth() {
        var resp = restTemplate.exchange("/api/user/me", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()),
                String.class);
        assertThat(resp.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.UNAUTHORIZED);
    }
}
