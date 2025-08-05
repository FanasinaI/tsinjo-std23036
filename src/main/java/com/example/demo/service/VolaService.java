package com.example.demo.service;

import com.example.demo.model.Payment;
import com.example.demo.model.Payment.PaymentStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Slf4j
public class VolaService {
    private static final String VOLA_API_BASE_URL = "https://42cwka3n4ifcp7ufheyrpmph240iuaxo.lambda-url.eu-west-3.on.aws";
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final String apiKey;
    private final WebClient webClient;

    public VolaService(@Value("${vola.api.key}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("API key must not be null or empty");
        }
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(VOLA_API_BASE_URL)
                .defaultHeader(AUTH_HEADER, BEARER_PREFIX + apiKey)
                .build();
    }

    public Mono<Payment> verifyPayment(String paymentId) {
        log.info("Initiating payment verification for payment ID: {}", paymentId);

        if (paymentId == null || paymentId.isBlank()) {
            return Mono.error(new IllegalArgumentException("Payment ID must not be null or empty"));
        }

        return webClient.get()
                .uri("/payments/{id}", paymentId)
                .retrieve()
                .bodyToMono(VolaPaymentResponse.class)
                .map(this::mapToPayment)
                .doOnSuccess(payment ->
                        log.info("Successfully verified payment ID: {}, Status: {}",
                                paymentId, payment.getStatus()))
                .doOnError(error ->
                        log.error("Failed to verify payment ID: {}, Error: {}",
                                paymentId, error.getMessage()))
                .onErrorResume(error -> {
                    log.warn("Returning empty payment due to verification error");
                    return Mono.just(new Payment());
                });
    }

    private Payment mapToPayment(VolaPaymentResponse response) {
        if (response == null) {
            throw new IllegalStateException("Payment response cannot be null");
        }

        return Payment.builder()
                .id(response.getId())
                .amount(response.getAmount())
                .paymentMethod(response.getPaymentMethod())
                .status(parsePaymentStatus(response.getStatus()))
                .creationDatetime(response.getCreationDatetime())
                .build();
    }

    private PaymentStatus parsePaymentStatus(String status) {
        try {
            return PaymentStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown payment status: {}, defaulting to VERIFYING", status);
            return PaymentStatus.VERIFYING;
        }
    }
    @Data
    private static class VolaPaymentResponse {
        private String id;
        private BigDecimal amount;
        private Instant creationDatetime;
        private String paymentMethod;
        private String status;

    }
}