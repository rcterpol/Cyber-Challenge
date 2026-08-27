# Lab 13 — Cyber Challenge: PaySprint Merchant Portal Security Audit

**Codebase audited**: `starter/src/main/java/com/neueda/leap/merchantportal/`

---

## Executive Summary (non-technical)

---

## Findings

For each vulnerability found: file, OWASP 2025 category, real-world risk, and fix.


WebhookController.java contains A01: Broken Access Control, A08: Software and Data Integrity Failures. Since the endpoint has no authentication or signature verification, anyone on the internet who knows or guesses the URL can POST arbitrary payout/status values and force markSettled to mark any payout as settled. Thus, attackers could mark their own unpaid or rejected payout as "settled". The fix is to have the webhook provider sign their payloads (e.g., HMAC signature header) so the receiver can verify the request actually came from the trusted source.

WebhookController.java contains A03: Injection / Improper Input Validation. event.getStatus() is a raw, unvalidated String passed into markSettled. Downstream, PayoutRepository uses this string and could build a query, log statement, or shell/command call, which could enable SQL injection, log injection, or unexpected state transitions. The fix is to use an enum for PayoutStatus so that event.getStatus() can be validated against valid enum states.

---

## Remediation Priority

| # | Vulnerability | Severity |
|---|----------------|----------|
