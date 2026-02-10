package com.trustbridge.AuthTests;

import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.UserRole;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Auth.JpaUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JpaUserDetailsService jpaUserDetailsService;

    private Users testUser;

    @Test
    void loadUserByUsername_WithExistingEmail_ShouldReturnUserDetails() {
        // Arrange
        String email = "test@example.com";
        String password = "encodedPassword123";

        testUser = Users.builder()
                .email(email)
                .password(password)
                .firstName("John")
                .lastName("Doe")
                .userRole(UserRole.role.CLIENT)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(email);

        // Assert
        verify(userRepository).findByEmail(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo(password);
        assertThat(userDetails.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .containsExactly("ROLE_CLIENT");
    }

    @Test
    void loadUserByUsername_WithNonExistingEmail_ShouldThrowUsernameNotFoundException() {
        // Arrange
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jpaUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: " + email);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WithAdminRole_ShouldReturnUserDetailsWithAdminRole() {
        // Arrange
        String email = "admin@example.com";

        testUser = Users.builder()
                .email(email)
                .password("adminPassword")
                .firstName("Admin")
                .lastName("User")
                .userRole(UserRole.role.ADMIN)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(email);

        // Assert
        verify(userRepository).findByEmail(email);

        assertThat(userDetails.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_ShouldUseEmailAsUsername() {
        // Arrange
        String email = "user@example.com";

        testUser = Users.builder()
                .email(email)
                .password("password")
                .firstName("Test")
                .lastName("User")
                .userRole(UserRole.role.FREELANCER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(userDetails.getUsername()).isEqualTo(email);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithEncodedPassword() {
        // Arrange
        String email = "test@example.com";
        String encodedPassword = "$2a$10$EncodedPasswordHash";

        testUser = Users.builder()
                .email(email)
                .password(encodedPassword)
                .firstName("John")
                .lastName("Doe")
                .userRole(UserRole.role.CLIENT)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(userDetails.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    void loadUserByUsername_WithDifferentEmail_ShouldQueryRepositoryWithCorrectEmail() {
        // Arrange
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        Users user1 = Users.builder()
                .email(email1)
                .password("pass1")
                .userRole(UserRole.role.CLIENT)
                .build();

        when(userRepository.findByEmail(email1)).thenReturn(Optional.of(user1));

        // Act
        jpaUserDetailsService.loadUserByUsername(email1);

        // Assert
        verify(userRepository).findByEmail(email1);
        verify(userRepository, never()).findByEmail(email2);
    }

    @Test
    void loadUserByUsername_WithFreelancerRole_ShouldReturnCorrectRole() {
        // Arrange
        String email = "freelancer@example.com";

        testUser = Users.builder()
                .email(email)
                .password("freelancerPass")
                .userRole(UserRole.role.FREELANCER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_FREELANCER");
    }

    @Test
    void loadUserByUsername_WithClientGuestRole_ShouldReturnCorrectRole() {
        // Arrange
        String email = "guest@example.com";

        testUser = Users.builder()
                .email(email)
                .password("guestPass")
                .userRole(UserRole.role.CLIENT_GUEST)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CLIENT_GUEST");
    }

    @Test
    void loadUserByUsername_ShouldReturnEnabledAccount() {
        // Arrange
        String email = "enabled@example.com";

        testUser = Users.builder()
                .email(email)
                .password("password")
                .userRole(UserRole.role.CLIENT)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = jpaUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }
}