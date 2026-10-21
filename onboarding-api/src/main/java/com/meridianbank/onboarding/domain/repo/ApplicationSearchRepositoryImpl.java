package com.meridianbank.onboarding.domain.repo;

import com.meridianbank.onboarding.domain.entity.Application;
import com.meridianbank.onboarding.domain.entity.Applicant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Application search over bound filter parameters.
 *
 * The sort column is resolved through a bounded, whitelisted map: a caller-supplied sort key is
 * only ever translated to a hard-coded column expression from this map, so arbitrary columns or
 * SQL fragments can never reach the ORDER BY clause. All filter values are bound query parameters.
 */
public class ApplicationSearchRepositoryImpl implements ApplicationSearchRepository {

    /** Bounded whitelist: exposed sort keys -> fixed column expressions. */
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "reference", "reference",
            "created", "createdAt",
            "status", "status",
            "tier", "derivedKycTier",
            "applicant", "applicantName",
            "submitted", "submittedAt"
    );

    private static final String DEFAULT_SORT = "createdAt";

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Application> search(ApplicationSearchCriteria c) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Application> q = cb.createQuery(Application.class);
        Root<Application> root = q.from(Application.class);
        q.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (c.branchId() != null) {
            predicates.add(cb.equal(root.get("branchId"), c.branchId()));
        }
        if (c.status() != null) {
            predicates.add(cb.equal(root.get("status"), c.status()));
        }
        if (c.kycTier() != null && !c.kycTier().isBlank()) {
            predicates.add(cb.equal(root.get("derivedKycTier"), c.kycTier()));
        }
        if (c.from() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), c.from()));
        }
        if (c.to() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), c.to()));
        }
        if (c.query() != null && !c.query().isBlank()) {
            String like = "%" + c.query().trim().toLowerCase() + "%";
            Subquery<Long> applicantIds = q.subquery(Long.class);
            Root<Applicant> applicantRoot = applicantIds.from(Applicant.class);
            applicantIds.select(applicantRoot.get("id"))
                    .where(cb.or(
                            cb.like(cb.lower(applicantRoot.get("fullName")), like),
                            cb.like(cb.lower(applicantRoot.get("email")), like),
                            cb.like(cb.lower(applicantRoot.get("nationalIdentifier")), like)));
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("reference")), like),
                    root.get("applicantId").in(applicantIds)));
        }
        q.where(predicates.toArray(new Predicate[0]));

        // Bounded, whitelisted sort column; everything else is bound parameters.
        String sortKey = c.sortBy() == null ? DEFAULT_SORT : SORT_COLUMNS.getOrDefault(c.sortBy(), DEFAULT_SORT);
        Order order = c.ascending() ? cb.asc(root.get(sortKey)) : cb.desc(root.get(sortKey));
        q.orderBy(order, cb.desc(root.get("id")));

        TypedQuery<Application> query = em.createQuery(q);
        query.setFirstResult(Math.max(0, c.page()) * Math.max(1, c.size()));
        query.setMaxResults(Math.min(Math.max(1, c.size()), 200));
        return query.getResultList();
    }

    @Override
    public long count(ApplicationSearchCriteria c) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<Application> root = q.from(Application.class);
        q.select(cb.count(root));

        List<Predicate> predicates = new ArrayList<>();
        if (c.branchId() != null) {
            predicates.add(cb.equal(root.get("branchId"), c.branchId()));
        }
        if (c.status() != null) {
            predicates.add(cb.equal(root.get("status"), c.status()));
        }
        if (c.kycTier() != null && !c.kycTier().isBlank()) {
            predicates.add(cb.equal(root.get("derivedKycTier"), c.kycTier()));
        }
        if (c.from() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), c.from()));
        }
        if (c.to() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), c.to()));
        }
        if (c.query() != null && !c.query().isBlank()) {
            String like = "%" + c.query().trim().toLowerCase() + "%";
            Subquery<Long> applicantIds = q.subquery(Long.class);
            Root<Applicant> applicantRoot = applicantIds.from(Applicant.class);
            applicantIds.select(applicantRoot.get("id"))
                    .where(cb.or(
                            cb.like(cb.lower(applicantRoot.get("fullName")), like),
                            cb.like(cb.lower(applicantRoot.get("email")), like),
                            cb.like(cb.lower(applicantRoot.get("nationalIdentifier")), like)));
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("reference")), like),
                    root.get("applicantId").in(applicantIds)));
        }
        q.where(predicates.toArray(new Predicate[0]));
        return em.createQuery(q).getSingleResult();
    }
}
