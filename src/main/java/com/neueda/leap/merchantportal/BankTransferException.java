package com.neueda.leap.merchantportal;

public class BankTransferException extends RuntimeException {

    private static final String GENERIC_MESSAGE = "Bank transfer failed";

    // Full internal detail, for server-side logging only - never exposed via getMessage()
    private final String internalDetail;

    public BankTransferException(String internalDetail) {
        super(GENERIC_MESSAGE);
        this.internalDetail = internalDetail;
    }

    public String getInternalDetail() {
        return internalDetail;
    }
}
