package com.moo.authenticationservice.controllers;

import com.moo.authenticationservice.config.JwtAuthenticationFilter;
import com.moo.authenticationservice.config.SecurityConfiguration;
import com.moo.authenticationservice.models.AuthenticationResponse;
import com.moo.authenticationservice.services.AuthenticationService;
import com.moo.authenticationservice.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthenticationController.class)
@Import(SecurityConfiguration.class)
class AuthenticationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @Test
    void register_isPublic() throws Exception {
        when(authenticationService.register(any()))
                .thenReturn(new AuthenticationResponse(/* ... */));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"pw\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void authenticate_isPublic() throws Exception {
        when(authenticationService.authenticate(any()))
                .thenReturn(new AuthenticationResponse(/* ... */));

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"pw\"}"))
                .andExpect(status().isOk());
    }
}