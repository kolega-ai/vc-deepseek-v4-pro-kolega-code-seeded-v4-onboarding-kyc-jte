package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.OtpChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    @Query("""
            SELECT o FROM OtpChallenge o
            WHERE o.userAccountId = :userId
              AND o.purpose = :purpose
              AND o.channel = :channel
              AND o.consumedAt IS NULL
              AND o.expiresAt > :now
            ORDER BY o.issuedAt DESC
            """)
    java.util.List<OtpChallenge> findActiveByUserPurposeChannel(
            @Param("userId") Long userId,
            @Param("purpose") String purpose,
            @Param("channel") com.meridianbank.onboarding.domain.enums.OtpChannel channel,
            @Param("now") Instant now);

    @Query("""
            SELECT o FROM OtpChallenge o
            WHERE o.userAccountId = :userId
              AND o.sessionRefHash = :sessionRefHash
              AND o.purpose = :purpose
              AND o.consumedAt IS NULL
              AND o.expiresAt > :now
            ORDER BY o.issuedAt DESC
            """)
    java.util.List<OtpChallenge> findActiveForSession(
            @Param("userId") Long userId,
            @Param("sessionRefHash") String sessionRefHash,
            @Param("purpose") String purpose,
            @Param("now") Instant now);

    long countByUserAccountIdAndIssuedAtGreaterThan(Long userId, Instant since);
}
