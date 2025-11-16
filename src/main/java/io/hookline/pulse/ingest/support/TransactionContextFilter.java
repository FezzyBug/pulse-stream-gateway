package io.hookline.pulse.ingest.support;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TransactionContextFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_HEADER = "X-Pulse-Transaction-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String transactionId = resolveTransactionId(request);
        TransactionContextHolder.bind(transactionId);
        response.setHeader(TRANSACTION_HEADER, transactionId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TransactionContextHolder.clear();
        }
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String headerValue = request.getHeader(TRANSACTION_HEADER);
        if (StringUtils.hasText(headerValue)) {
            return headerValue;
        }
        return UUID.randomUUID().toString();
    }
}
