package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.Document;
import com.meridianbank.onboarding.domain.enums.DocState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByApplicationIdOrderByCreatedAtAsc(Long applicationId);

    List<Document> findByApplicationIdAndVerificationState(Long applicationId, DocState state);

    Optional<Document> findByStorageName(String storageName);

    long countByApplicationId(Long applicationId);

    long countByApplicationIdAndVerificationState(Long applicationId, DocState state);

    List<Document> findByVerificationStateAndExpiryDateLessThan(DocState state, java.time.Instant now);
}
