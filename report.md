# Lab 13 — Cyber Challenge: PaySprint Merchant Portal Security Audit

**Codebase audited**: `starter/src/main/java/com/neueda/leap/merchantportal/`

---

## Executive Summary (non-technical)

---

## Findings

For each vulnerability found: file, OWASP 2025 category, real-world risk, and fix.

MerchantController contains a possible A01 vulnerability, where a bad actor can query the database through the GetPayout method without proper authorization. This could lead to payment data being leaked. A fix can be implemented through implementation of proper access control. Additionally, error messages and status codes should match those of an invalid payment in order to limit information exposed.

BatchPayoutJob contains an A10 vulnerability, where a transfer will be marked as paid regardless of transfer result. This can result in poor logging as invalid transactions will not be shown as such. Also, invalid payments will nonetheless be marked as having been paid without any transfer of funds. The fix can be implemented by marking invalid payments as such rather than paid and not processing the transfer.

BatchPayoutJob also never checks that the payments are approved, an A01 vulnerability. Since payments are never checked for approval status, an invalid payment may be passed to the function and payment could be processed on an invalid payment. A simple check for each payout as they pass through the function will stop a non-approved payment from being processed.

BankTransferException contains an A06 vulnerability, where the full stack trace is printed to the error message. Should this exception be propagated to the user, they could gain knowledge of the internal functionality of the system. The fix is to print a pruned error message instead of the stack trace. The internal details will be maintained for logging purposes, but not reported to the user.

WebhookController.java contains A01: Broken Access Control, A08: Software and Data Integrity Failures. Since the endpoint has no authentication or signature verification, anyone on the internet who knows or guesses the URL can POST arbitrary payout/status values and force markSettled to mark any payout as settled. Thus, attackers could mark their own unpaid or rejected payout as "settled". The fix is to have the webhook provider sign their payloads (e.g., HMAC signature header) so the receiver can verify the request actually came from the trusted source.

WebhookController.java contains A03: Injection / Improper Input Validation. event.getStatus() is a raw, unvalidated String passed into markSettled. Downstream, PayoutRepository uses this string and could build a query, log statement, or shell/command call, which could enable SQL injection, log injection, or unexpected state transitions. The fix is to use an enum for PayoutStatus so that event.getStatus() can be validated against valid enum states.

WebhookController.java contains A09: Security Logging and Monitoring Failures. No logging of incoming webhook calls is present, so a forged or replayed request would leave no audit trail. The fix is to include logged warnings to alert when a request signature is incorrect, when status or id is null, and when the request does go through properly.

---

## Remediation Priority

| # | Vulnerability | Severity |
|---|----------------|----------|
