package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.domain.entity.KycTierPolicy;
import com.meridianbank.onboarding.domain.repo.KycTierPolicyRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reference data service. The KYC tier policy response carries an MD5 ETag for conditional
 * requests — the digest is used ONLY as a cache validator for reference data, never for anything
 * security-related (non-cryptographic use, per the precision baseline).
 */
@Service
public class ReferenceDataService {

    private final KycTierPolicyRepository policyRepository;

    public ReferenceDataService(KycTierPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public List<KycTierPolicy> kycTiers() {
        return policyRepository.findAllByOrderByTierCodeAsc();
    }

    /** Canonical JSON payload used for the reference response and its ETag. */
    public String canonicalJson() {
        return "[" + kycTiers().stream()
                .map(p -> "{\"tier_code\":\"" + p.getTierCode()
                        + "\",\"display_name\":\"" + p.getDisplayName()
                        + "\",\"min_identity_match\":" + p.getMinIdentityMatch()
                        + ",\"daily_transaction_limit\":" + p.getDailyTransactionLimit()
                        + ",\"balance_ceiling\":" + p.getBalanceCeiling()
                        + ",\"currency\":\"" + p.getCurrency() + "\"}")
                .collect(Collectors.joining(",")) + "]";
    }

    /**
     * MD5 ETag for the kyc_tier policy reference response. Non-cryptographic use: this digest only
     * enables HTTP conditional requests (If-None-Match / 304) for reference data.
     */
    public String etag() {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(canonicalJson().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
