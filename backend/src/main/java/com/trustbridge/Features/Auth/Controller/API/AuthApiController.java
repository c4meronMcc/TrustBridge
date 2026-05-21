package com.trustbridge.Features.Auth.Controller.API;

import com.trustbridge.Features.Auth.Service.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import com.trustbridge.Features.Auth.Dto.LoginDto;
import com.trustbridge.Features.Auth.Dto.RegistrationDTO;
import com.trustbridge.Features.Auth.Dto.RegistrationVerificationDTO;
import com.trustbridge.Features.Auth.Service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
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
            // Call the service layer
            loginService.login(dto);

            // If it succeeds, return 200 OK
            return ResponseEntity.ok(Map.of("message", "Successfully logged in!"));

        } catch (RuntimeException e) {
            // If the specific verification exception is thrown, return 403 Forbidden
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", "UNVERIFIED_ACCOUNT",
                            "message", "Please check your email to verify your account."
                    ));
        }
    }
}
