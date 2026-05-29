package com.trustbridge.Features.Auth.Controller.API;

import com.trustbridge.Features.Auth.Service.LoginService;
import com.trustbridge.Security.JwtService;
import com.trustbridge.Security.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import com.trustbridge.Features.Auth.Dto.LoginDto;
import com.trustbridge.Features.Auth.Dto.RegistrationDTO;
import com.trustbridge.Features.Auth.Dto.RegistrationVerificationDTO;
import com.trustbridge.Features.Auth.Service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtTokenProvider jwtTokenProvider;
    private Authentication authentication;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationDTO request) {
        registrationService.registerPreVerification(request);
        return ResponseEntity.ok(Map.of("message", "User registration successful"));
    }

    @PostMapping("/verificationCode")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody RegistrationVerificationDTO dto) {
        registrationService.registerPostVerification(dto);

        return ResponseEntity.ok(Map.of("message", "User verified successfully!"));
    }

    @PostMapping("/resendVerificationCode")
    public ResponseEntity<?> resendVerificationCode(@RequestBody String email) {
        registrationService.resetVerificationCode(email);

        return ResponseEntity.ok(Map.of("message", "Verification code resent successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto dto) {
        try {
            // 1. Authenticate using the manager (this returns the 'authenticated' object)
            Authentication authentication = authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            dto.email(),
                            dto.password()
                    )
            );

            // 2. Perform your business logic check (is the account verified?)
            loginService.login(dto);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);

            System.out.println("AT BIRTH LOGIN COOKIE: " + jwt);

            // 4. Build the cookie
            ResponseCookie springCookie = ResponseCookie.from("jwt_token", jwt)
                    .httpOnly(true)
                    .secure(false) // Set to true in production
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                    .body(Map.of("message", "Login successful"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "UNVERIFIED_ACCOUNT",
                            "message", e.getMessage() // Dynamically show the error from service
                    ));
        } catch (Exception e) {
            // Handle invalid password/email specifically
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // Overwrite the cookie with a blank value and a 0 lifespan
        ResponseCookie cleanCookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body("Logged out successfully");
    }
}
