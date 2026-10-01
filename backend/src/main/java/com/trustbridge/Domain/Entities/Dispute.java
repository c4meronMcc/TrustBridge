package com.trustbridge.Domain.Entities;

import com.trustbridge.Domain.Enums.DisputeState;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "disputes")
public class Dispute extends BaseEntity {

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeState state = DisputeState.AWAITING_DISPUTE_DECISION;

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
    private Integer negotiationRound = 0;

    @Column(name = "final_settlement_amount")
    private BigDecimal finalSettlementAmount;

    @Column(name = "final_settlement_currency", nullable = false)
    private String finalSettlementCurrency;

    @Column(name = "resolution_reason", length = 500)
    private String resolutionReason;

}
