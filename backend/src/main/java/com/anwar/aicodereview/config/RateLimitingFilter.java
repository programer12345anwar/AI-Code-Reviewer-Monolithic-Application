package com.anwar.aicodereview.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(2)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MS = 60_000L;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientId = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clientId, id -> new Bucket());
        synchronized (bucket) {
            long now = System.currentTimeMillis();
            if (now - bucket.windowStart > WINDOW_MS) {
                bucket.windowStart = now;
                bucket.count.set(0);
            }
            if (bucket.count.incrementAndGet() > MAX_REQUESTS) {
                response.sendError(429, "Rate limit exceeded");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static class Bucket {
        private long windowStart = System.currentTimeMillis();
        private final AtomicInteger count = new AtomicInteger();
    }
}
