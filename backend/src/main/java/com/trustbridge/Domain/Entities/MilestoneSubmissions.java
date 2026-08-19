package com.trustbridge.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "milestone_submissions")
public class MilestoneSubmissions extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestones milestone;

    @Column(name = "deliverable_link", length = 2048)
    private String deliverableLink;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "scope_items_json", columnDefinition = "TEXT")
    private List<String> scopeItemsJson; // stored as raw JSON string

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_id", nullable = false)
    private Users submittedBy;
}
