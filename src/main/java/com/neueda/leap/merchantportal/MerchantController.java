package com.neueda.leap.merchantportal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MerchantController {

    @Autowired
    private PayoutRepository payoutRepository;

    @GetMapping("/api/payouts/{payoutId}")
    public PayoutRequest getPayout(@PathVariable Long payoutId,
                                    @RequestHeader("X-Merchant-Id") Long requestingMerchantId) {
        PayoutRequest payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Payout not found"));

        // Prevent BOLA/IDOR: only the owning merchant may view this payout
        if (!payout.getMerchantId().equals(requestingMerchantId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Payout not found");
        }

        return payout;
    }
}
