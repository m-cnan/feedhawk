-- FeedHawk RSS Feed Aggregator Database Schema
-- Created: September 3, 2025
-- Description: Finalized database schema for FeedHawk RSS feed reader with reel-style interface

-- ==========================================
-- 1. USERS TABLE
-- ==========================================
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    last_login TIMESTAMP NULL
);

-- ==========================================
-- 2. LISTS TABLE
-- ==========================================
-- User-created lists to organize feeds (e.g., Home, Tech, Sports)
CREATE TABLE lists (
    list_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE, -- Home list is default
    created_at TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- 3. SOURCES TABLE
-- ==========================================
-- All available RSS/feed sources that users can subscribe to
CREATE TABLE sources (
    source_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url TEXT UNIQUE NOT NULL, -- RSS feed URL
    description TEXT,
    category VARCHAR(50), -- For discovery/search grouping (Tech, Sports, News, etc.)
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ==========================================
-- 4. SUBSCRIPTIONS TABLE
-- ==========================================
-- Links sources to user lists (which sources are in which list)
CREATE TABLE subscriptions (
    sub_id SERIAL PRIMARY KEY,
    list_id INT NOT NULL REFERENCES lists(list_id) ON DELETE CASCADE,
    source_id INT NOT NULL REFERENCES sources(source_id) ON DELETE CASCADE,
    subscribed_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(list_id, source_id) -- Prevent duplicate subscriptions
);

-- ==========================================
-- 5. FEED_ITEMS TABLE
-- ==========================================
-- Actual feed entries fetched from RSS sources
CREATE TABLE feed_items (
    feed_id SERIAL PRIMARY KEY,
    source_id INT NOT NULL REFERENCES sources(source_id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    content TEXT, -- Raw HTML/text content
    summary TEXT, -- AI-generated summary
    url TEXT UNIQUE NOT NULL, -- Article URL
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(source_id, url) -- Prevent duplicate articles from same source
);

-- ==========================================
-- 6. READ_STATUS TABLE
-- ==========================================
-- Tracks which feeds each user has read/marked as read
CREATE TABLE read_status (
    status_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    feed_id INT NOT NULL REFERENCES feed_items(feed_id) ON DELETE CASCADE,
    is_read BOOLEAN DEFAULT FALSE,
    marked_read_at TIMESTAMP,
    UNIQUE(user_id, feed_id) -- One read status per user per feed
);

-- ==========================================
-- 7. USER_BOOKMARKS TABLE
-- ==========================================
-- User's saved/bookmarked feeds (replaces both bookmark and read-later)
CREATE TABLE user_bookmarks (
    bookmark_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    feed_id INT NOT NULL REFERENCES feed_items(feed_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, feed_id) -- Prevent duplicate bookmarks
);

-- ==========================================
-- INDEXES FOR PERFORMANCE
-- ==========================================

-- Users table indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Feed items indexes for fast retrieval
CREATE INDEX idx_feed_items_source ON feed_items(source_id);
CREATE INDEX idx_feed_items_published ON feed_items(published_at DESC);
CREATE INDEX idx_feed_items_source_date ON feed_items(source_id, published_at DESC);

-- Subscriptions indexes
CREATE INDEX idx_subscriptions_list ON subscriptions(list_id);
CREATE INDEX idx_subscriptions_source ON subscriptions(source_id);

-- Read status indexes
CREATE INDEX idx_read_status_user ON read_status(user_id);
CREATE INDEX idx_read_status_user_read ON read_status(user_id, is_read);

-- Bookmarks indexes
CREATE INDEX idx_bookmarks_user ON user_bookmarks(user_id);

-- ==========================================
-- INITIAL DATA - CURATED SOURCES
-- ==========================================

-- Insert curated categories for discovery
INSERT INTO sources (name, url, description, category) VALUES
-- Tech Sources
('TechCrunch', 'https://techcrunch.com/feed/', 'Latest technology news and startup information', 'Tech'),
('Ars Technica', 'http://feeds.arstechnica.com/arstechnica/index', 'In-depth technology analysis and reviews', 'Tech'),
('The Verge', 'https://www.theverge.com/rss/index.xml', 'Technology, science, art, and culture', 'Tech'),

-- News Sources  
('BBC News', 'http://feeds.bbci.co.uk/news/rss.xml', 'Latest breaking news and top stories', 'News'),
('Reuters', 'https://feeds.reuters.com/reuters/topNews', 'International breaking news and headlines', 'News'),
('Associated Press', 'https://feeds.apnews.com/rss/apf-topnews', 'Breaking news and latest headlines', 'News'),

-- Sports Sources
('ESPN', 'https://www.espn.com/espn/rss/news', 'Sports news, scores, and highlights', 'Sports'),
('Sports Illustrated', 'https://www.si.com/rss/si_topstories.rss', 'Sports news and analysis', 'Sports'),

-- Science Sources
('NASA News', 'https://www.nasa.gov/rss/dyn/breaking_news.rss', 'Latest space and astronomy news', 'Science'),
('Scientific American', 'http://rss.sciam.com/ScientificAmerican-Global', 'Science news and research', 'Science'),

-- Entertainment Sources
('Entertainment Weekly', 'https://ew.com/feed/', 'Celebrity news and entertainment updates', 'Entertainment'),
('Variety', 'https://variety.com/feed/', 'Entertainment industry news', 'Entertainment');

-- ==========================================
-- HELPER FUNCTIONS FOR APPLICATION
-- ==========================================

-- Function to create default "Home" list for new users
CREATE OR REPLACE FUNCTION create_default_home_list(new_user_id INT)
RETURNS INT AS $$
DECLARE
    new_list_id INT;
BEGIN
    INSERT INTO lists (user_id, name, is_default, created_at)
    VALUES (new_user_id, 'Home', TRUE, NOW())
    RETURNING list_id INTO new_list_id;
    
    RETURN new_list_id;
END;
$$ LANGUAGE plpgsql;

-- ==========================================
-- SAMPLE QUERIES FOR COMMON OPERATIONS
-- ==========================================

/*
-- Get all feed items for a user's Home list (main feed)
SELECT 
    fi.feed_id,
    fi.title,
    fi.content,
    fi.summary,
    fi.url,
    fi.published_at,
    s.name as source_name,
    COALESCE(rs.is_read, FALSE) as is_read,
    CASE WHEN ub.bookmark_id IS NOT NULL THEN TRUE ELSE FALSE END as is_bookmarked
FROM feed_items fi
JOIN sources s ON fi.source_id = s.source_id
JOIN subscriptions sub ON s.source_id = sub.source_id
JOIN lists l ON sub.list_id = l.list_id
LEFT JOIN read_status rs ON fi.feed_id = rs.feed_id AND rs.user_id = ?
LEFT JOIN user_bookmarks ub ON fi.feed_id = ub.feed_id AND ub.user_id = ?
WHERE l.user_id = ? AND l.is_default = TRUE
ORDER BY fi.published_at DESC
LIMIT 10;

-- Get all bookmarked feeds for a user
SELECT 
    fi.feed_id,
    fi.title,
    fi.content,
    fi.url,
    fi.published_at,
    s.name as source_name,
    ub.created_at as bookmarked_at
FROM user_bookmarks ub
JOIN feed_items fi ON ub.feed_id = fi.feed_id
JOIN sources s ON fi.source_id = s.source_id
WHERE ub.user_id = ?
ORDER BY ub.created_at DESC;

-- Get all sources in a specific list
SELECT 
    s.source_id,
    s.name,
    s.url,
    s.description,
    sub.subscribed_at
FROM sources s
JOIN subscriptions sub ON s.source_id = sub.source_id
JOIN lists l ON sub.list_id = l.list_id
WHERE l.user_id = ? AND l.list_id = ?
ORDER BY sub.subscribed_at DESC;
*/

-- ==========================================
-- END OF SCHEMA
-- ==========================================