package com.neueda.leap.merchantportal;

public class PayoutApprovalService {

    private PayoutRepository payoutRepository;

    public PayoutApprovalService(PayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    public void approve(Long payoutId, Long approvingUserId) {
        PayoutRequest payout = payoutRepository.findById(payoutId)
                .orElseThrow(() -> new BankTransferException("Payout not found"));

        // maker-checker: the requester must not be able to approve their own payout
        if (payout.getRequestedByUserId().equals(approvingUserId)) {
            throw new BankTransferException("Requester cannot approve their own payout");
        }

        // only a PENDING payout may transition to APPROVED
        if (!"PENDING".equals(payout.getApprovalStatus())) {
            throw new BankTransferException("Payout is not pending approval");
        }

        payout.setApprovalStatus("APPROVED");
        payout.setApprovedByUserId(approvingUserId);
        payoutRepository.save(payout);
    }
}

