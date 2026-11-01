package com.anwar.aicodereview.service;

import com.anwar.aicodereview.dto.AuthResponse;
import com.anwar.aicodereview.dto.LoginRequest;
import com.anwar.aicodereview.dto.RegisterRequest;
import com.anwar.aicodereview.exception.EmailAlreadyRegisteredException;
import com.anwar.aicodereview.exception.InvalidCredentialsException;
import com.anwar.aicodereview.model.UserAccount;
import com.anwar.aicodereview.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerNormalizesEmailAndStoresOnlyEncodedPassword() {
        UUID userId = UUID.randomUUID();
        when(userAccountRepository.existsByEmail("person@example.com")).thenReturn(false);
        when(passwordEncoder.encode("long-enough-password")).thenReturn("bcrypt-hash");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> {
            UserAccount account = invocation.getArgument(0);
            account.setId(userId);
            return account;
        });
        when(jwtService.createAccessToken(userId)).thenReturn("signed-token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.register(
                new RegisterRequest(" Person@Example.com ", "long-enough-password"));

        assertEquals(userId, response.userId());
        assertEquals("signed-token", response.accessToken());
        verify(passwordEncoder).encode("long-enough-password");
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void registerRejectsAnExistingEmail() {
        when(userAccountRepository.existsByEmail("person@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class,
                () -> authService.register(new RegisterRequest("person@example.com", "long-enough-password")));
    }

    @Test
    void loginUsesTheSameGenericErrorForUnknownUsersAndWrongPasswords() {
        when(userAccountRepository.findByEmail("person@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("person@example.com", "wrong-password")));
    }
}
