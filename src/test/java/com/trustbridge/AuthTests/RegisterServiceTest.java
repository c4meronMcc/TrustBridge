package com.trustbridge.AuthTests;

import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.UserRole;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Auth.Dto.RegistrationDTO;
import com.trustbridge.Features.Auth.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    private RegistrationDTO validRegistrationDTO;

    @BeforeEach
    void setUp() {
        validRegistrationDTO = new RegistrationDTO(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                UserRole.role.FREELANCER
        );
    }

    @Test
    void register_WithValidData_ShouldCreateUser() {
        // Arrange
        when(userRepository.findByEmail(validRegistrationDTO.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validRegistrationDTO.password())).thenReturn("encodedPassword");

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        // Act
        registrationService.register(validRegistrationDTO);

        // Assert
        verify(userRepository).findByEmail(validRegistrationDTO.email());
        verify(passwordEncoder).encode(validRegistrationDTO.password());
        verify(userRepository).save(userCaptor.capture());

        Users savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(validRegistrationDTO.email());
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getFirstName()).isEqualTo(validRegistrationDTO.firstName());
        assertThat(savedUser.getLastName()).isEqualTo(validRegistrationDTO.lastName());
        assertThat(savedUser.getUserRole()).isEqualTo(validRegistrationDTO.role());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowRuntimeException() {
        // Arrange
        Users existingUser = Users.builder()
                .email(validRegistrationDTO.email())
                .build();

        when(userRepository.findByEmail(validRegistrationDTO.email()))
                .thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> registrationService.register(validRegistrationDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email already in use");

        verify(userRepository).findByEmail(validRegistrationDTO.email());
        verify(userRepository, never()).save(any(Users.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_ShouldEncodePasswordBeforeSaving() {
        // Arrange
        String rawPassword = "plainPassword";
        String encodedPassword = "encodedPassword123";

        RegistrationDTO dto = new RegistrationDTO(
                "newuser@example.com",
                rawPassword,
                "Jane",
                "Smith",
                UserRole.role.ADMIN
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        // Act
        registrationService.register(dto);

        // Assert
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(userCaptor.capture());

        Users savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword);
    }

    @Test
    void register_ShouldSaveAllUserFields() {
        // Arrange
        RegistrationDTO dto = new RegistrationDTO(
                "complete@example.com",
                "securePassword",
                "Alice",
                "Johnson",
                UserRole.role.CLIENT
        );

        when(userRepository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("encoded");

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        // Act
        registrationService.register(dto);

        // Assert
        verify(userRepository).save(userCaptor.capture());

        Users savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(dto.email());
        assertThat(savedUser.getFirstName()).isEqualTo(dto.firstName());
        assertThat(savedUser.getLastName()).isEqualTo(dto.lastName());
        assertThat(savedUser.getUserRole()).isEqualTo(dto.role());
    }

    @Test
    void register_WithDifferentRole_ShouldSaveCorrectRole() {
        // Arrange
        RegistrationDTO adminDto = new RegistrationDTO(
                "admin@example.com",
                "adminPass",
                "Admin",
                "User",
                UserRole.role.ADMIN
        );

        when(userRepository.findByEmail(adminDto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(adminDto.password())).thenReturn("encoded");

        ArgumentCaptor<Users> userCaptor = ArgumentCaptor.forClass(Users.class);

        // Act
        registrationService.register(adminDto);

        // Assert
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserRole()).isEqualTo(UserRole.role.ADMIN);
    }
}