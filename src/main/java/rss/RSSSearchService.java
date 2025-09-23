package rss;

import utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Powerful RSS search service that can find RSS feeds from:
 * - Any website by auto-discovering RSS feeds
 * - YouTube channels and users
 * - Popular RSS feed directories
 */
public class RSSSearchService {
    private static final Logger logger = LoggerFactory.getLogger(RSSSearchService.class);
    
    public static class SearchResult {
        private final String title;
        private final String url;
        private final String description;
        private final String category;
        private final String type; // "website", "youtube", "directory"
        
        public SearchResult(String title, String url, String description, String category, String type) {
            this.title = title;
            this.url = url;
            this.description = description;
            this.category = category;
            this.type = type;
        }
        
        // Getters
        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getType() { return type; }
    }
    
    /**
     * Main search method that searches across all sources
     */
    public static List<SearchResult> searchFeeds(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        if (query == null || query.trim().isEmpty()) {
            return getPopularFeeds();
        }
        
        String cleanQuery = query.trim().toLowerCase();
        
        // 1. Check if it's a direct URL
        if (isValidUrl(cleanQuery)) {
            results.addAll(discoverFeedsFromUrl(cleanQuery));
        }
        
        // 2. Search YouTube if it looks like a channel search
        if (cleanQuery.contains("youtube") || cleanQuery.contains("yt") || cleanQuery.startsWith("@")) {
            results.addAll(searchYouTube(cleanQuery));
        }
        
        // 3. Auto-discover from website domains
        results.addAll(searchWebsiteDomains(cleanQuery));
        
        // 4. Search popular RSS directories
        results.addAll(searchRSSDirectories(cleanQuery));
        
        // Remove duplicates and limit results
        return removeDuplicates(results).subList(0, Math.min(results.size(), 50));
    }
    
    /**
     * Discover RSS feeds from a given website URL
     */
    public static List<SearchResult> discoverFeedsFromUrl(String websiteUrl) {
        List<SearchResult> results = new ArrayList<>();
        
        try {
            // Normalize URL
            if (!websiteUrl.startsWith("http://") && !websiteUrl.startsWith("https://")) {
                websiteUrl = "https://" + websiteUrl;
            }
            
            // Common RSS feed paths to try
            String[] commonPaths = {
                "/rss", "/rss.xml", "/feed", "/feed.xml", "/feeds/all.atom.xml",
                "/index.rdf", "/atom.xml", "/news/rss", "/blog/rss", "/rss/news"
            };
            
            String domain = extractDomain(websiteUrl);
            
            for (String path : commonPaths) {
                String rssUrl = websiteUrl.replaceAll("/$", "") + path;
                if (isValidRSSFeed(rssUrl)) {
                    results.add(new SearchResult(
                        domain + " RSS Feed",
                        rssUrl,
                        "RSS feed from " + domain,
                        "Website",
                        "website"
                    ));
                }
            }
            
            // Try to parse HTML and find RSS links
            results.addAll(parseHtmlForRSSLinks(websiteUrl));
            
        } catch (Exception e) {
            logger.warn("Error discovering feeds from URL: " + websiteUrl, e);
        }
        
        return results;
    }
    
    /**
     * Search YouTube channels and users
     */
    public static List<SearchResult> searchYouTube(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        try {
            // Remove @ symbol if present
            String cleanQuery = query.replace("@", "").trim();
            
            // Try as channel ID first
            if (cleanQuery.startsWith("UC") && cleanQuery.length() == 24) {
                String rssUrl = Constants.YOUTUBE_RSS_BASE + cleanQuery;
                if (isValidRSSFeed(rssUrl)) {
                    results.add(new SearchResult(
                        "YouTube Channel",
                        rssUrl,
                        "YouTube channel RSS feed",
                        "YouTube",
                        "youtube"
                    ));
                }
            }
            
            // Try as username
            String userRssUrl = Constants.YOUTUBE_USER_RSS_BASE + cleanQuery;
            if (isValidRSSFeed(userRssUrl)) {
                results.add(new SearchResult(
                    cleanQuery + " (YouTube)",
                    userRssUrl,
                    "YouTube user RSS feed",
                    "YouTube",
                    "youtube"
                ));
            }
            
            // Search for popular YouTube channels matching the query
            results.addAll(searchPopularYouTubeChannels(cleanQuery));
            
        } catch (Exception e) {
            logger.warn("Error searching YouTube: " + query, e);
        }
        
        return results;
    }
    
    /**
     * Search website domains for RSS feeds
     */
    private static List<SearchResult> searchWebsiteDomains(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        // List of popular websites that might match the query
        Map<String, String> popularSites = Map.of(
            "reddit", "https://www.reddit.com/.rss",
            "hackernews", "https://hnrss.org/frontpage",
            "medium", "https://medium.com/feed",
            "dev.to", "https://dev.to/feed",
            "github", "https://github.blog/feed/",
            "stackoverflow", "https://stackoverflow.com/feeds",
            "techcrunch", "https://techcrunch.com/feed/",
            "verge", "https://www.theverge.com/rss/index.xml"
        );
        
        for (Map.Entry<String, String> site : popularSites.entrySet()) {
            if (site.getKey().contains(query.toLowerCase()) || query.toLowerCase().contains(site.getKey())) {
                results.add(new SearchResult(
                    site.getKey() + " RSS Feed",
                    site.getValue(),
                    "Official RSS feed from " + site.getKey(),
                    "Website",
                    "website"
                ));
            }
        }
        
        return results;
    }
    
    /**
     * Search RSS directories and aggregators
     */
    private static List<SearchResult> searchRSSDirectories(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        // Add curated results based on categories
        for (String category : Constants.DISCOVERY_CATEGORIES) {
            if (category.toLowerCase().contains(query.toLowerCase())) {
                results.addAll(getFeedsForCategory(category));
            }
        }
        
        return results;
    }
    
    /**
     * Get popular feeds when no search query is provided
     */
    private static List<SearchResult> getPopularFeeds() {
        List<SearchResult> results = new ArrayList<>();
        
        // Add popular RSS feeds
        results.add(new SearchResult("BBC World News", "https://feeds.bbci.co.uk/news/world/rss.xml", "BBC World News RSS feed", "News", "website"));
        results.add(new SearchResult("Reuters", "https://www.reutersagency.com/feed/?best-regions=north-america&post_type=best", "Reuters news feed", "News", "website"));
        results.add(new SearchResult("TechCrunch", "https://techcrunch.com/feed/", "TechCrunch technology news", "Tech", "website"));
        results.add(new SearchResult("Hacker News", "https://hnrss.org/frontpage", "Hacker News front page", "Tech", "website"));
        results.add(new SearchResult("Reddit Front Page", "https://www.reddit.com/.rss", "Reddit front page RSS", "Social", "website"));
        
        return results;
    }
    
    /**
     * Parse HTML content to find RSS feed links
     */
    private static List<SearchResult> parseHtmlForRSSLinks(String url) {
        List<SearchResult> results = new ArrayList<>();
        
        try {
            String html = fetchHtml(url);
            if (html == null) return results;
            
            // Look for RSS link tags
            Pattern rssPattern = Pattern.compile(
                "<link[^>]*type=[\"']application/rss\\+xml[\"'][^>]*href=[\"']([^\"']+)[\"'][^>]*>",
                Pattern.CASE_INSENSITIVE
            );
            
            Matcher matcher = rssPattern.matcher(html);
            while (matcher.find()) {
                String rssUrl = matcher.group(1);
                if (!rssUrl.startsWith("http")) {
                    rssUrl = url + "/" + rssUrl.replaceAll("^/", "");
                }
                
                results.add(new SearchResult(
                    extractDomain(url) + " RSS",
                    rssUrl,
                    "Auto-discovered RSS feed",
                    "Website",
                    "website"
                ));
            }
            
        } catch (Exception e) {
            logger.warn("Error parsing HTML for RSS links: " + url, e);
        }
        
        return results;
    }
    
    /**
     * Get feeds for a specific category
     */
    private static List<SearchResult> getFeedsForCategory(String category) {
        List<SearchResult> results = new ArrayList<>();
        
        switch (category.toLowerCase()) {
            case "news":
                results.add(new SearchResult("CNN", "http://rss.cnn.com/rss/edition.rss", "CNN International", "News", "website"));
                results.add(new SearchResult("New York Times", "https://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml", "NYT Homepage", "News", "website"));
                break;
            case "tech":
                results.add(new SearchResult("Ars Technica", "https://feeds.arstechnica.com/arstechnica/index", "Ars Technica tech news", "Tech", "website"));
                results.add(new SearchResult("Wired", "https://www.wired.com/feed/rss", "Wired magazine", "Tech", "website"));
                break;
            // Add more categories as needed
        }
        
        return results;
    }
    
    /**
     * Search popular YouTube channels
     */
    private static List<SearchResult> searchPopularYouTubeChannels(String query) {
        List<SearchResult> results = new ArrayList<>();
        
        // Popular tech YouTube channels
        Map<String, String> popularChannels = Map.of(
            "mkbhd", "UCBJycsmduvYEL83R_U4JriQ",
            "unboxtherapy", "UCsTcErHg8oDvUnTzoqsYeNw",
            "linustechtips", "UCXuqSBlHAE6Xw-yeJA0Tunw",
            "veritasium", "UCHnyfMqiRRG1u-2MsSQLbXA"
        );
        
        for (Map.Entry<String, String> channel : popularChannels.entrySet()) {
            if (channel.getKey().contains(query.toLowerCase())) {
                String rssUrl = Constants.YOUTUBE_RSS_BASE + channel.getValue();
                results.add(new SearchResult(
                    channel.getKey() + " (YouTube)",
                    rssUrl,
                    "Popular YouTube channel",
                    "YouTube",
                    "youtube"
                ));
            }
        }
        
        return results;
    }
    
    // Utility methods
    private static boolean isValidUrl(String url) {
        return url.matches(Constants.URL_PATTERN);
    }
    
    private static boolean isValidRSSFeed(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", Constants.USER_AGENT);
            
            int responseCode = conn.getResponseCode();
            return responseCode >= 200 && responseCode < 400;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static String fetchHtml(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", Constants.USER_AGENT);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            return content.toString();
        } catch (Exception e) {
            return null;
        }
    }
    
    private static String extractDomain(String url) {
        try {
            return new URL(url).getHost().replaceAll("^www\\.", "");
        } catch (Exception e) {
            return url;
        }
    }
    
    private static List<SearchResult> removeDuplicates(List<SearchResult> results) {
        Set<String> seen = new HashSet<>();
        List<SearchResult> unique = new ArrayList<>();
        
        for (SearchResult result : results) {
            if (seen.add(result.getUrl())) {
                unique.add(result);
            }
        }
        
        return unique;
    }
}
