package com.moo.authenticationservice.config;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // TODO: What does this do?
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
// On application startup, Spring Security will try to look for a bean of type "Security filter chain"
    //This chain is responsible or configuring all the HTTP Security of our application

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable()
                // Whitelist means we have some endpoints that dont require any authentication or tokens to access
                .authorizeHttpRequests()
                .requestMatchers("/api/v1/auth/**") // Insert endpoints here for whitelisting,
                .permitAll()
                .anyRequest() // Otherwise, all other requests need to be authenticated
                .authenticated()
                .and()// Adds a new configuration
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // We want to execute this filter before we do the "UserNameAuthentication" Filter.

        return httpSecurity.build();
    }
}
