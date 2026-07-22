package com.moo.authenticationservice.repositories;

import com.moo.authenticationservice.user.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
    void deleteByUserUuidAndUsedAtIsNull(UUID userUuid);

}
