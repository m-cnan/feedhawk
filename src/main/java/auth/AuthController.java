package auth;

import db.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Controller layer for authentication operations
 * Handles UI interactions and business logic coordination
 */
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private static AuthController instance;

    private AuthController() {
        this.authService = new AuthService();
    }

    /**
     * Singleton pattern for global access
     */
    public static AuthController getInstance() {
        if (instance == null) {
            instance = new AuthController();
        }
        return instance;
    }

    /**
     * Handle user registration
     */
    public AuthResult registerUser(String username, String email, String password, String confirmPassword) {
        try {
            // Validate password confirmation
            if (!password.equals(confirmPassword)) {
                return new AuthResult(false, "Passwords do not match", null);
            }

            // Validate registration data
            String validationError = authService.validateRegistration(username, email, password);
            if (validationError != null) {
                return new AuthResult(false, validationError, null);
            }

            // Attempt registration
            Optional<User> userOpt = authService.register(username, email, password);
            if (userOpt.isPresent()) {
                return new AuthResult(true, "Registration successful! You can now log in.", userOpt.get());
            } else {
                return new AuthResult(false, "Registration failed. Please try again.", null);
            }

        } catch (Exception e) {
            logger.error("Error in user registration", e);
            return new AuthResult(false, "An error occurred during registration. Please try again.", null);
        }
    }

    /**
     * Handle user login
     */
    public AuthResult loginUser(String usernameOrEmail, String password) {
        try {
            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                return new AuthResult(false, "Please enter username or email", null);
            }

            if (password == null || password.isEmpty()) {
                return new AuthResult(false, "Please enter password", null);
            }

            Optional<User> userOpt = authService.login(usernameOrEmail, password);
            if (userOpt.isPresent()) {
                return new AuthResult(true, "Login successful!", userOpt.get());
            } else {
                return new AuthResult(false, "Invalid username/email or password", null);
            }

        } catch (Exception e) {
            logger.error("Error in user login", e);
            return new AuthResult(false, "An error occurred during login. Please try again.", null);
        }
    }

    /**
     * Handle user logout
     */
    public void logoutUser() {
        authService.logout();
    }

    /**
     * Get current authenticated user
     */
    public User getCurrentUser() {
        return authService.getCurrentUser();
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        return authService.isAuthenticated();
    }

    /**
     * Handle password change
     */
    public AuthResult changePassword(String currentPassword, String newPassword, String confirmNewPassword) {
        try {
            if (!isAuthenticated()) {
                return new AuthResult(false, "You must be logged in to change password", null);
            }

            if (!newPassword.equals(confirmNewPassword)) {
                return new AuthResult(false, "New passwords do not match", null);
            }

            if (authService.changePassword(currentPassword, newPassword)) {
                return new AuthResult(true, "Password changed successfully", getCurrentUser());
            } else {
                return new AuthResult(false, "Failed to change password. Please check your current password.", null);
            }

        } catch (Exception e) {
            logger.error("Error changing password", e);
            return new AuthResult(false, "An error occurred while changing password", null);
        }
    }

    /**
     * Result class for authentication operations
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;

        public AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }
    }
}
