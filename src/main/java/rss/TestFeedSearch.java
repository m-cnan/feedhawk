package rss;

import java.util.List;

/**
 * Simple test class to verify feedsearch.dev integration
 * Run this to test the feed search functionality
 */
public class TestFeedSearch {
    
    public static void main(String[] args) {
        System.out.println("=== FeedHawk - Testing feedsearch.dev Integration ===\n");
        
        // Test 1: Search for AI-related feeds
        System.out.println("Test 1: Searching for 'ai' feeds...");
        testSearch("ai");
        
        // Test 2: Search by website URL
        System.out.println("\nTest 2: Searching feeds on techcrunch.com...");
        testSearch("techcrunch.com");
        
        // Test 3: Search by another keyword
        System.out.println("\nTest 3: Searching for 'science' feeds...");
        testSearch("science");
        
        System.out.println("\n=== Test Complete ===");
    }
    
    private static void testSearch(String query) {
        try {
            List<RSSSearchService.SearchResult> results = RSSSearchService.searchFeeds(query);
            
            System.out.println("Found " + results.size() + " feeds:");
            
            int count = 0;
            for (RSSSearchService.SearchResult result : results) {
                count++;
                System.out.println("\n" + count + ". " + result.getTitle());
                System.out.println("   URL: " + result.getUrl());
                System.out.println("   Description: " + result.getDescription());
                System.out.println("   Type: " + result.getType() + " | Category: " + result.getCategory());
                
                // Only show first 5 results to keep output manageable
                if (count >= 5) {
                    if (results.size() > 5) {
                        System.out.println("\n... and " + (results.size() - 5) + " more results");
                    }
                    break;
                }
            }
            
            if (results.isEmpty()) {
                System.out.println("No feeds found for query: " + query);
            }
            
        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
