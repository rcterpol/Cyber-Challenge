package com.neueda.leap.merchantportal;

public class PaymentStatusEvent {
    private Long payoutId;
    private PayoutStatus status;

    public Long getPayoutId() { return payoutId; }
    public PayoutStatus getStatus() { return status; }
}
