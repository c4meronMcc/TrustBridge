package com.trustbridge.Features.Auth.Service;

import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Auth.Dto.LoginDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;

import java.net.Authenticator;


@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;


    public void login(LoginDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );


        var user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Check verification
        if (!user.getIsVerified()) {
            throw new RuntimeException("Account not verified.");
        }
    }
}
