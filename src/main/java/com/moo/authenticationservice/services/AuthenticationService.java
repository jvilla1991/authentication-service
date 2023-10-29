package com.moo.authenticationservice.services;

import com.moo.authenticationservice.exceptions.ApiException;
import com.moo.authenticationservice.exceptions.ApiRequestException;
import com.moo.authenticationservice.models.AuthenticationRequest;
import com.moo.authenticationservice.models.AuthenticationResponse;
import com.moo.authenticationservice.models.RegisterRequest;
import com.moo.authenticationservice.repositories.UserRepository;
import com.moo.authenticationservice.user.Role;
import com.moo.authenticationservice.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder() // TODO: Why are we using var here?
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .userName(request.getUserName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        Optional<User> existingUser = userRepository.findByEmailOrUserName(user.getEmail(), user.getUsername());
        if (existingUser.isPresent()) {
            throw new ApiRequestException("User Already Exists");
        }
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new ApiRequestException("User does not Exist"));
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
