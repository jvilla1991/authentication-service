package com.moo.authenticationservice.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserDTOMapper implements Function<User, UserDTO> {

    // TODO: Switch to user MapStruct
    @Override
    public UserDTO apply(User user) {
        return new UserDTO(
                user.getUuid(),
                user.getUsername(),
                user.getFirstName(),
                user.getFirstName(),
                user.getEmail(),
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    }
}
