package com.moo.authenticationservice.services;

import com.moo.authenticationservice.exceptions.ApiRequestException;
import com.moo.authenticationservice.models.ResetTokenResponse;
import com.moo.authenticationservice.repositories.PasswordResetTokenRepository;
import com.moo.authenticationservice.repositories.UserRepository;
import com.moo.authenticationservice.user.PasswordResetToken;
import com.moo.authenticationservice.user.Role;
import com.moo.authenticationservice.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTests {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService service;

    private final UUID userUuid = UUID.randomUUID();
    private User user;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(tokenRepository, userRepository, passwordEncoder);
        ReflectionTestUtils.setField(service, "ttlHours", 24L);
        user = User.builder()
                .uuid(userUuid)
                .userName("frodo")
                .email("frodo@shire.me")
                .password("old-hash")
                .role(Role.USER)
                .build();
    }

    private static String sha256(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    // ── issueToken ────────────────────────────────────────────────────────────

    @Test
    void issueToken_storesHashNotRawToken_andRevokesPriorUnused() throws Exception {
        when(userRepository.findByUserName("frodo")).thenReturn(Optional.of(user));

        ResetTokenResponse response = service.issueToken("frodo");

        verify(tokenRepository).deleteByUserUuidAndUsedAtIsNull(userUuid);

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(saved.capture());
        assertNotEquals(response.token(), saved.getValue().getTokenHash());
        assertEquals(sha256(response.token()), saved.getValue().getTokenHash());
        assertEquals(userUuid, saved.getValue().getUserUuid());
        assertTrue(response.expiresAt().isAfter(Instant.now()));
    }

    @Test
    void issueToken_unknownUser_throws() {
        when(userRepository.findByUserName("nobody")).thenReturn(Optional.empty());

        assertThrows(ApiRequestException.class, () -> service.issueToken("nobody"));
        verify(tokenRepository, never()).save(any());
    }

    // ── resetPassword ─────────────────────────────────────────────────────────

    private PasswordResetToken tokenRow(String raw, Instant expiresAt, Instant usedAt) throws Exception {
        return PasswordResetToken.builder()
                .id(1L)
                .tokenHash(sha256(raw))
                .userUuid(userUuid)
                .expiresAt(expiresAt)
                .usedAt(usedAt)
                .build();
    }

    @Test
    void resetPassword_happyPath_encodesAndMarksUsed() throws Exception {
        PasswordResetToken row = tokenRow("raw", Instant.now().plusSeconds(3600), null);
        when(tokenRepository.findByTokenHash(sha256("raw"))).thenReturn(Optional.of(row));
        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword1")).thenReturn("new-hash");

        service.resetPassword("raw", "newpassword1");

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
        assertNotNull(row.getUsedAt());
        verify(tokenRepository).save(row);
    }

    @Test
    void resetPassword_expiredToken_rejected() throws Exception {
        PasswordResetToken row = tokenRow("raw", Instant.now().minusSeconds(60), null);
        when(tokenRepository.findByTokenHash(sha256("raw"))).thenReturn(Optional.of(row));

        assertThrows(ApiRequestException.class, () -> service.resetPassword("raw", "newpassword1"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_usedToken_rejected() throws Exception {
        PasswordResetToken row = tokenRow("raw", Instant.now().plusSeconds(3600), Instant.now().minusSeconds(60));
        when(tokenRepository.findByTokenHash(sha256("raw"))).thenReturn(Optional.of(row));

        assertThrows(ApiRequestException.class, () -> service.resetPassword("raw", "newpassword1"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_unknownToken_rejected() {
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThrows(ApiRequestException.class, () -> service.resetPassword("bogus", "newpassword1"));
    }

    @Test
    void resetPassword_shortPassword_rejectedBeforeTokenLookup() {
        assertThrows(ApiRequestException.class, () -> service.resetPassword("raw", "short"));
        verify(tokenRepository, never()).findByTokenHash(any());
    }

    // ── changePassword ────────────────────────────────────────────────────────

    @Test
    void changePassword_wrongCurrentPassword_rejected() {
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        assertThrows(ApiRequestException.class,
                () -> service.changePassword(user, "wrong", "newpassword1"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_happyPath_encodesNewPassword() {
        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("newpassword1")).thenReturn("new-hash");

        service.changePassword(user, "current", "newpassword1");

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_shortNewPassword_rejected() {
        when(passwordEncoder.matches("current", "old-hash")).thenReturn(true);

        assertThrows(ApiRequestException.class,
                () -> service.changePassword(user, "current", "short"));
        verify(userRepository, never()).save(any());
    }
}
