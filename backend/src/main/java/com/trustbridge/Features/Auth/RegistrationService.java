package com.trustbridge.Features.Auth;

import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.UserRole.*;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Auth.Dto.RegistrationDTO;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegistrationDTO dto) {

        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }


        Users newUser = Users.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .userRole(dto.role())
                .build();

        userRepository.save(newUser);
    }

    public void ensureClientExists(String email, JobCreationDto dto) {
        if (userRepository.findByEmail(email).isEmpty()) {
            // This relies on your RegistrationService to handle the heavy lifting
            createGuestUser(dto);
        }
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
