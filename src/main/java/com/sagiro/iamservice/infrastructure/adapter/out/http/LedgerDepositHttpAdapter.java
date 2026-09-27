package com.sagiro.iamservice.infrastructure.adapter.out.http;

import com.sagiro.iamservice.application.port.output.LedgerDepositPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP adapter that calls the Ledger Service deposit endpoint.
 * Used by the welcome bonus flow to credit funds into the user's ledger account.
 */
public class LedgerDepositHttpAdapter implements LedgerDepositPort {

    private static final Logger log = LoggerFactory.getLogger(LedgerDepositHttpAdapter.class);

    private final RestTemplate restTemplate;
    private final String ledgerServiceBaseUrl;

    public LedgerDepositHttpAdapter(RestTemplate restTemplate, String ledgerServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.ledgerServiceBaseUrl = ledgerServiceBaseUrl;
    }

    @Override
    public void depositFunds(UUID userId, BigDecimal amount, String currency, String idempotencyKey, String description) {
        String url = ledgerServiceBaseUrl + "/api/v1/accounts/deposit";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", userId.toString());
        headers.set("X-Idempotency-Key", idempotencyKey);

        Map<String, Object> body = Map.of(
                "amount", amount,
                "currency", currency,
                "description", description
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("Depositing {} {} for user {} via Ledger Service at {}", amount, currency, userId, url);
        restTemplate.postForEntity(url, request, String.class);
        log.info("Deposit successful for user {}", userId);
    }

    @Override
    public void claimInitialBonus(UUID userId, String idempotencyKey) {
        String url = ledgerServiceBaseUrl + "/api/v1/accounts/user/" + userId + "/claim-bonus";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Idempotency-Key", idempotencyKey);

        HttpEntity<Void> request = new HttpEntity<>(null, headers);

        log.info("Claiming welcome bonus for user {} via Ledger Service at {}", userId, url);
        restTemplate.postForEntity(url, request, String.class);
        log.info("Bonus claimed successfully in ledger for user {}", userId);
    }
}
