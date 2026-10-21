package com.meridianbank.onboarding.service;

import com.meridianbank.onboarding.domain.entity.Application;
import com.meridianbank.onboarding.domain.entity.Notification;
import com.meridianbank.onboarding.domain.entity.StaffUser;
import com.meridianbank.onboarding.domain.enums.NotificationType;
import com.meridianbank.onboarding.domain.enums.Role;
import com.meridianbank.onboarding.domain.enums.UserStatus;
import com.meridianbank.onboarding.domain.repo.ApplicantRepository;
import com.meridianbank.onboarding.domain.repo.NotificationRepository;
import com.meridianbank.onboarding.domain.repo.StaffUserRepository;
import com.meridianbank.onboarding.domain.repo.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** In-app notifications for workflow events. Each notification is owned by exactly one user. */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StaffUserRepository staffUserRepository;
    private final ApplicantRepository applicantRepository;
    private final UserAccountRepository userAccountRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               StaffUserRepository staffUserRepository,
                               ApplicantRepository applicantRepository,
                               UserAccountRepository userAccountRepository) {
        this.notificationRepository = notificationRepository;
        this.staffUserRepository = staffUserRepository;
        this.applicantRepository = applicantRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public void notify(Long userId, NotificationType type, String title, String body, String link) {
        Notification n = new Notification();
        n.setUserAccountId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setLink(link);
        n.setRead(false);
        n.setCreatedAt(Instant.now());
        notificationRepository.save(n);
    }

    public void notifyApplicant(Application app, NotificationType type, String title, String body, String link) {
        applicantRepository.findById(app.getApplicantId())
                .filter(a -> a.getUserAccountId() != null)
                .ifPresent(a -> notify(a.getUserAccountId(), type, title, body, link));
    }

    public void notifyBranchOfficers(Application app, NotificationType type, String title, String body, String link) {
        if (app.getBranchId() == null) {
            return;
        }
        List<StaffUser> officers = staffUserRepository.findByHomeBranchIdOrderByFullNameAsc(app.getBranchId()).stream()
                .filter(s -> s.getRole() == Role.ONBOARDING_OFFICER && s.getStatus() == UserStatus.ACTIVE)
                .toList();
        for (StaffUser officer : officers) {
            staffAccountId(officer).ifPresent(id -> notify(id, type, title, body, link));
        }
    }

    public void notifyAllCompliance(NotificationType type, String title, String body, String link) {
        for (StaffUser staff : staffUserRepository.findByRoleOrderByFullNameAsc(Role.COMPLIANCE_OFFICER)) {
            staffAccountId(staff).ifPresent(id -> notify(id, type, title, body, link));
        }
    }

    public void notifyAllAdmins(NotificationType type, String title, String body, String link) {
        for (StaffUser staff : staffUserRepository.findByRoleOrderByFullNameAsc(Role.BANK_ADMIN)) {
            staffAccountId(staff).ifPresent(id -> notify(id, type, title, body, link));
        }
    }

    private Optional<Long> staffAccountId(StaffUser staff) {
        return userAccountRepository.findByStaffId(staff.getId()).map(u -> u.getId());
    }
}
