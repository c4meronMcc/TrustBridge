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
    @JoinColumn(name = "job_id", referencedColumnName = "id",nullable = false)
    private Milestones milestone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mediator_id", referencedColumnName = "id",nullable = true)
    private Users mediator;

    @Column(nullable = false)
    private BigDecimal clientProposedAmount;

    @Column(nullable = false)
    private BigDecimal freelancerProposedAmount;

    @Column(nullable = false)
    private Integer negotiationRound;

    @Column(nullable = false)
    private BigDecimal finalSettlementAmount;

    @Column(nullable = false)
    private String finalSettlementCurrency;

    @Column(nullable = false, length = 500)
    private String resolutionReason;

}
