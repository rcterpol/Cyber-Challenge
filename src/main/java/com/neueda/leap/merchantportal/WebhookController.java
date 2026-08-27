package com.neueda.leap.merchantportal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@RestController
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final PayoutStatusUpdater payoutStatusUpdater;
    private final ObjectMapper objectMapper;
    private final String webhookSecret;

    public WebhookController(PayoutStatusUpdater payoutStatusUpdater,
                              ObjectMapper objectMapper,
                              @Value("${webhooks.payment-status.secret}") String webhookSecret) {
        this.payoutStatusUpdater = payoutStatusUpdater;
        this.objectMapper = objectMapper;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/api/webhooks/payment-status")
    public ResponseEntity<Void> handlePaymentStatusWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Signature") String signatureHeader) throws Exception {

        if (!isValidSignature(rawBody, signatureHeader)) {
            log.warn("Rejected payment-status webhook: invalid signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PaymentStatusEvent event = objectMapper.readValue(rawBody, PaymentStatusEvent.class);
        if (event.getPayoutId() == null || event.getStatus() == null) {
            log.warn("Rejected payment-status webhook: missing payoutId or status");
            return ResponseEntity.badRequest().build();
        }

        payoutStatusUpdater.markSettled(event.getPayoutId(), event.getStatus());
        log.info("Processed payment-status webhook for payoutId={} status={}",
                event.getPayoutId(), event.getStatus());
        return ResponseEntity.ok().build();
    }

    private boolean isValidSignature(String rawBody, String signatureHeader) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
        byte[] computed = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
        String expectedHex = HexFormat.of().formatHex(computed);
        // constant-time comparison to avoid timing attacks
        return MessageDigest.isEqual(
                expectedHex.getBytes(StandardCharsets.UTF_8),
                signatureHeader.getBytes(StandardCharsets.UTF_8));
    }
}


