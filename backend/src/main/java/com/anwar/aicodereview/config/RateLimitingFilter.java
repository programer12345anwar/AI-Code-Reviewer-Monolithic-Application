package com.anwar.aicodereview.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int GENERAL_MAX_REQUESTS = 100;
    private static final long WINDOW_MS = 60_000L;
    private static final int MAX_BUCKETS = 10_000;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }
        int maxRequests = path.startsWith("/api/v1/auth/") ? 10
                : path.contains("/analyze/") ? 20 : GENERAL_MAX_REQUESTS;
        String bucketId = request.getRemoteAddr() + ':' + rateLimitGroup(path);
        Bucket bucket = getBucket(bucketId);
        if (bucket == null) {
            reject(response);
            return;
        }
        synchronized (bucket) {
            long now = System.currentTimeMillis();
            if (now - bucket.windowStart > WINDOW_MS) {
                bucket.windowStart = now;
                bucket.count = 0;
            }
            if (++bucket.count > maxRequests) {
                reject(response);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private Bucket getBucket(String bucketId) {
        synchronized (buckets) {
            Bucket existing = buckets.get(bucketId);
            if (existing != null) {
                return existing;
            }
            long cutoff = System.currentTimeMillis() - WINDOW_MS;
            buckets.entrySet().removeIf(entry -> entry.getValue().windowStart < cutoff);
            if (buckets.size() >= MAX_BUCKETS) {
                return null;
            }
            Bucket created = new Bucket();
            buckets.put(bucketId, created);
            return created;
        }
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many requests\",\"details\":[],\"timestamp\":\""
                + Instant.now() + "\"}");
    }

    private String rateLimitGroup(String path) {
        if (path.startsWith("/api/v1/auth/")) {
            return "auth";
        }
        if (path.contains("/analyze/")) {
            return "analysis";
        }
        return "general";
    }

    private static class Bucket {
        private long windowStart = System.currentTimeMillis();
        private int count;
    }
}
