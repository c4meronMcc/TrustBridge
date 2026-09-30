package com.trustbridge.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "dispute_evidence_submissions")
public class DisputeEvidenceSubmission extends BaseEntity {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "dispute_id", nullable = false)
    private Dispute dispute;

    @Column(name = "reason", nullable = false)
    private String reason;
}
