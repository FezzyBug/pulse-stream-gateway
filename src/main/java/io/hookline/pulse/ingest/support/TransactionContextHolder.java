package io.hookline.pulse.ingest.support;

import org.slf4j.MDC;

/**
 * Captures the transaction identifier for the current request thread so it can be
 * retrieved outside of servlet filters and attached to outbound calls.
 */
public final class TransactionContextHolder {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TransactionContextHolder() {
    }

    static void bind(String transactionId) {
        CURRENT.set(transactionId);
        MDC.put("transactionId", transactionId);
    }

    static void clear() {
        CURRENT.remove();
        MDC.remove("transactionId");
    }

    public static String getTransactionId() {
        return CURRENT.get();
    }
}
