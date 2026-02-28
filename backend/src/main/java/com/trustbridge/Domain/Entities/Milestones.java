package com.trustbridge.Domain.Entities;


import com.trustbridge.Domain.Enums.MilestoneStatus.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "milestones")
public class Milestones extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Jobs job;

    @Column(name = "title", length = 255 ,nullable = false)
    private String title;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50 ,nullable = false)
    private milestoneStatus status =  milestoneStatus.LOCKED;


}
