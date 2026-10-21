package com.meridianbank.onboarding.auth;

import com.meridianbank.onboarding.domain.entity.UserAccount;
import com.meridianbank.onboarding.domain.enums.Role;
import com.meridianbank.onboarding.domain.repo.UserAccountRepository;
import com.meridianbank.onboarding.service.error.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Resolves the acting user on every request from the authenticated JWT's subject claim, loading
 * the user, role, branch scope and approval authority from the decision tier's OWN database.
 * Client-supplied user ids, roles, branches or limits are never consulted.
 */
@Service
public class UserContextService {

    private final UserAccountRepository userAccountRepository;

    public UserContextService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    /** The authenticated user, loaded fresh from the database on each request. */
    public UserAccount currentUser() {
        Jwt jwt = jwt();
        String subject = jwt.getSubject();
        UserAccount user = userAccountRepository.findById(Long.parseLong(subject))
                .orElseThrow(() -> ApiException.unauthorized("User account no longer exists"));
        if (user.getStatus() == com.meridianbank.onboarding.domain.enums.UserStatus.DISABLED) {
            throw ApiException.unauthorized("User account is disabled");
        }
        return user;
    }

    /** Session id (sid) of the current token; OTP step-up challenges are bound to it. */
    public String currentSessionId() {
        String sid = jwt().getClaimAsString("sid");
        if (sid == null) {
            throw ApiException.unauthorized("Missing session binding");
        }
        return sid;
    }

    public Long currentUserId() {
        return currentUser().getId();
    }

    public Role currentRole() {
        return currentUser().getRole();
    }

    private Jwt jwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw ApiException.unauthorized("Not authenticated");
        }
        return jwt;
    }

    /** Current JWT if present (used by endpoints that work with the raw token). */
    public Optional<Jwt> currentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }
}
