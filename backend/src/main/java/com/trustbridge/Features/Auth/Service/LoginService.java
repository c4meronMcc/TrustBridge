package com.trustbridge.Features.Auth.Service;

import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Auth.Dto.LoginDto;
import com.trustbridge.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService; // Safely loads the Spring Security profile
    private final JwtService jwtService; // Our new token generator

    public String login(LoginDto dto) {

        // 1. Authenticate the raw credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        // 2. Fetch your custom user entity to check your business logic
        var user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Check verification status
        if (!user.getIsVerified()) {
            throw new RuntimeException("Account not verified.");
        }

        // 4. Load the core Spring Security UserDetails profile
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.email());

        // 5. Generate and return the signed JWT
        return jwtService.generateToken(userDetails);
    }
}