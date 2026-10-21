package com.meridianbank.onboarding.domain.entity;

import com.meridianbank.onboarding.domain.enums.EmploymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "applicant")
@Getter
@Setter
@NoArgsConstructor
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_account_id")
    private Long userAccountId;

    @Column(name = "full_name", length = 120)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "national_identifier", length = 40)
    private String nationalIdentifier;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String mobile;

    @Column(name = "address_line1", length = 160)
    private String addressLine1;

    @Column(name = "address_line2", length = 160)
    private String addressLine2;

    @Column(length = 80)
    private String city;

    @Column(name = "postal_code", length = 12)
    private String postalCode;

    @Column(length = 60)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", length = 24)
    private EmploymentStatus employmentStatus;

    @Column(name = "employer_name", length = 160)
    private String employerName;

    /** Fixed-precision money (SGD). Never floating point. */
    @Column(name = "declared_annual_income", precision = 19, scale = 2)
    private BigDecimal declaredAnnualIncome;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
