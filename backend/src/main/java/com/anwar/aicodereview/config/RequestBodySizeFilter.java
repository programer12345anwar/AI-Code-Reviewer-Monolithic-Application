package com.anwar.aicodereview.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestBodySizeFilter extends OncePerRequestFilter {

    public static final long MAX_REQUEST_BODY_BYTES = 256 * 1024;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String contentType = request.getContentType();
        if (!request.getRequestURI().startsWith("/api/") || contentType == null
                || !contentType.toLowerCase(Locale.ROOT).startsWith("application/json")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getContentLengthLong() > MAX_REQUEST_BODY_BYTES) {
            writePayloadTooLarge(response);
            return;
        }

        try {
            filterChain.doFilter(new SizeLimitedRequest(request), response);
        } catch (RequestBodyTooLargeException ex) {
            writePayloadTooLarge(response);
        }
    }

    private void writePayloadTooLarge(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"error\":\"PAYLOAD_TOO_LARGE\",\"message\":\"JSON request body exceeds 256 KiB\","
                + "\"details\":[],\"timestamp\":\"" + java.time.Instant.now() + "\"}");
    }

    private static class SizeLimitedRequest extends HttpServletRequestWrapper {

        private final ServletInputStream inputStream;

        private SizeLimitedRequest(HttpServletRequest request) throws IOException {
            super(request);
            this.inputStream = new SizeLimitedInputStream(request.getInputStream());
        }

        @Override
        public ServletInputStream getInputStream() {
            return inputStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            String encoding = getCharacterEncoding();
            Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
            return new BufferedReader(new InputStreamReader(inputStream, charset));
        }
    }

    private static class SizeLimitedInputStream extends ServletInputStream {

        private final ServletInputStream delegate;
        private long bytesRead;

        private SizeLimitedInputStream(ServletInputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            if (bytesRead == MAX_REQUEST_BODY_BYTES) {
                if (delegate.read() == -1) {
                    return -1;
                }
                throw new RequestBodyTooLargeException();
            }
            int value = delegate.read();
            if (value != -1) {
                bytesRead++;
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (length == 0) {
                return 0;
            }
            if (bytesRead == MAX_REQUEST_BODY_BYTES) {
                return read();
            }
            int allowedLength = (int) Math.min(length, MAX_REQUEST_BODY_BYTES - bytesRead);
            int count = delegate.read(buffer, offset, allowedLength);
            if (count > 0) {
                bytesRead += count;
            }
            return count;
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            delegate.setReadListener(readListener);
        }
    }

    public static class RequestBodyTooLargeException extends IOException {
    }
}
