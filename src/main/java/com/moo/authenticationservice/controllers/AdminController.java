package com.moo.authenticationservice.controllers;

import com.moo.authenticationservice.models.ResetTokenResponse;
import com.moo.authenticationservice.repositories.UserRepository;
import com.moo.authenticationservice.services.PasswordResetService;
import com.moo.authenticationservice.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// hasAuthority, not hasRole: our authorities are the bare enum names (ADMIN),
// while hasRole would look for a ROLE_-prefixed authority and always deny.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;

    public AdminController(UserRepository userRepository, PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.passwordResetService = passwordResetService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Mint a one-time password-reset token for the given user. The response
     * carries the raw token; the frontend composes the shareable link and the
     * admin hands it to the user out-of-band.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/users/{userName}/reset-token")
    public ResponseEntity<ResetTokenResponse> issueResetToken(@PathVariable String userName) {
        return ResponseEntity.ok(passwordResetService.issueToken(userName));
    }
}
