package com.trustbridge.Features.Auth;

import com.trustbridge.Features.Auth.Dto.LoginDto;
import com.trustbridge.Features.Auth.Dto.RegistrationDTO;
import jakarta.servlet.Registration;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationDTO dto) {
        registrationService.register(dto);
        return ResponseEntity.ok("User registered successfully!");
    }



    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDto dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        return ResponseEntity.ok("Successfully logged in!");
    }

}
