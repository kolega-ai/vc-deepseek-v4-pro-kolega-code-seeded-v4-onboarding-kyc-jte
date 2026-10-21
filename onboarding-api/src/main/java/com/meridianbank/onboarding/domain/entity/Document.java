package com.meridianbank.onboarding.domain.entity;

import com.meridianbank.onboarding.domain.enums.DocCategory;
import com.meridianbank.onboarding.domain.enums.DocState;
import com.meridianbank.onboarding.domain.enums.DocType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "document")
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_type", nullable = false, length = 32)
    private DocType docType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DocCategory category;

    /** Original client-supplied filename. Metadata only; never used for storage paths. */
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    /** Server-generated storage name (UUID). */
    @Column(name = "storage_name", nullable = false, unique = true, length = 100)
    private String storageName;

    @Column(name = "content_type", nullable = false, length = 80)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_state", nullable = false, length = 16)
    private DocState verificationState;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "expiry_date")
    private Instant expiryDate;

    @Column(name = "rekyc_case_id")
    private Long reKycCaseId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
