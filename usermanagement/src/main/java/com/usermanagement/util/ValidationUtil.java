package com.usermanagement.util;

import com.usermanagement.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;


public class ValidationUtil {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // Phone validation pattern (10 digits)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9]{10}$"
    );

    // Password requirements: minimum 6 characters
    private static final int MIN_PASSWORD_LENGTH = 6;

    // Name requirements: minimum 2 characters, only letters and spaces
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^[A-Za-z\\s]{2,50}$"
    );

    // Gender validation
    private static final Pattern GENDER_PATTERN = Pattern.compile(
            "^(Male|Female|Other)$", Pattern.CASE_INSENSITIVE
    );

    /**
     * Validate complete user object
     */
    public static ValidationResult validateUser(User user) {
        ValidationResult result = new ValidationResult();

        if (user == null) {
            result.addError("User object cannot be null");
            return result;
        }

        // Validate name
        if (!isValidName(user.getName())) {
            result.addError("Name must be 2-50 characters and contain only letters and spaces");
        }

        // Validate email
        if (!isValidEmail(user.getEmail())) {
            result.addError("Invalid email format");
        }

        // Validate password
        if (!isValidPassword(user.getPassword())) {
            result.addError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }

        // Validate phone
        if (!isValidPhone(user.getPhone())) {
            result.addError("Phone number must be exactly 10 digits");
        }

        // Validate gender
        if (!isValidGender(user.getGender())) {
            result.addError("Gender must be Male, Female, or Other");
        }

        // Validate date of birth
        if (!isValidDateOfBirth(user.getDob())) {
            result.addError("Invalid date of birth or user must be at least 13 years old");
        }

        // Validate address
        if (!isValidAddress(user.getAddress())) {
            result.addError("Address must be between 5 and 200 characters");
        }

        return result;
    }

    /**
     * Validate name
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validate email
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate password
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /**
     * Validate phone number
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Validate gender
     */
    public static boolean isValidGender(String gender) {
        return gender != null && GENDER_PATTERN.matcher(gender.trim()).matches();
    }

    /**
     * Validate date of birth (must be at least 13 years old)
     */
    public static boolean isValidDateOfBirth(Date dob) {
        if (dob == null) return false;

        Date today = new Date();
        long diffInMillis = today.getTime() - dob.getTime();
        long age = diffInMillis / (1000L * 60 * 60 * 24 * 365);

        return age >= 13 && age <= 120;
    }

    /**
     * Validate address
     */
    public static boolean isValidAddress(String address) {
        return address != null && address.trim().length() >= 5 && address.trim().length() <= 200;
    }

    /**
     * Sanitize string input (remove HTML tags and special characters)
     */
    public static String sanitizeString(String input) {
        if (input == null) return null;

        // Remove HTML tags
        String sanitized = input.replaceAll("<[^>]*>", "");

        // Remove potentially dangerous characters
        sanitized = sanitized.replaceAll("[<>&\"']", "");

        return sanitized.trim();
    }

    /**
     * Parse date string in DD/MM/YYYY format
     */
    public static Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new ParseException("Date string cannot be null or empty", 0);
        }

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        format.setLenient(false);

        try {
            return format.parse(dateStr.trim());
        } catch (ParseException e) {
            // Try alternative format: yyyy-MM-dd
            SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd");
            altFormat.setLenient(false);
            return altFormat.parse(dateStr.trim());
        }
    }

    /**
     * Format date to DD/MM/YYYY string
     */
    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(date);
    }

    /**
     * Normalize phone number (remove non-digits)
     */
    public static String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9]", "");
    }

    /**
     * Normalize email (lowercase and trim)
     */
    public static String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }

    /**
     * Check if string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * ValidationResult class to hold validation errors
     */
    public static class ValidationResult {
        private final java.util.List<String> errors;

        public ValidationResult() {
            this.errors = new java.util.ArrayList<>();
        }

        public void addError(String error) {
            errors.add(error);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public java.util.List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }

    /**
     * Validate and sanitize user input
     */
    public static User sanitizeUser(User user) {
        if (user == null) return null;

        User sanitized = new User();
        sanitized.setId(user.getId());
        sanitized.setName(sanitizeString(user.getName()));
        sanitized.setEmail(normalizeEmail(user.getEmail()));
        sanitized.setPassword(user.getPassword()); // Don't sanitize password
        sanitized.setPhone(normalizePhone(user.getPhone()));
        sanitized.setGender(sanitizeString(user.getGender()));
        sanitized.setAddress(sanitizeString(user.getAddress()));
        sanitized.setDob(user.getDob());
        sanitized.setCreatedAt(user.getCreatedAt());
        sanitized.setUpdatedAt(user.getUpdatedAt());

        return sanitized;
    }


    public static String hashPassword(String password) {
        // In production, use BCrypt or Argon2
        // For now, this is just a placeholder
        return password; // TODO: Implement proper password hashing
    }

    /**
     * Verify password against hash
     */
    public static boolean verifyPassword(String password, String hash) {
        // In production, use BCrypt.checkpw() or similar
        return password != null && password.equals(hash);
    }
}