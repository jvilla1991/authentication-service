package com.moo.authenticationservice.repositories;

import com.moo.authenticationservice.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmailOrUserName(String email, String userName);

}
