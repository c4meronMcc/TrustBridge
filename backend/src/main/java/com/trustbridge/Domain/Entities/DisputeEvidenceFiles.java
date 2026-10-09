package com.trustbridge.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "dispute_evidence_files")
public class DisputeEvidenceFiles extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private DisputeEvidenceSubmission submission;

    @Column(name = "original_filename", length = 255)
    private String fileName;

    @Column(name = "stored_path", length = 255)
    private String storedPath;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private long sizeBytes;

    @Column(name = "sha_256_hash", length = 64)
    private String sha256Hash;

}
