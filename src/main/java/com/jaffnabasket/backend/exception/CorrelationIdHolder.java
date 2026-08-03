package com.jaffnabasket.backend.exception;

import org.slf4j.MDC;

public final class CorrelationIdHolder {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationIdHolder() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }
}
