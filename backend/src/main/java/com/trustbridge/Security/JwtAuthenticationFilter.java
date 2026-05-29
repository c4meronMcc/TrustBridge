package com.trustbridge.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService; // Your class that generates/decodes tokens
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("FILTER TRIGGERED: Checking cookies for: " + request.getRequestURI());

        final String jwt = getJwtFromRequest(request);
        final String userEmail;

        if (jwt == null) {
            System.out.println("NO JWT FOUND IN REQUEST");
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("JWT FOUND IN REQUEST: " + jwt);

        userEmail = jwtService.extractEmail(jwt); // Extract the email from the token payload

        // 4. If we found an email and the user isn't already authenticated in this context...
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 5. If the token is valid, formally authenticate them in Spring Security
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Boom: The user is no longer "Anonymous"
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("User successfully authenticated via cookie!");
            } else {
                System.out.println("Token was invalid or expired.");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                System.out.println("FOUND COOKIE: " + cookie.getName() + " = " + cookie.getValue());
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        } else {
            System.out.println("NO COOKIES FOUND IN REQUEST");
        }
        return null;
    }
}