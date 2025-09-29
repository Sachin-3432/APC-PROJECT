
package com.example.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;

import com.example.dto.BudgetRequest;
import com.example.dto.BudgetResponse;

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
class BudgetControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createAndGetBudget() {
        // Register and login user
        var regReq = new HttpEntity<>(
                Map.of("email", "budg1@example.com", "password", "Password1", "fullName", "Budget User"),
                jsonHeaders());
        restTemplate.postForEntity("/api/auth/register", regReq, Object.class);
        var loginReq = new HttpEntity<>(Map.of("email", "budg1@example.com", "password", "Password1"), jsonHeaders());
        var loginResp = restTemplate.postForEntity("/api/auth/login", loginReq, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> loginBody = Optional.ofNullable((Map<String, Object>) loginResp.getBody())
                .orElseThrow(() -> new AssertionError("Login response body is null"));
        String token = loginBody.get("token").toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // Create budget
        BudgetRequest req = new BudgetRequest();
        req.setAmount(5000.0);
        var budReq = new HttpEntity<>(req, headers);
        var createResp = restTemplate.postForEntity("/api/budget", budReq, BudgetResponse.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResp.getBody()).isNotNull();
        assertThat(createResp.getBody()).isNotNull();
        BudgetResponse createBody = createResp.getBody();
        if (createBody == null)
            throw new AssertionError("createResp body is null");
        assertThat(createBody.getAmount()).isEqualTo(5000.0);

        // Get budget
        var getResp = restTemplate.exchange("/api/budget", HttpMethod.GET, new HttpEntity<>(headers),
                BudgetResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody()).isNotNull();
        assertThat(getResp.getBody()).isNotNull();
        BudgetResponse getBody = getResp.getBody();
        if (getBody == null)
            throw new AssertionError("getResp body is null");
        assertThat(getBody.getAmount()).isEqualTo(5000.0);
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
