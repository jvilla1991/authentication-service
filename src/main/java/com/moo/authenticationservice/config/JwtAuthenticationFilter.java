package com.moo.authenticationservice.config;

import com.moo.authenticationservice.services.JwtService;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Can also be "@repository" or "@service" as they all extend "@Component," but this is more generic
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, // Our HTTP Request
            @NonNull HttpServletResponse response, // Our Response, allows us to intercept every request and provide new data within the response (e.g. we can provide a new header in the response)
            @NonNull FilterChain filterChain) // TODO: Chain of Responsibility Design Pattern, contains list of filters that we need to execute
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization"); // Contains JWT, also known as the "Bearer" Token
        final String jwt;
        final String userName;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Goes to the next filter if the conditional resolves to "false"
            return;
        }
        jwt = authHeader.substring(7);
        userName = jwtService.extractUserName(jwt);
        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userName);
            if (jwtService.isTokenValid(jwt, userDetails)) { // If the User is valid, we need to update the security context and then send the request to the dispatcher servlet
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Here we don't have credentials when we created the user
                        userDetails.getAuthorities());

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request, response);
            // At this point, JwtAuthFilter Validation and User Details service is all implemented.Now we need to bind them together. Spring Boot 3 Amigoscode Video @ 1:26
        }
    }
}
