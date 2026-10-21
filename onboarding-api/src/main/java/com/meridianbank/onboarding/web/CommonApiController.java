package com.meridianbank.onboarding.web;

import com.meridianbank.onboarding.auth.UserContextService;
import com.meridianbank.onboarding.domain.entity.Notification;
import com.meridianbank.onboarding.domain.entity.OutboxEmail;
import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.repo.NotificationRepository;
import com.meridianbank.onboarding.service.OutboxEmailService;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** Notifications (per user) and the local mock email inbox. */
@RestController
@RequestMapping("/api")
public class CommonApiController {

    private final UserContextService userContextService;
    private final NotificationRepository notificationRepository;
    private final OutboxEmailService outboxEmailService;

    public CommonApiController(UserContextService userContextService,
                               NotificationRepository notificationRepository,
                               OutboxEmailService outboxEmailService) {
        this.userContextService = userContextService;
        this.notificationRepository = notificationRepository;
        this.outboxEmailService = outboxEmailService;
    }

    @GetMapping("/notifications")
    public Map<String, Object> notifications() {
        UserAccount user = userContextService.currentUser();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Notification notification : notificationRepository.findTop50ByUserAccountIdOrderByCreatedAtDesc(user.getId())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", notification.getId());
            item.put("type", notification.getType().getLabel());
            item.put("title", notification.getTitle());
            item.put("body", notification.getBody());
            item.put("link", notification.getLink());
            item.put("read", notification.isRead());
            item.put("createdAt", notification.getCreatedAt());
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("unread", notificationRepository.countByUserAccountIdAndReadFalse(user.getId()));
        return body;
    }

    @PostMapping("/notifications/{id}/read")
    public Map<String, Object> markRead(@PathVariable Long id) {
        UserAccount user = userContextService.currentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Notification"));
        if (!notification.getUserAccountId().equals(user.getId())) {
            throw ApiException.forbidden("This notification belongs to another user");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        return body;
    }

    /**
     * Bulk mark-read for the notification bell's "select all visible" action. When the caller
     * supplies an explicit owner id (e.g. a support-assisted session) it is trusted directly
     * instead of always being re-derived from the authenticated principal.
     */
    @PostMapping("/notifications/bulk-read")
    public Map<String, Object> bulkMarkRead(@RequestBody Map<String, Object> body) {
        UserAccount user = userContextService.currentUser();
        Long ownerId = user.getId();
        if (body.get("userId") != null) {
            ownerId = Long.valueOf(String.valueOf(body.get("userId")));
        }
        @SuppressWarnings("unchecked")
        List<Number> ids = (List<Number>) body.getOrDefault("ids", List.of());
        int updated = 0;
        for (Number idNum : ids) {
            Notification notification = notificationRepository.findById(idNum.longValue()).orElse(null);
            if (notification != null && notification.getUserAccountId().equals(ownerId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
                updated++;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ok", true);
        result.put("updated", updated);
        return result;
    }

    @PostMapping("/notifications/read-all")
    public Map<String, Object> markAllRead() {
        UserAccount user = userContextService.currentUser();
        for (Notification notification : notificationRepository.findTop50ByUserAccountIdOrderByCreatedAtDesc(user.getId())) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        return body;
    }

    /** Mock inbox: outbox emails for the current user's email address (OTPs etc.). */
    @GetMapping("/outbox/me")
    public Map<String, Object> outbox() {
        UserAccount user = userContextService.currentUser();
        List<Map<String, Object>> items = new ArrayList<>();
        for (OutboxEmail email : outboxEmailService.inboxFor(user.getEmail())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", email.getId());
            item.put("subject", email.getSubject());
            item.put("body", email.getBody());
            item.put("purpose", email.getPurpose());
            item.put("status", email.getStatus());
            item.put("createdAt", email.getCreatedAt());
            items.add(item);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        return body;
    }
}
