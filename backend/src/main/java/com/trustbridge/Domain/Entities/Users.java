package com.trustbridge.Domain.Entities;

import com.trustbridge.Domain.Enums.UserRole.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "users")
public class Users extends BaseEntity {

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = true)
    private String password;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private role userRole;

    @Column(name = "verification_code", nullable = true)
    private String verificationCode;

    @Column(name = "verification_code_expires_at", nullable = true)
    private OffsetDateTime verificationCodeExpiry;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified;
}