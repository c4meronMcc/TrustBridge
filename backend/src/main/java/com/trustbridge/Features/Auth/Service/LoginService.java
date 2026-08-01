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

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        var user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getIsVerified()) {
            throw new RuntimeException("Account not verified.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.email());

        return jwtService.generateToken(userDetails);
    }
}