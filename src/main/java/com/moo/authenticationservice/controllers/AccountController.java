package com.moo.authenticationservice.controllers;

import com.moo.authenticationservice.models.AuthenticationResponse;
import com.moo.authenticationservice.models.ChangePasswordRequest;
import com.moo.authenticationservice.services.PasswordResetService;
import com.moo.authenticationservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Self-service account actions for signed-in users. Deliberately outside
 * /api/v1/auth/** (which is permitAll) so these fall under
 * anyRequest().authenticated() and the JWT filter supplies the principal.
 */
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/change-password")
    public ResponseEntity<AuthenticationResponse> changePassword(@AuthenticationPrincipal User user,
                                                                 @RequestBody ChangePasswordRequest request) {
        passwordResetService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(AuthenticationResponse.builder().success(true).build());
    }
}
