package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.domain.entity.OtpChallenge;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.repo.OtpChallengeRepository;
import com.meridianbank.onboarding.domain.repo.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Persists security counters (failed logins, OTP verify attempts) in their own transaction so
 * that the rejection they trigger does not roll the counter back with the request transaction.
 */
@Service
public class AttemptTrackerService {

    private final UserAccountRepository userAccountRepository;
    private final OtpChallengeRepository otpChallengeRepository;

    public AttemptTrackerService(UserAccountRepository userAccountRepository,
                                 OtpChallengeRepository otpChallengeRepository) {
        this.userAccountRepository = userAccountRepository;
        this.otpChallengeRepository = otpChallengeRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserAccount recordFailedLogin(Long userId, Instant lockUntil) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User vanished: " + userId));
        user.recordFailedLogin(lockUntil);
        return userAccountRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OtpChallenge recordOtpAttempt(Long challengeId) {
        OtpChallenge challenge = otpChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalStateException("Challenge vanished: " + challengeId));
        challenge.setAttemptCount(challenge.getAttemptCount() + 1);
        return otpChallengeRepository.save(challenge);
    }
}
