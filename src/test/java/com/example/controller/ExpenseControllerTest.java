package com.example.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;

import com.example.dto.AuthResponse;
import com.example.dto.ExpenseRequest;
import com.example.dto.ExpenseResponse;

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
class ExpenseControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAndDeleteExpense() {
        var regReq = new HttpEntity<>(
                Map.of("email", "exp1@example.com", "password", "Password1", "fullName", "Exp User"), jsonHeaders());
        restTemplate.postForEntity("/api/auth/register", regReq, Object.class);
        var loginReq = new HttpEntity<>(Map.of("email", "exp1@example.com", "password", "Password1"), jsonHeaders());
        var loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, AuthResponse.class);
        assertThat(loginResp.getBody()).isNotNull();
        String token = java.util.Objects.requireNonNull(loginResp.getBody()).getToken();
        assertThat(token).isNotNull();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ExpenseRequest req = new ExpenseRequest();
        req.setDescription("Test Expense");
        req.setAmount(10.0);
        req.setCategory("Test");
        req.setDate(LocalDate.now());
        var createResp = restTemplate.postForEntity("/api/expenses", new HttpEntity<>(req, headers),
                ExpenseResponse.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResp.getBody()).isNotNull();
        String id = java.util.Objects.requireNonNull(createResp.getBody()).getId();
        assertThat(id).isNotNull();
        var delResp = restTemplate.exchange("/api/expenses/" + id, HttpMethod.DELETE, new HttpEntity<>(headers),
                Void.class);
        assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
