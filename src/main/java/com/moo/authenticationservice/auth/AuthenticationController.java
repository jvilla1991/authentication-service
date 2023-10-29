package com.moo.authenticationservice.auth;

import com.moo.authenticationservice.models.AuthenticationRequest;
import com.moo.authenticationservice.models.AuthenticationResponse;
import com.moo.authenticationservice.models.RegisterRequest;
import com.moo.authenticationservice.services.AuthenticationService;
import com.moo.authenticationservice.services.UserService;
import com.moo.authenticationservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/authorize")
    public ResponseEntity<User> authorize(@RequestHeader("Authorization") String authorizationHeader) {
        User user = userService.authorizeAndReturnUserDetails(authorizationHeader).get();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
