package com.meridianbank.web.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Shared-tablet desk sessions: the front desk opens a workspace for the shift and the same tablet
 * reopens it from its cookie without signing in again.
 */
@Component
public class DeskSessionStore {

    private final AtomicLong deskSeq = new AtomicLong(1000);
    private final Map<String, WebUser> desks = new ConcurrentHashMap<>();

    public String open(String role, String email, String name) {
        String deskRef = "DESK-" + deskSeq.incrementAndGet();
        desks.put(deskRef, new WebUser(email, role, name));
        return deskRef;
    }

    public WebUser find(String deskRef) {
        return deskRef == null ? null : desks.get(deskRef);
    }
}
