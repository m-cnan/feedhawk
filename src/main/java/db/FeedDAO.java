package db;

import db.models.Feed;
import db.models.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeedDAO {
    private static final Logger logger = LoggerFactory.getLogger(FeedDAO.class);

    // Feed/Source related queries (using your schema's 'sources' table)
    private static final String INSERT_SOURCE =
        "INSERT INTO sources (name, url, description, category, created_at) VALUES (?, ?, ?, ?, ?) RETURNING source_id";

    private static final String FIND_SOURCE_BY_ID =
        "SELECT source_id, name, url, description, category, is_active, created_at FROM sources WHERE source_id = ?";

    private static final String FIND_SOURCE_BY_URL =
        "SELECT source_id, name, url, description, category, is_active, created_at FROM sources WHERE url = ?";

    private static final String GET_USER_FEEDS =
        "SELECT s.source_id, s.name, s.url, s.description, s.category, s.is_active, s.created_at " +
        "FROM sources s " +
        "JOIN subscriptions sub ON s.source_id = sub.source_id " +
        "JOIN lists l ON sub.list_id = l.list_id " +
        "WHERE l.user_id = ? AND s.is_active = true " +
        "ORDER BY s.name";

    private static final String GET_FEEDS_BY_CATEGORY =
        "SELECT source_id, name, url, description, category, is_active, created_at " +
        "FROM sources WHERE category = ? AND is_active = true ORDER BY name";

    private static final String SUBSCRIBE_TO_FEED =
        "INSERT INTO subscriptions (list_id, source_id) VALUES (?, ?) ON CONFLICT DO NOTHING";

    private static final String UNSUBSCRIBE_FROM_FEED =
        "DELETE FROM subscriptions WHERE list_id = ? AND source_id = ?";

    // Article/Feed Items queries
    private static final String INSERT_ARTICLE =
        "INSERT INTO feed_items (source_id, title, content, summary, url, published_at, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (source_id, url) DO NOTHING RETURNING feed_id";

    private static final String GET_RECENT_ARTICLES =
        "SELECT f.feed_id, f.source_id, f.title, f.content, f.summary, f.url, f.published_at, f.created_at, " +
        "s.name as source_name FROM feed_items f " +
        "JOIN sources s ON f.source_id = s.source_id " +
        "WHERE f.source_id IN (SELECT source_id FROM subscriptions sub JOIN lists l ON sub.list_id = l.list_id WHERE l.user_id = ?) " +
        "ORDER BY f.published_at DESC LIMIT ?";

    public Optional<Feed> createSource(Feed feed) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SOURCE)) {

            stmt.setString(1, feed.getTitle());
            stmt.setString(2, feed.getUrl());
            stmt.setString(3, feed.getDescription());
            stmt.setString(4, feed.getCategory());
            stmt.setTimestamp(5, Timestamp.valueOf(feed.getCreatedAt()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                feed.setId(rs.getInt("source_id"));
                logger.info("Source created successfully: {}", feed.getTitle());
                return Optional.of(feed);
            }

        } catch (SQLException e) {
            logger.error("Error creating source: {}", feed.getTitle(), e);
        }
        return Optional.empty();
    }

    public Optional<Feed> findSourceById(int sourceId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_SOURCE_BY_ID)) {

            stmt.setInt(1, sourceId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFeed(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding source by ID: {}", sourceId, e);
        }
        return Optional.empty();
    }

    public Optional<Feed> findSourceByUrl(String url) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_SOURCE_BY_URL)) {

            stmt.setString(1, url);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToFeed(rs));
            }

        } catch (SQLException e) {
            logger.error("Error finding source by URL: {}", url, e);
        }
        return Optional.empty();
    }

    public List<Feed> getUserFeeds(int userId) {
        List<Feed> feeds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_USER_FEEDS)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                feeds.add(mapResultSetToFeed(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting user feeds for user: {}", userId, e);
        }
        return feeds;
    }

    public List<Feed> getFeedsByCategory(String category) {
        List<Feed> feeds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_FEEDS_BY_CATEGORY)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                feeds.add(mapResultSetToFeed(rs));
            }

        } catch (SQLException e) {
            logger.error("Error getting feeds by category: {}", category, e);
        }
        return feeds;
    }

    public boolean subscribeToFeed(int listId, int sourceId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SUBSCRIBE_TO_FEED)) {

            stmt.setInt(1, listId);
            stmt.setInt(2, sourceId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User subscribed to feed - List: {}, Source: {}", listId, sourceId);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error subscribing to feed - List: {}, Source: {}", listId, sourceId, e);
        }
        return false;
    }

    public boolean unsubscribeFromFeed(int listId, int sourceId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UNSUBSCRIBE_FROM_FEED)) {

            stmt.setInt(1, listId);
            stmt.setInt(2, sourceId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("User unsubscribed from feed - List: {}, Source: {}", listId, sourceId);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error unsubscribing from feed - List: {}, Source: {}", listId, sourceId, e);
        }
        return false;
    }
    
    /**
     * Subscribe a user to a feed by adding to their default list
     */
    public boolean subscribeUserToFeed(int userId, int sourceId) {
        // First, get the user's default list (Home list)
        try (Connection conn = DBConnection.getConnection()) {
            // Get user's default list ID
            String getListQuery = "SELECT list_id FROM lists WHERE user_id = ? AND list_name = 'Home' LIMIT 1";
            int listId = -1;
            
            try (PreparedStatement stmt = conn.prepareStatement(getListQuery)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    listId = rs.getInt("list_id");
                }
            }
            
            // If no Home list exists, create it
            if (listId == -1) {
                String createListQuery = "INSERT INTO lists (user_id, list_name) VALUES (?, 'Home') RETURNING list_id";
                try (PreparedStatement stmt = conn.prepareStatement(createListQuery)) {
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        listId = rs.getInt("list_id");
                        logger.info("Created Home list for user {}", userId);
                    }
                }
            }
            
            // Now subscribe to the feed
            if (listId != -1) {
                return subscribeToFeed(listId, sourceId);
            }
            
        } catch (SQLException e) {
            logger.error("Error subscribing user {} to feed {}", userId, sourceId, e);
        }
        return false;
    }

    public Optional<Article> saveArticle(Article article) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ARTICLE)) {

            stmt.setInt(1, article.getFeedId()); // This is actually source_id in our schema
            stmt.setString(2, article.getTitle());
            stmt.setString(3, article.getContent());
            stmt.setString(4, article.getDescription()); // Using description as summary for now
            stmt.setString(5, article.getUrl());
            stmt.setTimestamp(6, article.getPublishedDate() != null ?
                Timestamp.valueOf(article.getPublishedDate()) : null);
            stmt.setTimestamp(7, Timestamp.valueOf(article.getCreatedAt()));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                article.setId(rs.getInt("feed_id"));
                logger.debug("Article saved: {}", article.getTitle());
                return Optional.of(article);
            }

        } catch (SQLException e) {
            logger.error("Error saving article: {}", article.getTitle(), e);
        }
        return Optional.empty();
    }

    public List<Article> getRecentArticles(int userId, int limit) {
        List<Article> articles = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_RECENT_ARTICLES)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Article article = new Article();
                article.setId(rs.getInt("feed_id"));
                article.setFeedId(rs.getInt("source_id"));
                article.setTitle(rs.getString("title"));
                article.setContent(rs.getString("content"));
                article.setDescription(rs.getString("summary"));
                article.setUrl(rs.getString("url"));

                Timestamp publishedAt = rs.getTimestamp("published_at");
                if (publishedAt != null) {
                    article.setPublishedDate(publishedAt.toLocalDateTime());
                }

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    article.setCreatedAt(createdAt.toLocalDateTime());
                }

                articles.add(article);
            }

        } catch (SQLException e) {
            logger.error("Error getting recent articles for user: {}", userId, e);
        }
        return articles;
    }

    private Feed mapResultSetToFeed(ResultSet rs) throws SQLException {
        Feed feed = new Feed();
        feed.setId(rs.getInt("source_id"));
        feed.setTitle(rs.getString("name"));
        feed.setUrl(rs.getString("url"));
        feed.setDescription(rs.getString("description"));
        feed.setCategory(rs.getString("category"));
        feed.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            feed.setCreatedAt(createdAt.toLocalDateTime());
        }

        return feed;
    }
    
    // ==========================================
    // LIST MANAGEMENT METHODS
    // ==========================================
    
    private static final String GET_USER_LISTS = 
        "SELECT list_id, user_id, name, is_default, created_at FROM lists WHERE user_id = ? ORDER BY is_default DESC, name";
    
    private static final String CREATE_LIST = 
        "INSERT INTO lists (user_id, name, is_default, created_at) VALUES (?, ?, ?, NOW()) RETURNING list_id";
    
    private static final String GET_LIST_BY_NAME = 
        "SELECT list_id, user_id, name, is_default, created_at FROM lists WHERE user_id = ? AND name = ?";
    
    /**
     * Get all lists for a user
     */
    public List<UserList> getUserLists(int userId) {
        List<UserList> lists = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_USER_LISTS)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                UserList list = new UserList();
                list.setId(rs.getInt("list_id"));
                list.setUserId(rs.getInt("user_id"));
                list.setName(rs.getString("name"));
                list.setDefault(rs.getBoolean("is_default"));
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    list.setCreatedAt(createdAt.toLocalDateTime());
                }
                
                lists.add(list);
            }
            
        } catch (SQLException e) {
            logger.error("Error getting user lists for user: {}", userId, e);
        }
        return lists;
    }
    
    /**
     * Create a new list for a user
     */
    public Optional<UserList> createList(int userId, String listName) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(CREATE_LIST)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, listName);
            stmt.setBoolean(3, false); // Only Home is default
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UserList list = new UserList();
                list.setId(rs.getInt("list_id"));
                list.setUserId(userId);
                list.setName(listName);
                list.setDefault(false);
                list.setCreatedAt(LocalDateTime.now());
                
                logger.info("Created new list '{}' for user {}", listName, userId);
                return Optional.of(list);
            }
            
        } catch (SQLException e) {
            logger.error("Error creating list '{}' for user {}", listName, userId, e);
        }
        return Optional.empty();
    }
    
    /**
     * Subscribe a user to a feed in a specific list
     */
    public boolean subscribeToFeedInList(int listId, int sourceId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SUBSCRIBE_TO_FEED)) {

            stmt.setInt(1, listId);
            stmt.setInt(2, sourceId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Subscribed to feed - List: {}, Source: {}", listId, sourceId);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error subscribing to feed - List: {}, Source: {}", listId, sourceId, e);
        }
        return false;
    }
    
    /**
     * Simple UserList model class
     */
    public static class UserList {
        private int id;
        private int userId;
        private String name;
        private boolean isDefault;
        private LocalDateTime createdAt;
        
        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public int getUserId() { return userId; }
        public void setUserId(int userId) { this.userId = userId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public boolean isDefault() { return isDefault; }
        public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        @Override
        public String toString() {
            return (isDefault ? "🏠 " : "📁 ") + name;
        }
    }
}
