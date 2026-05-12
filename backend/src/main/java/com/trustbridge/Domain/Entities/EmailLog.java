package com.trustbridge.Domain.Entities;

import com.trustbridge.Domain.Enums.EmailStatus;
import com.trustbridge.Domain.Enums.EmailTemplateType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    private EmailTemplateType templateType;

    private UUID relatedEntityId;
    private String subject;

    @Enumerated(EnumType.STRING)
    private EmailStatus status;

    private String errorMessage;
    private OffsetDateTime sentAt;
}
