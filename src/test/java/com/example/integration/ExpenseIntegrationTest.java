package com.example.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;

import com.example.dto.AuthResponse;
import com.example.dto.ExpenseRequest;
import com.example.dto.ExpenseResponse;
import com.example.repository.ExpenseRepository;
import com.example.repository.UserRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
public class ExpenseIntegrationTest {

        @Autowired
        private TestRestTemplate restTemplate;

        @Autowired
        private ExpenseRepository expenseRepository;

        @Autowired
        private UserRepository userRepository;

        @BeforeAll
        public void beforeAll() {
                expenseRepository.deleteAll();
                // Only delete users with test emails
                userRepository.findAll().stream()
                                .filter(u -> u.getEmail() != null && u.getEmail().endsWith("@example.com"))
                                .forEach(u -> userRepository.deleteById(u.getId()));
        }

        @Test
        public void expenseCrud_happyPath() {
                Map<String, String> reg = Map.of("email", "expuser@example.com", "password", "Password1", "fullName",
                                "Expense User");
                HttpHeaders headers = jsonHeaders();
                restTemplate.postForEntity("/api/auth/register", new HttpEntity<>(reg, headers), Object.class);

                ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity("/api/auth/login",
                                new HttpEntity<>(Map.of("email", "expuser@example.com", "password", "Password1"),
                                                headers),
                                AuthResponse.class);
                AuthResponse loginBody = loginResp.getBody();
                assertThat(loginBody).isNotNull();
                String token = loginBody != null ? loginBody.getToken() : null;
                assertThat(token).isNotEmpty();

                HttpHeaders auth = new HttpHeaders();
                if (token != null)
                        auth.setBearerAuth(token);

                ExpenseRequest createReq = new ExpenseRequest();
                createReq.setDescription("Coffee");
                createReq.setAmount(3.5);
                createReq.setCategory("Beverage");
                createReq.setDate(LocalDate.now());

                ResponseEntity<ExpenseResponse> createResp = restTemplate.postForEntity("/api/expenses",
                                new HttpEntity<>(createReq, auth), ExpenseResponse.class);
                assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
                ExpenseResponse created = createResp.getBody();
                assertThat(created).isNotNull();
                String id = created != null ? created.getId() : null;
                assertThat(id).isNotNull();

                ResponseEntity<ExpenseResponse> getResp = restTemplate.exchange("/api/expenses/" + id, HttpMethod.GET,
                                new HttpEntity<>(auth), ExpenseResponse.class);
                assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);

                ExpenseRequest updateReq = new ExpenseRequest();
                updateReq.setDescription("Coffee Large");
                updateReq.setAmount(5.0);
                updateReq.setCategory("Beverage");
                updateReq.setDate(LocalDate.now());

                ResponseEntity<ExpenseResponse> updateResp = restTemplate.exchange("/api/expenses/" + id,
                                HttpMethod.PUT,
                                new HttpEntity<>(updateReq, auth), ExpenseResponse.class);
                assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
                ExpenseResponse updated = updateResp.getBody();
                assertThat(updated).isNotNull();
                assertThat(updated != null ? updated.getDescription() : null).isEqualTo("Coffee Large");

                ResponseEntity<Void> delResp = restTemplate.exchange("/api/expenses/" + id, HttpMethod.DELETE,
                                new HttpEntity<>(auth), Void.class);
                assertThat(delResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        private HttpHeaders jsonHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
        }
}
