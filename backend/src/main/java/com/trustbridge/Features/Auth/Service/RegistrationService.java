package com.trustbridge.Features.Auth.Service;

import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.UserRole;
import com.trustbridge.Domain.Enums.UserRole.*;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Auth.Dto.RegistrationDTO;
import com.trustbridge.Features.Auth.Dto.RegistrationVerificationDTO;
import com.trustbridge.Features.Auth.Events.UserRegistrationEvent;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import com.trustbridge.Features.Notifications.Listeners.AuthEmail;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();
    private final AuthEmail authEmail;
    private final ApplicationEventPublisher eventPublisher;

    public String VerificationCode() {
        int verificationCode = random.nextInt(999999) + 1;

        return String.format("%06d", verificationCode);
    };

    @Transactional
    public void registerPreVerification(RegistrationDTO dto) {

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        role role;

        switch (dto.role()) {
            case "freelancer":
                role = UserRole.role.FREELANCER; break;
            case "client":
                role = UserRole.role.CLIENT; break;
            case "admin":
                role = UserRole.role.ADMIN; break;
            default:
                role = UserRole.role.CLIENT_GUEST;
        }

        Users newUser = Users.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .userRole(role)
                .phoneNumber(dto.phoneNumber())
                .verificationCode(VerificationCode())
                .isVerified(false)
                .verificationCodeExpiry(OffsetDateTime.now().plusMinutes(15))
                .build();

        userRepository.save(newUser);

        UserRegistrationEvent event = new UserRegistrationEvent(
                newUser.getEmail(),
                newUser.getFirstName(),
                newUser.getVerificationCode()
        );

        if (event == null) {
            throw new RuntimeException("Failed to create UserRegistrationEvent");
        }

        eventPublisher.publishEvent(event);
    }

    @Transactional
    public void registerPostVerification(RegistrationVerificationDTO dto) {

        Users user = checkUserExists(dto.email());

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new IllegalStateException("User is already verified");
        }

        boolean isCodeNull = user.getVerificationCode() == null;
        boolean isCodeExpired = user.getVerificationCodeExpiry() != null &&
                user.getVerificationCodeExpiry().isBefore(OffsetDateTime.now());

        boolean isCodeInvalid = isCodeNull || !MessageDigest.isEqual(
                user.getVerificationCode().getBytes(StandardCharsets.UTF_8),
                dto.verificationCode().getBytes(StandardCharsets.UTF_8)
        );

        if (isCodeNull || isCodeExpired || isCodeInvalid) {
            throw new IllegalArgumentException("The verification code is incorrect or expired.");
        }

        user.setIsVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public void resetVerificationCode(String email) {
        log.info("Resetting verification code for user: {}", email);
        Users user = checkUserExists(email);

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new IllegalStateException("User is already verified");
        }

        user.setVerificationCode(VerificationCode());
        user.setVerificationCodeExpiry(OffsetDateTime.now().plusMinutes(15));
        userRepository.save(user);

        log.info("Verification code reset for user: {}", user.getVerificationCode());

        UserRegistrationEvent event = new UserRegistrationEvent(
                user.getEmail(),
                user.getFirstName(),
                user.getVerificationCode()
        );

        eventPublisher.publishEvent(event);
    }

    private void ensureClientExists(String email, JobCreationDto dto) {
        if (userRepository.findByEmail(email).isEmpty()) {
            // This relies on your RegistrationService to handle the heavy lifting
            createGuestUser(dto);
        }
    }

    private Users checkUserExists(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return user;
    }

    @Transactional
    public Users createGuestUser(JobCreationDto dto) {

        Users newGuestUser = Users.builder()
                .email(dto.clientEmail())
                .phoneNumber(dto.clientPhoneNumber())
                .firstName(dto.clientFirstName())
                .lastName(dto.clientLastName())
                .userRole(role.CLIENT_GUEST)
                .build();

        userRepository.save(newGuestUser);
        return newGuestUser;
    }

}
