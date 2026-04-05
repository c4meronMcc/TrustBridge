package com.trustbridge.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "stripe_customers")
public class StripeCustomer extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private Users user;

    @Column(name = "stripe_customer_id", nullable = false, unique = true)
    private String stripeCustomerId;
}