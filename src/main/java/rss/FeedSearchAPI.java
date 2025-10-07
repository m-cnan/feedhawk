package rss;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Constants;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for searching RSS feeds using the feedsearch.dev API
 * 
 * API Documentation: https://feedsearch.dev/
 * 
 * This API allows searching for RSS/Atom feeds by:
 * - Website URL (finds all feeds on a site)
 * - Search query (finds feeds matching keywords)
 */
public class FeedSearchAPI {
    private static final Logger logger = LoggerFactory.getLogger(FeedSearchAPI.class);
    
    private static final String API_BASE_URL = "https://feedsearch.dev/api/v1";
    private static final String SEARCH_ENDPOINT = API_BASE_URL + "/search";
    
    private final Gson gson;
    private final CloseableHttpClient httpClient;
    
    public FeedSearchAPI() {
        this.gson = new Gson();
        this.httpClient = HttpClients.createDefault();
    }
    
    /**
     * Feed result from feedsearch.dev API
     */
    public static class FeedResult {
        private String title;
        private String url;
        private String description;
        private String siteUrl;
        private String siteName;
        private String favicon;
        private boolean selfUrl;
        private int score;
        
        // Getters
        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getDescription() { return description; }
        public String getSiteUrl() { return siteUrl; }
        public String getSiteName() { return siteName; }
        public String getFavicon() { return favicon; }
        public boolean isSelfUrl() { return selfUrl; }
        public int getScore() { return score; }
        
        // Setters (for manual construction)
        public void setTitle(String title) { this.title = title; }
        public void setUrl(String url) { this.url = url; }
        public void setDescription(String description) { this.description = description; }
        public void setSiteUrl(String siteUrl) { this.siteUrl = siteUrl; }
        public void setSiteName(String siteName) { this.siteName = siteName; }
        public void setFavicon(String favicon) { this.favicon = favicon; }
        public void setSelfUrl(boolean selfUrl) { this.selfUrl = selfUrl; }
        public void setScore(int score) { this.score = score; }
        
        @Override
        public String toString() {
            return "FeedResult{" +
                    "title='" + title + '\'' +
                    ", url='" + url + '\'' +
                    ", siteName='" + siteName + '\'' +
                    ", score=" + score +
                    '}';
        }
    }
    
    /**
     * Search for RSS feeds by query or URL
     * 
     * @param query The search query or website URL
     * @return List of feed results
     */
    public List<FeedResult> searchFeeds(String query) {
        List<FeedResult> results = new ArrayList<>();
        
        if (query == null || query.trim().isEmpty()) {
            logger.warn("Empty search query provided");
            return results;
        }
        
        try {
            String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String apiUrl = SEARCH_ENDPOINT + "?url=" + encodedQuery;
            
            logger.info("Searching feedsearch.dev for: {}", query);
            logger.debug("API URL: {}", apiUrl);
            
            HttpGet request = new HttpGet(apiUrl);
            request.setHeader("User-Agent", Constants.USER_AGENT);
            request.setHeader("Accept", "application/json");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (statusCode == 200) {
                    results = parseSearchResults(responseBody);
                    logger.info("Found {} feeds from feedsearch.dev", results.size());
                } else {
                    logger.warn("API returned status code {}: {}", statusCode, responseBody);
                }
            }
            
        } catch (IOException e) {
            logger.error("Error calling feedsearch.dev API", e);
        } catch (Exception e) {
            logger.error("Unexpected error during feed search", e);
        }
        
        return results;
    }
    
    /**
     * Parse JSON response from feedsearch.dev API
     */
    private List<FeedResult> parseSearchResults(String jsonResponse) {
        List<FeedResult> results = new ArrayList<>();
        
        try {
            // Try parsing as array first
            try {
                JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
                
                for (JsonElement element : jsonArray) {
                    JsonObject feedObj = element.getAsJsonObject();
                    FeedResult feed = parseFeedObject(feedObj);
                    if (feed != null) {
                        results.add(feed);
                    }
                }
            } catch (com.google.gson.JsonSyntaxException e) {
                // If it's not an array, it might be an error object or empty result
                logger.debug("Response is not an array, checking if it's an error: {}", jsonResponse);
                // Return empty list for error responses
            }
            
        } catch (Exception e) {
            logger.error("Error parsing API response", e);
        }
        
        return results;
    }
    
    /**
     * Parse a single feed object from JSON
     */
    private FeedResult parseFeedObject(JsonObject json) {
        try {
            FeedResult feed = new FeedResult();
            
            // Required fields
            if (json.has("url") && !json.get("url").isJsonNull()) {
                feed.setUrl(json.get("url").getAsString());
            } else {
                return null; // URL is required
            }
            
            // Optional fields
            if (json.has("title") && !json.get("title").isJsonNull()) {
                feed.setTitle(json.get("title").getAsString());
            } else {
                feed.setTitle("Untitled Feed");
            }
            
            if (json.has("description") && !json.get("description").isJsonNull()) {
                feed.setDescription(json.get("description").getAsString());
            } else {
                feed.setDescription("");
            }
            
            if (json.has("site_url") && !json.get("site_url").isJsonNull()) {
                feed.setSiteUrl(json.get("site_url").getAsString());
            }
            
            if (json.has("site_name") && !json.get("site_name").isJsonNull()) {
                feed.setSiteName(json.get("site_name").getAsString());
            }
            
            if (json.has("favicon") && !json.get("favicon").isJsonNull()) {
                feed.setFavicon(json.get("favicon").getAsString());
            }
            
            if (json.has("self_url") && !json.get("self_url").isJsonNull()) {
                feed.setSelfUrl(json.get("self_url").getAsBoolean());
            }
            
            if (json.has("score") && !json.get("score").isJsonNull()) {
                feed.setScore(json.get("score").getAsInt());
            }
            
            return feed;
            
        } catch (Exception e) {
            logger.error("Error parsing feed object", e);
            return null;
        }
    }
    
    /**
     * Search for feeds on a specific website
     * 
     * @param websiteUrl The website URL to search
     * @return List of feeds found on the website
     */
    public List<FeedResult> searchByWebsite(String websiteUrl) {
        return searchFeeds(websiteUrl);
    }
    
    /**
     * Search for feeds by keyword/topic
     * For better results with keywords, you might want to combine this
     * with the website search by searching popular sites related to the topic
     * 
     * @param keyword The keyword/topic to search for
     * @return List of feeds matching the keyword
     */
    public List<FeedResult> searchByKeyword(String keyword) {
        // feedsearch.dev is primarily URL-based, so we'll search common
        // sites that might have feeds related to the keyword
        List<FeedResult> allResults = new ArrayList<>();
        
        // For topics like "ai", "tech", etc., we can search known sites
        List<String> sitesToSearch = getSitesForKeyword(keyword);
        
        for (String site : sitesToSearch) {
            try {
                List<FeedResult> siteResults = searchFeeds(site);
                allResults.addAll(siteResults);
                
                // Limit results to avoid overwhelming the user
                if (allResults.size() >= 20) {
                    break;
                }
            } catch (Exception e) {
                logger.debug("Error searching site {}: {}", site, e.getMessage());
            }
        }
        
        return allResults;
    }
    
    /**
     * Get popular websites related to a keyword for searching
     */
    private List<String> getSitesForKeyword(String keyword) {
        List<String> sites = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        
        // Tech-related keywords
        if (lowerKeyword.contains("ai") || lowerKeyword.contains("artificial intelligence") ||
            lowerKeyword.contains("machine learning") || lowerKeyword.contains("ml")) {
            sites.add("https://www.artificialintelligence-news.com");
            sites.add("https://machinelearningmastery.com");
            sites.add("https://www.deeplearning.ai");
            sites.add("https://ai.googleblog.com");
            sites.add("https://openai.com/blog");
        }
        
        if (lowerKeyword.contains("tech") || lowerKeyword.contains("technology")) {
            sites.add("https://techcrunch.com");
            sites.add("https://www.theverge.com");
            sites.add("https://arstechnica.com");
            sites.add("https://www.wired.com");
        }
        
        if (lowerKeyword.contains("news")) {
            sites.add("https://www.bbc.com/news");
            sites.add("https://www.reuters.com");
            sites.add("https://www.aljazeera.com");
        }
        
        if (lowerKeyword.contains("science")) {
            sites.add("https://www.sciencedaily.com");
            sites.add("https://www.scientificamerican.com");
            sites.add("https://www.nasa.gov");
        }
        
        if (lowerKeyword.contains("gaming") || lowerKeyword.contains("game")) {
            sites.add("https://www.ign.com");
            sites.add("https://www.gamespot.com");
            sites.add("https://kotaku.com");
        }
        
        if (lowerKeyword.contains("finance") || lowerKeyword.contains("money") || 
            lowerKeyword.contains("stock") || lowerKeyword.contains("crypto")) {
            sites.add("https://www.bloomberg.com");
            sites.add("https://www.coindesk.com");
            sites.add("https://finance.yahoo.com");
        }
        
        // If no specific match, add some general sites
        if (sites.isEmpty()) {
            sites.add("https://www.reddit.com/r/" + keyword);
            sites.add("https://medium.com/tag/" + keyword);
        }
        
        return sites;
    }
    
    /**
     * Close the HTTP client
     */
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            logger.error("Error closing HTTP client", e);
        }
    }
}
