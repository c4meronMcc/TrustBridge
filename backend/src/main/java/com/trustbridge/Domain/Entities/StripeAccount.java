package com.trustbridge.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stripe_accounts")
public class StripeAccount extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Users user;

    @Column(name = "stripe_account_id", nullable = false, unique = true)
    private String stripeAccountId;

    @Column(name = "payouts_enabled", nullable = false)
    @Builder.Default
    private Boolean payoutsEnabled = false;
}
