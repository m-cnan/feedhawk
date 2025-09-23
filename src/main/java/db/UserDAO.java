package db;

import db.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    // SQL Queries
    private static final String INSERT_USER =
        "INSERT INTO users (username, email, password_hash, created_at) VALUES (?, ?, ?, ?) RETURNING user_id";

    private static final String FIND_BY_ID =
        "SELECT user_id, username, email, password_hash, created_at, last_login FROM users WHERE user_id = ?";

    private static final String FIND_BY_USERNAME =
        "SELECT user_id, username, email, password_hash, created_at, last_login FROM users WHERE username = ?";

    private static final String FIND_BY_EMAIL =
        "SELECT user_id, username, email, password_hash, created_at, last_login FROM users WHERE email = ?";

    private static final String UPDATE_LAST_LOGIN =
        "UPDATE users SET last_login = ? WHERE user_id = ?";

    private static final String UPDATE_USER =
        "UPDATE users SET username = ?, email = ? WHERE user_id = ?";

    private static final String DELETE_USER =
        "DELETE FROM users WHERE user_id = ?";

    public Optional<User> createUser(User user) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_USER)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt("user_id"));
                logger.info("User created successfully: {}", user.getUsername());
                return Optional.of(user);
            }

        } catch (SQLException e) {
            logger.error("Error creating user: {}", user.getUsername(), e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_ID)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding user by ID: {}", userId, e);
        }
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_USERNAME)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_EMAIL)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
        }
        return Optional.empty();
    }

    public boolean updateLastLogin(int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LAST_LOGIN)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error updating last login for user: {}", userId, e);
        }
        return false;
    }

    public boolean updateUser(User user) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User updated successfully: {}", user.getUsername());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error updating user: {}", user.getUsername(), e);
        }
        return false;
    }

    public boolean deleteUser(int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_USER)) {

            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("User deleted successfully: {}", userId);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error deleting user: {}", userId, e);
        }
        return false;
    }

    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean emailExists(String email) {
        return findByEmail(email).isPresent();
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLoginAt(lastLogin.toLocalDateTime());
        }

        user.setActive(true); // Default to active
        return user;
    }
}
