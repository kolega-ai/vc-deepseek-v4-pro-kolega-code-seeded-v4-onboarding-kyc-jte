package com.meridianbank.web.auth;

/** The signed-in channel user carried in the server-side session. */
public record WebUser(String email, String role, String displayName) {

    public boolean isRole(String candidate) {
        return candidate.equals(role);
    }

    public boolean isApplicant() {
        return "prospect".equals(role) || "customer".equals(role);
    }

    public boolean isStaff() {
        return !isApplicant();
    }
}
