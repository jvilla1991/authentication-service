package com.moo.authenticationservice.models;

import java.time.Instant;

/**
 * Returned to the admin who issued a reset token. Carries the RAW token —
 * the only place it ever appears; the database holds just its hash. The
 * frontend composes the full reset link from it.
 */
public record ResetTokenResponse(String token, Instant expiresAt) {
}
