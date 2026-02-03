package com.trustbridge.Domain.Entities;

import com.trustbridge.Domain.Enums.JobStatus.jobStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "jobs")
public class Jobs extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private Users freelancer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true)
    private Users client;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "currency_code", length = 2, nullable = false)
    private String countryCode; //TODO: change to currencyCode

    // @ManyToOne(fetch = FetchType.LAZY)
    // @Column(name = "provider")
    // private PaymentProvider provider;

    // @Column(name = "provider_reference")
    // private String providerReference;

    @Column(name = "invite_token", unique = true, length = 64)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private jobStatus status = jobStatus.DRAFT;

}