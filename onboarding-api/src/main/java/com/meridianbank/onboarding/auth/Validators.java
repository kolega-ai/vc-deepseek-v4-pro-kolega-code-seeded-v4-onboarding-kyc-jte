package com.meridianbank.onboarding.auth;

import com.meridianbank.onboarding.service.error.ApiException;

import java.util.regex.Pattern;

/** Input validation rules for identity and credential fields. */
public final class Validators {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern MOBILE = Pattern.compile("^(\\+65[ -]?)?[689]\\d{7}$");
    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");

    private Validators() {
    }

    public static void validateEmail(String email) {
        if (email == null || !EMAIL.matcher(email).matches() || email.length() > 254) {
            throw ApiException.badRequest("A valid email address is required");
        }
    }

    public static void validateMobile(String mobile) {
        if (mobile == null || !MOBILE.matcher(mobile.trim()).matches()) {
            throw ApiException.badRequest("A valid Singapore mobile number is required (8 digits, optionally +65)");
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.length() < 10 || password.length() > 128
                || !UPPER.matcher(password).find()
                || !LOWER.matcher(password).find()
                || !DIGIT.matcher(password).find()) {
            throw ApiException.badRequest(
                    "Password must be 10-128 characters with at least one uppercase letter, one lowercase letter and one digit");
        }
    }

    public static void validateRequiredText(String field, String value, int min, int max) {
        if (value == null || value.trim().length() < min || value.trim().length() > max) {
            throw ApiException.badRequest(field + " must be between " + min + " and " + max + " characters");
        }
    }
}
