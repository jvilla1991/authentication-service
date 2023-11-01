package com.moo.authenticationservice.user;

import java.util.List;
import java.util.UUID;

public record UserDTO(UUID uuid,
                      String userName,
                      String firstName,
                      String lastName,
                      String email,
                      List<String> roles) {
}
