package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.domain.entity.*;
import com.meridianbank.onboarding.domain.repo.*;
import com.meridianbank.onboarding.service.ReferenceDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** Reference data endpoints (authenticated). The tier-policy response carries an MD5 ETag. */
@RestController
@RequestMapping("/api/reference")
public class ReferenceApiController {

    private final ReferenceDataService referenceDataService;
    private final BranchRepository branchRepository;
    private final KycTierPolicyRepository policyRepository;
    private final DocumentRequirementRepository requirementRepository;

    public ReferenceApiController(ReferenceDataService referenceDataService,
                                  BranchRepository branchRepository,
                                  KycTierPolicyRepository policyRepository,
                                  DocumentRequirementRepository requirementRepository) {
        this.referenceDataService = referenceDataService;
        this.branchRepository = branchRepository;
        this.policyRepository = policyRepository;
        this.requirementRepository = requirementRepository;
    }

    @GetMapping("/kyc-tiers")
    public ResponseEntity<String> kycTiers(@RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        String etag = "\"" + referenceDataService.etag() + "\"";
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(org.springframework.http.CacheControl.noCache())
                .body(referenceDataService.canonicalJson());
    }

    @GetMapping("/branches")
    public List<Map<String, Object>> branches() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Branch branch : branchRepository.findAllByOrderByNameAsc()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", branch.getId());
            item.put("code", branch.getCode());
            item.put("name", branch.getName());
            item.put("region", branch.getRegion());
            items.add(item);
        }
        return items;
    }

    @GetMapping("/document-requirements")
    public List<Map<String, Object>> requirements() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (DocumentRequirement req : requirementRepository.findAllByOrderByKycTierAsc()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("kycTier", req.getKycTier());
            item.put("docType", req.getDocType());
            item.put("conditionType", req.getConditionType());
            items.add(item);
        }
        return items;
    }
}
