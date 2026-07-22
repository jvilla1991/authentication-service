package com.moo.authenticationservice.services;

import com.moo.authenticationservice.exceptions.ApiRequestException;
import com.moo.authenticationservice.models.ResetTokenResponse;
import com.moo.authenticationservice.repositories.PasswordResetTokenRepository;
import com.moo.authenticationservice.repositories.UserRepository;
import com.moo.authenticationservice.user.PasswordResetToken;
import com.moo.authenticationservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Password recovery without email: an ADMIN issues a one-time reset token and
 * hands the resulting link to the user out-of-band (Discord/text). Tokens are
 * stored SHA-256-hashed, are single-use, and expire after
 * {@code app.reset-token.ttl-hours} (default 24 — hand-delivery can lag).
 * Also hosts the logged-in change-password flow, which shares the same
 * password rules.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String INVALID_TOKEN_MESSAGE = "Reset link is invalid or has expired";

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.reset-token.ttl-hours:24}")
    private long ttlHours;

    /**
     * Admin path: mint a one-time token for the given user. Any earlier
     * unused tokens for that user are revoked so only the newest link works.
     * Returns the raw token — it is never stored or logged.
     */
    @Transactional
    public ResetTokenResponse issueToken(String userName) {
        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new ApiRequestException("User does not Exist"));

        tokenRepository.deleteByUserUuidAndUsedAtIsNull(user.getUuid());

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant expiresAt = Instant.now().plus(Duration.ofHours(ttlHours));
        tokenRepository.save(PasswordResetToken.builder()
                .tokenHash(sha256(raw))
                .userUuid(user.getUuid())
                .expiresAt(expiresAt)
                .build());

        return new ResetTokenResponse(raw, expiresAt);
    }

    /**
     * Public path: exchange a valid token for a new password. One generic
     * error for missing/used/expired so the endpoint leaks nothing about
     * which tokens exist.
     */
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ApiRequestException(INVALID_TOKEN_MESSAGE);
        }
        validateNewPassword(newPassword);

        PasswordResetToken token = tokenRepository.findByTokenHash(sha256(rawToken))
                .orElseThrow(() -> new ApiRequestException(INVALID_TOKEN_MESSAGE));
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiRequestException(INVALID_TOKEN_MESSAGE);
        }

        User user = userRepository.findByUuid(token.getUserUuid())
                .orElseThrow(() -> new ApiRequestException(INVALID_TOKEN_MESSAGE));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    /** Logged-in path: verify the current password, then set the new one. */
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ApiRequestException("Current password is incorrect");
        }
        validateNewPassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private static void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new ApiRequestException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
