package com.trustbridge.Domain.Entities;

import com.trustbridge.Domain.Enums.DisputeState;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "disputes")
public class Dispute extends BaseEntity {

    @Column(nullable = false)
    private DisputeState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id", referencedColumnName = "id",nullable = false)
    private Milestones milestone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mediator_id", referencedColumnName = "id",nullable = true)
    private Users mediator;

    @Column(name = "client_proposed_amount", nullable = false)
    private BigDecimal clientProposedAmount;

    @Column(name = "freelancer_proposed_amount", nullable = false)
    private BigDecimal freelancerProposedAmount;

    @Column(name = "negotiation_round", nullable = false)
    private Integer negotiationRound;

    @Column(name = "final_settlement_amount",nullable = false)
    private BigDecimal finalSettlementAmount;

    @Column(name = "final_settlement_currency", nullable = false)
    private String finalSettlementCurrency;

    @Column(name = "resolution_reason", nullable = false, length = 500)
    private String resolutionReason;

}
