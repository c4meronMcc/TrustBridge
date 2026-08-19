package com.trustbridge.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "milestone_submission_file")
public class MilestoneSubmissionFile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private MilestoneSubmissions submission;

    @Column(name="original_filename", length = 255)
    private String originalFilename;

    @Column(name="stored_path", length = 255)
    private String storedPath;

    @Column(name="content_type", length = 255)
    private String contentType;

    @Column(name="size_bytes")
    private long sizeBytes;

    @Column(name="sha_256_hash", length = 64)
    private String sha256Hash;

}
