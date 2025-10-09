import rss.RSSSearchService;
import rss.FeedSearchAPI;

public class TestSearchEngine {
    public static void main(String[] args) {
        System.out.println("Testing FeedHawk Search Engine...");
        
        // Test queries
        String[] testQueries = {
            "bbc news",
            "https://www.bbc.com",
            "tech",
            "artificial intelligence",
            "youtube"
        };
        
        for (String query : testQueries) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Testing search for: " + query);
            System.out.println("=".repeat(60));
            
            try {
                long startTime = System.currentTimeMillis();
                var results = RSSSearchService.searchFeeds(query);
                long endTime = System.currentTimeMillis();
                
                System.out.println("Found " + results.size() + " results in " + (endTime - startTime) + "ms");
                
                for (int i = 0; i < Math.min(5, results.size()); i++) {
                    var result = results.get(i);
                    System.out.println((i + 1) + ". " + result.getTitle());
                    System.out.println("   URL: " + result.getUrl());
                    System.out.println("   Type: " + result.getType());
                    System.out.println("   Description: " + result.getDescription());
                    System.out.println();
                }
                
                if (results.size() > 5) {
                    System.out.println("... and " + (results.size() - 5) + " more results");
                }
                
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Test direct FeedSearchAPI
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Testing FeedSearchAPI directly...");
        System.out.println("=".repeat(60));
        
        try {
            FeedSearchAPI api = new FeedSearchAPI();
            var apiResults = api.searchFeeds("https://www.bbc.com");
            System.out.println("FeedSearchAPI found " + apiResults.size() + " results for BBC");
            
            for (var result : apiResults) {
                System.out.println("- " + result.getTitle() + " (" + result.getUrl() + ")");
            }
            
            api.close();
            
        } catch (Exception e) {
            System.out.println("FeedSearchAPI ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\nSearch engine test completed!");
    }
}