-- Reference data seed: KYC tier policy (SGD), document requirements and initial branches.
-- These are domain reference objects, not workflow states, so SQL seeding is appropriate.

INSERT INTO kyc_tier_policy (tier_code, display_name, min_identity_match, daily_transaction_limit, balance_ceiling, currency)
VALUES ('tier_0', 'Unverified', 0, 0, 0, 'SGD'),
       ('tier_1', 'Basic', 80, 1000.00, 5000.00, 'SGD'),
       ('tier_2', 'Standard', 80, 10000.00, 50000.00, 'SGD'),
       ('tier_3', 'Premium', 80, 100000.00, 1000000.00, 'SGD');

-- Required documents per tier (the tier table from the spec). IDENTITY_DOC is satisfied by any
-- verified identity-category document (NATIONAL_ID or PASSPORT).
INSERT INTO document_requirement (kyc_tier, doc_type, condition_type)
VALUES ('tier_1', 'IDENTITY_DOC', NULL),
       ('tier_2', 'IDENTITY_DOC', NULL),
       ('tier_2', 'ADDRESS_PROOF', NULL),
       ('tier_3', 'IDENTITY_DOC', NULL),
       ('tier_3', 'ADDRESS_PROOF', NULL),
       ('tier_3', 'PROOF_OF_INCOME', NULL),
       -- Conditional: proof of income is a prerequisite for ANY tier above tier_0 when the
       -- applicant is self-employed or declares an annual income of 100,000 SGD or above.
       ('tier_1', 'PROOF_OF_INCOME', 'SELF_EMPLOYED'),
       ('tier_1', 'PROOF_OF_INCOME', 'INCOME_GE_100K'),
       ('tier_2', 'PROOF_OF_INCOME', 'SELF_EMPLOYED'),
       ('tier_2', 'PROOF_OF_INCOME', 'INCOME_GE_100K');

INSERT INTO branch (code, name, region, created_at)
VALUES ('MB-CEN-01', 'Marina Bay Financial Centre', 'Central', CURRENT_TIMESTAMP),
       ('MB-EAS-02', 'Tampines Central', 'East', CURRENT_TIMESTAMP),
       ('MB-WES-03', 'Jurong Gateway', 'West', CURRENT_TIMESTAMP);

INSERT INTO webhook_setting (id, endpoint_url, secret, enabled, updated_at)
VALUES (1, NULL, NULL, FALSE, CURRENT_TIMESTAMP);
