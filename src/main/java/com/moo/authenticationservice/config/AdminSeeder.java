package com.moo.authenticationservice.config;

import com.moo.authenticationservice.repositories.UserRepository;
import com.moo.authenticationservice.user.Role;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * On startup, promotes the user named by {@code admin.username} (env var
 * ADMIN_USERNAME) to the ADMIN role. Idempotent and safe to run on every boot:
 * it only elevates an existing account, never changes passwords or creates users.
 *
 * <p>Usage: register normally through the app, set ADMIN_USERNAME to that username
 * (wired by Terraform), and redeploy. If the variable is blank, seeding is skipped.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;

    @Value("${admin.username:}")
    private String adminUsername;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUsername == null || adminUsername.isBlank()) {
            return; // no admin configured — nothing to do
        }

        userRepository.findByUserName(adminUsername).ifPresentOrElse(
                user -> {
                    if (user.getRole() != Role.ADMIN) {
                        user.setRole(Role.ADMIN);
                        userRepository.save(user);
                        log.info("AdminSeeder: promoted user '{}' to ADMIN", adminUsername);
                    } else {
                        log.info("AdminSeeder: user '{}' is already ADMIN", adminUsername);
                    }
                },
                () -> log.warn("AdminSeeder: ADMIN_USERNAME='{}' is set but no such user exists yet; "
                        + "register that account first, then redeploy to promote it", adminUsername)
        );
    }
}
