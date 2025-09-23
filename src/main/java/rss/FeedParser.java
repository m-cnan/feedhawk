package rss;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import db.FeedDAO;
import db.models.Article;
import db.models.Feed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class FeedParser {
    private static final Logger logger = LoggerFactory.getLogger(FeedParser.class);
    private final FeedDAO feedDAO;

    public FeedParser() {
        this.feedDAO = new FeedDAO();
    }

    /**
     * Parse RSS feed and extract articles
     * @param feedUrl The RSS feed URL
     * @return ParseResult containing success status, feed info, and articles
     */
    public ParseResult parseFeed(String feedUrl) {
        try {
            logger.info("Parsing RSS feed: {}", feedUrl);

            // Fetch the RSS feed
            URL url = new URL(feedUrl);
            URLConnection connection = url.openConnection();

            // Set headers to avoid being blocked
            connection.setRequestProperty("User-Agent", "FeedHawk RSS Reader 1.0");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(15000); // 15 seconds

            XmlReader reader = new XmlReader(connection.getInputStream());
            SyndFeed syndFeed = new SyndFeedInput().build(reader);

            // Create Feed object from RSS metadata
            Feed feed = new Feed();
            feed.setTitle(syndFeed.getTitle());
            feed.setUrl(feedUrl);
            feed.setDescription(syndFeed.getDescription());
            feed.setSiteUrl(syndFeed.getLink());
            feed.setLastFetched(LocalDateTime.now());

            // Parse articles
            List<Article> articles = new ArrayList<>();
            for (SyndEntry entry : syndFeed.getEntries()) {
                Article article = parseEntry(entry);
                if (article != null) {
                    articles.add(article);
                }
            }

            reader.close();

            logger.info("Successfully parsed feed: {} articles from {}", articles.size(), feed.getTitle());
            return new ParseResult(true, "Feed parsed successfully", feed, articles);

        } catch (Exception e) {
            logger.error("Error parsing RSS feed: {}", feedUrl, e);
            return new ParseResult(false, "Failed to parse feed: " + e.getMessage(), null, null);
        }
    }

    /**
     * Parse and save feed articles to database
     * @param sourceId The database source ID
     * @param feedUrl The RSS feed URL
     * @return Number of new articles saved
     */
    public int parseAndSaveFeed(int sourceId, String feedUrl) {
        ParseResult result = parseFeed(feedUrl);

        if (!result.isSuccess() || result.getArticles() == null) {
            logger.warn("Failed to parse feed for saving: {}", feedUrl);
            return 0;
        }

        int savedCount = 0;
        for (Article article : result.getArticles()) {
            article.setFeedId(sourceId); // Set the source ID

            Optional<Article> saved = feedDAO.saveArticle(article);
            if (saved.isPresent()) {
                savedCount++;
            }
        }

        logger.info("Saved {} new articles from feed: {}", savedCount, feedUrl);
        return savedCount;
    }

    /**
     * Parse individual RSS entry into Article object
     */
    private Article parseEntry(SyndEntry entry) {
        try {
            Article article = new Article();

            // Basic information
            article.setTitle(cleanText(entry.getTitle()));
            article.setUrl(entry.getLink());
            article.setGuid(entry.getUri() != null ? entry.getUri() : entry.getLink());

            // Description/Content
            if (entry.getDescription() != null) {
                article.setDescription(cleanText(entry.getDescription().getValue()));
            }

            // Try to get full content if available
            if (entry.getContents() != null && !entry.getContents().isEmpty()) {
                article.setContent(cleanText(entry.getContents().get(0).getValue()));
            } else if (entry.getDescription() != null) {
                article.setContent(cleanText(entry.getDescription().getValue()));
            }

            // Author
            if (entry.getAuthor() != null) {
                article.setAuthor(cleanText(entry.getAuthor()));
            }

            // Published date
            Date publishedDate = entry.getPublishedDate();
            if (publishedDate != null) {
                article.setPublishedDate(publishedDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            } else {
                article.setPublishedDate(LocalDateTime.now());
            }

            article.setCreatedAt(LocalDateTime.now());

            return article;

        } catch (Exception e) {
            logger.error("Error parsing RSS entry: {}", entry.getTitle(), e);
            return null;
        }
    }

    /**
     * Clean and sanitize text content
     */
    private String cleanText(String text) {
        if (text == null) return null;

        return text
            .replaceAll("<[^>]*>", "") // Remove HTML tags
            .replaceAll("\\s+", " ") // Replace multiple whitespace with single space
            .trim();
    }

    /**
     * Validate if URL is a valid RSS feed
     * @param feedUrl The URL to validate
     * @return true if valid RSS feed
     */
    public boolean isValidRSSFeed(String feedUrl) {
        try {
            ParseResult result = parseFeed(feedUrl);
            return result.isSuccess();
        } catch (Exception e) {
            logger.debug("Invalid RSS feed: {}", feedUrl);
            return false;
        }
    }

    /**
     * Get feed metadata without parsing all articles
     * @param feedUrl The RSS feed URL
     * @return Feed metadata or null if failed
     */
    public Feed getFeedMetadata(String feedUrl) {
        ParseResult result = parseFeed(feedUrl);
        return result.isSuccess() ? result.getFeed() : null;
    }

    /**
     * Result class for RSS parsing operations
     */
    public static class ParseResult {
        private final boolean success;
        private final String message;
        private final Feed feed;
        private final List<Article> articles;

        public ParseResult(boolean success, String message, Feed feed, List<Article> articles) {
            this.success = success;
            this.message = message;
            this.feed = feed;
            this.articles = articles;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Feed getFeed() {
            return feed;
        }

        public List<Article> getArticles() {
            return articles;
        }
    }
}
