package com.moo.authenticationservice.services;

import com.moo.authenticationservice.exceptions.ApiRequestException;
import com.moo.authenticationservice.repositories.UserRepository;
import com.moo.authenticationservice.user.User;

import com.moo.authenticationservice.user.UserDTO;
import com.moo.authenticationservice.user.UserDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final UserDTOMapper userDTOMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    public UserService(UserRepository userRepository, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.userDTOMapper = userDTOMapper;
    }

    public Optional<User> findUserByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User: " + username + " was not found")));
    }

    public Optional<UserDTO> authorizeAndReturnUserDetails(@RequestHeader("Authorization") String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        try {
            String username = jwtService.extractUserName(jwt);
            return findUserByUsername(username).map(userDTOMapper);
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }
}
