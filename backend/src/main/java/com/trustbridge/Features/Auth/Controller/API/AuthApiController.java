package com.trustbridge.Features.Auth.Controller.API;

import com.trustbridge.Features.Auth.Dto.*;
import com.trustbridge.Features.Auth.Service.LoginService;
import com.trustbridge.Security.JwtService;
import com.trustbridge.Security.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.logging.Logger;

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
    private Authentication auth;
    private ApiResponse apiResponse;

    String message = "message";
    String error = "error";

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegistrationDTO request) {
        registrationService.registerPreVerification(request);
        return ResponseEntity.ok(new ApiResponse("User registration successful"));

    }

    @PostMapping("/verificationCode")
    public ResponseEntity<ApiResponse> verifyCode(@Valid @RequestBody RegistrationVerificationDTO dto) {
        registrationService.registerPostVerification(dto);

        return ResponseEntity.ok(new ApiResponse("User verified successfully!"));
    }

    @PostMapping("/resendVerificationCode")
    public ResponseEntity<ApiResponse> resendVerificationCode(@RequestBody String email) {
        registrationService.resetVerificationCode(email);

        return ResponseEntity.ok(new ApiResponse("Verification code resent successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginDto dto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            dto.email(),
                            dto.password()
                    )
            );

            loginService.login(dto);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);

            Logger.getLogger("AuthApiController").info("AT BIRTH LOGIN COOKIE: " + jwt);

            int cookieMaxAge = 24 * 60 * 60;

            ResponseCookie springCookie = ResponseCookie.from("jwt_token", jwt)
                    .httpOnly(true)
                    .secure(true) // Set to true in production
                    .path("/")
                    .maxAge(cookieMaxAge)
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                    .body(new ApiResponse("Login successful"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiErrorResponse(
                            "UNVERIFIED_ACCOUNT",
                            e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser() {
        ResponseCookie cleanCookie = ResponseCookie.from("jwt_token", "")
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(new ApiResponse("Logged out successfully"));
    }
}
