package com.moo.authenticationservice.services;

import com.moo.authenticationservice.exceptions.ApiRequestException;
import com.moo.authenticationservice.repositories.UserRepository;
import com.moo.authenticationservice.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUserByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User: " + username + " was not found")));
    }

    public Optional<User> authorizeAndReturnUserDetails(@RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        try {
            String username = jwtService.extractUserName(jwt);
            return findUserByUsername(username);
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }
}
