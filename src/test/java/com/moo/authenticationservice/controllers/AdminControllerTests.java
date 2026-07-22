package com.moo.authenticationservice.controllers;

import com.moo.authenticationservice.models.ResetTokenResponse;
import com.moo.authenticationservice.repositories.UserRepository;
import com.moo.authenticationservice.services.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// NOTE: real users carry the bare enum name (ADMIN) as their authority — see
// User.getAuthorities() — so these tests use authorities=, not roles= (which
// would mint ROLE_ADMIN and quietly test a permission model we don't have).
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        // Migrations are Postgres-specific SQL; Hibernate builds the schema here.
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=update",

        // Dummy JWT key so JwtService can be constructed IF you don't mock it
        // This is just some base64 string with enough length for HS256
        "DECODER_KEY=VGhpc0lzQUR1bW15SldUU2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaA=="
})
@AutoConfigureMockMvc
class AdminControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordResetService passwordResetService;

    @Test
    @WithAnonymousUser
    void adminUsers_anonymous_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @WithMockUser(authorities = "USER")
    void adminUsers_userRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminUsers_adminRole_isOk() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk()); // 200
    }

    @Test
    @WithMockUser(authorities = "USER")
    void issueResetToken_userRole_isForbidden() throws Exception {
        mockMvc.perform(post("/api/admin/users/frodo/reset-token"))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void issueResetToken_adminRole_returnsRawToken() throws Exception {
        when(passwordResetService.issueToken("frodo"))
                .thenReturn(new ResetTokenResponse("raw-token", Instant.parse("2026-07-23T00:00:00Z")));

        mockMvc.perform(post("/api/admin/users/frodo/reset-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("raw-token"));
    }
}
