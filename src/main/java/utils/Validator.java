package utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class Validator {

    // Compiled patterns for better performance
    private static final Pattern EMAIL_PATTERN_COMPILED = Pattern.compile(Constants.EMAIL_PATTERN);
    private static final Pattern USERNAME_PATTERN_COMPILED = Pattern.compile(Constants.USERNAME_PATTERN);
    private static final Pattern URL_PATTERN_COMPILED = Pattern.compile(Constants.URL_PATTERN);

    /**
     * Validate email address format
     * @param email The email to validate
     * @return true if email is valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN_COMPILED.matcher(email.trim()).matches();
    }

    /**
     * Validate username format and length
     * @param username The username to validate
     * @return true if username is valid
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String trimmed = username.trim();
        return trimmed.length() >= Constants.MIN_USERNAME_LENGTH &&
               trimmed.length() <= Constants.MAX_USERNAME_LENGTH &&
               USERNAME_PATTERN_COMPILED.matcher(trimmed).matches();
    }

    /**
     * Validate password strength
     * @param password The password to validate
     * @return true if password meets requirements
     */
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }

        if (password.length() < Constants.MIN_PASSWORD_LENGTH ||
            password.length() > Constants.MAX_PASSWORD_LENGTH) {
            return false;
        }

        // Check for at least 3 out of 4 character types
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        int typeCount = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) +
                       (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        return typeCount >= 3;
    }

    /**
     * Validate URL format
     * @param url The URL to validate
     * @return true if URL is valid
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            new URL(url.trim());
            return URL_PATTERN_COMPILED.matcher(url.trim()).matches();
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Validate RSS feed URL format
     * @param feedUrl The RSS feed URL to validate
     * @return true if appears to be a valid RSS URL
     */
    public static boolean isValidRSSUrl(String feedUrl) {
        if (!isValidUrl(feedUrl)) {
            return false;
        }

        String url = feedUrl.toLowerCase();
        return url.contains("rss") ||
               url.contains("feed") ||
               url.contains("atom") ||
               url.endsWith(".xml") ||
               url.contains("/feed/") ||
               url.contains("feeds.");
    }

    /**
     * Validate feed category
     * @param category The category to validate
     * @return true if category is valid
     */
    public static boolean isValidCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return false;
        }

        String trimmed = category.trim();
        return trimmed.length() >= 2 &&
               trimmed.length() <= 50 &&
               trimmed.matches("^[a-zA-Z0-9\\s&-]+$");
    }

    /**
     * Validate refresh interval
     * @param minutes The refresh interval in minutes
     * @return true if interval is within acceptable range
     */
    public static boolean isValidRefreshInterval(int minutes) {
        return minutes >= Constants.MIN_REFRESH_INTERVAL_MINUTES &&
               minutes <= Constants.MAX_REFRESH_INTERVAL_MINUTES;
    }

    /**
     * Sanitize text input by removing potentially harmful characters
     * @param input The input text to sanitize
     * @return Sanitized text
     */
    public static String sanitizeText(String input) {
        if (input == null) {
            return null;
        }

        return input.trim()
                   .replaceAll("[<>\"'&]", "") // Remove potentially harmful characters
                   .replaceAll("\\s+", " "); // Normalize whitespace
    }

    /**
     * Validate article title
     * @param title The article title to validate
     * @return true if title is valid
     */
    public static boolean isValidArticleTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }

        String trimmed = title.trim();
        return trimmed.length() >= 1 && trimmed.length() <= 500;
    }

    /**
     * Validate article URL
     * @param articleUrl The article URL to validate
     * @return true if URL is valid
     */
    public static boolean isValidArticleUrl(String articleUrl) {
        return isValidUrl(articleUrl);
    }

    /**
     * Check if string is null or empty
     * @param str The string to check
     * @return true if string is null or empty
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not null and not empty
     * @param str The string to check
     * @return true if string has content
     */
    public static boolean hasContent(String str) {
        return !isNullOrEmpty(str);
    }

    /**
     * Validate pagination parameters
     * @param page The page number (1-based)
     * @param pageSize The number of items per page
     * @return true if parameters are valid
     */
    public static boolean isValidPagination(int page, int pageSize) {
        return page >= 1 &&
               pageSize >= 1 &&
               pageSize <= Constants.MAX_ARTICLES_PER_PAGE;
    }

    /**
     * Get password strength description
     * @param password The password to evaluate
     * @return String describing password strength
     */
    public static String getPasswordStrength(String password) {
        if (password == null || password.length() < 4) {
            return "Very Weak";
        }

        if (password.length() < 6) {
            return "Weak";
        }

        if (!isValidPassword(password)) {
            return "Fair";
        }

        if (password.length() >= 12) {
            return "Strong";
        }

        return "Good";
    }

    private Validator() {
        // Prevent instantiation
    }
}
