package com.anwar.aicodereview.service;

import com.anwar.aicodereview.dto.AuthResponse;
import com.anwar.aicodereview.dto.LoginRequest;
import com.anwar.aicodereview.dto.RegisterRequest;
import com.anwar.aicodereview.exception.EmailAlreadyRegisteredException;
import com.anwar.aicodereview.exception.InvalidCredentialsException;
import com.anwar.aicodereview.model.UserAccount;
import com.anwar.aicodereview.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository userAccountRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateBcryptPasswordLength(request.password());
        String email = normalizeEmail(request.email());
        if (userAccountRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        UserAccount account = new UserAccount();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        UserAccount savedAccount = userAccountRepository.save(account);
        return createResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new InvalidCredentialsException();
        }
        String email = normalizeEmail(request.email());
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return createResponse(account);
    }

    private AuthResponse createResponse(UserAccount account) {
        return new AuthResponse(jwtService.createAccessToken(account.getId()), "Bearer",
                jwtService.getExpirationSeconds(), account.getId());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateBcryptPasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes");
        }
    }
}
