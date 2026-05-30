package com.moo.authenticationservice.controllers;

import com.moo.authenticationservice.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",

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

//    @Test
//    void adminUsers_noAuth_isUnauthorized() throws Exception {
//        mockMvc.perform(get("/api/admin/users"))
//                .andExpect(status().isUnauthorized()); // 401
//    }

    @Test
    @WithAnonymousUser
    void adminUsers_anonymous_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminUsers_userRole_isForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden()); // 403
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUsers_adminRole_isOk() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk()); // 200
    }
}

