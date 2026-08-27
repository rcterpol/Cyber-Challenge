package com.neueda.leap.merchantportal;

import java.util.List;

public class BatchPayoutJob {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BatchPayoutJob.class);

    private BankTransferClient bankTransferClient;
    private PayoutRepository payoutRepository;

    public BatchPayoutJob(BankTransferClient bankTransferClient, PayoutRepository payoutRepository) {
        this.bankTransferClient = bankTransferClient;
        this.payoutRepository = payoutRepository;
    }

    public void runNightlyBatch(List<PayoutRequest> approvedPayouts) {
        for (PayoutRequest payout : approvedPayouts) {
            // Never transfer funds for a payout that hasn't actually completed approval
            if (!"APPROVED".equals(payout.getApprovalStatus())) {
                log.warn("Skipping payout {} with unexpected status {}",
                        payout.getId(), payout.getApprovalStatus());
                continue;
            }
            try {
                bankTransferClient.transfer(payout.getMerchantId(), payout.getAmount());
                payout.setApprovalStatus("PAID");
            } catch (BankTransferException e) {
                log.warn("Transfer failed for payout {}: {}",
                        payout.getId(), e.getMessage());
                payout.setApprovalStatus("REJECTED");
            }
            payoutRepository.save(payout);
        }
    }
}
