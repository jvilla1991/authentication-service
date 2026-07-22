package com.moo.authenticationservice.controllers;

import com.moo.authenticationservice.models.AuthenticationRequest;
import com.moo.authenticationservice.models.AuthenticationResponse;
import com.moo.authenticationservice.models.RegisterRequest;
import com.moo.authenticationservice.models.ResetPasswordRequest;
import com.moo.authenticationservice.services.AuthenticationService;
import com.moo.authenticationservice.services.PasswordResetService;
import com.moo.authenticationservice.services.UserService;
import com.moo.authenticationservice.user.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    /**
     * Public: redeem an admin-issued one-time reset token for a new password.
     * No JWT is minted — the user signs in normally afterwards.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthenticationResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(AuthenticationResponse.builder().success(true).build());
    }

    @GetMapping("/authorize")
    public ResponseEntity<UserDTO> authorize(@RequestHeader("Authorization") String authorizationHeader) {
        UserDTO user = userService.authorizeAndReturnUserDetails(authorizationHeader)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

}
