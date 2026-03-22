package com.trustbridge.Domain.Entities;

import com.trustbridge.Domain.Enums.DisputeState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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

    @Column(nullable = false)
    private Milestones milestone;

    @Column
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
