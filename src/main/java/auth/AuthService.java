package auth;

import db.UserDAO;
import db.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO;
    private User currentUser; // Session management

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Register a new user
     * @param username The username
     * @param email The email address
     * @param password The plain text password
     * @return The created user or empty if registration failed
     */
    public Optional<User> register(String username, String email, String password) {
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                logger.warn("Registration failed: Username is empty");
                return Optional.empty();
            }

            if (email == null || email.trim().isEmpty()) {
                logger.warn("Registration failed: Email is empty");
                return Optional.empty();
            }

            if (!PasswordHasher.isPasswordStrong(password)) {
                logger.warn("Registration failed: Password is not strong enough");
                return Optional.empty();
            }

            // Check if username or email already exists
            if (userDAO.usernameExists(username)) {
                logger.warn("Registration failed: Username already exists: {}", username);
                return Optional.empty();
            }

            if (userDAO.emailExists(email)) {
                logger.warn("Registration failed: Email already exists: {}", email);
                return Optional.empty();
            }

            // Hash password and create user
            String hashedPassword = PasswordHasher.hashPassword(password);
            User user = new User(username.trim(), email.trim().toLowerCase(), hashedPassword);

            Optional<User> createdUser = userDAO.createUser(user);
            if (createdUser.isPresent()) {
                logger.info("User registered successfully: {}", username);
                return createdUser;
            }

        } catch (Exception e) {
            logger.error("Error during user registration", e);
        }

        return Optional.empty();
    }

    /**
     * Authenticate a user
     * @param usernameOrEmail Username or email
     * @param password Plain text password
     * @return The authenticated user or empty if authentication failed
     */
    public Optional<User> login(String usernameOrEmail, String password) {
        try {
            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() ||
                password == null || password.isEmpty()) {
                logger.warn("Login failed: Empty credentials");
                return Optional.empty();
            }

            String identifier = usernameOrEmail.trim();
            Optional<User> userOpt;

            // Try to find user by email first, then by username
            if (identifier.contains("@")) {
                userOpt = userDAO.findByEmail(identifier.toLowerCase());
            } else {
                userOpt = userDAO.findByUsername(identifier);
            }

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
                    // Update last login time
                    userDAO.updateLastLogin(user.getId());

                    // Set current user for session
                    this.currentUser = user;

                    logger.info("User logged in successfully: {}", user.getUsername());
                    return Optional.of(user);
                } else {
                    logger.warn("Login failed: Invalid password for user: {}", identifier);
                }
            } else {
                logger.warn("Login failed: User not found: {}", identifier);
            }

        } catch (Exception e) {
            logger.error("Error during user login", e);
        }

        return Optional.empty();
    }

    /**
     * Log out the current user
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("User logged out: {}", currentUser.getUsername());
            currentUser = null;
        }
    }

    /**
     * Get the currently authenticated user
     * @return The current user or null if not authenticated
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently authenticated
     * @return true if user is logged in
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Change password for the current user
     * @param currentPassword Current password
     * @param newPassword New password
     * @return true if password was changed successfully
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        if (!isAuthenticated()) {
            logger.warn("Password change failed: No user logged in");
            return false;
        }

        if (!PasswordHasher.isPasswordStrong(newPassword)) {
            logger.warn("Password change failed: New password is not strong enough");
            return false;
        }

        if (!PasswordHasher.verifyPassword(currentPassword, currentUser.getPasswordHash())) {
            logger.warn("Password change failed: Current password is incorrect");
            return false;
        }

        try {
            String newHashedPassword = PasswordHasher.hashPassword(newPassword);
            currentUser.setPasswordHash(newHashedPassword);

            if (userDAO.updateUser(currentUser)) {
                logger.info("Password changed successfully for user: {}", currentUser.getUsername());
                return true;
            }
        } catch (Exception e) {
            logger.error("Error changing password for user: {}", currentUser.getUsername(), e);
        }

        return false;
    }

    /**
     * Validate user registration data
     * @param username Username to validate
     * @param email Email to validate
     * @param password Password to validate
     * @return Error message or null if valid
     */
    public String validateRegistration(String username, String email, String password) {
        if (username == null || username.trim().length() < 3) {
            return "Username must be at least 3 characters long";
        }

        if (username.trim().length() > 50) {
            return "Username must be less than 50 characters";
        }

        if (!username.matches("^[a-zA-Z0-9_-]+$")) {
            return "Username can only contain letters, numbers, hyphens and underscores";
        }

        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Please enter a valid email address";
        }

        if (!PasswordHasher.isPasswordStrong(password)) {
            return "Password must be at least 8 characters with uppercase, lowercase, and numbers";
        }

        if (userDAO.usernameExists(username.trim())) {
            return "Username is already taken";
        }

        if (userDAO.emailExists(email.trim().toLowerCase())) {
            return "Email is already registered";
        }

        return null; // No errors
    }
}
