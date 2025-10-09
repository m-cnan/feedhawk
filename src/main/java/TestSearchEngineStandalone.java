import rss.FeedSearchAPI;

public class TestSearchEngineStandalone {
    public static void main(String[] args) {
        System.out.println("Testing FeedHawk Search Engine (Standalone)...");
        
        // Test queries
        String[] testQueries = {
            "https://www.bbc.com",
            "https://techcrunch.com", 
            "https://www.reddit.com",
            "artificial intelligence"
        };
        
        FeedSearchAPI api = new FeedSearchAPI();
        
        for (String query : testQueries) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Testing FeedSearchAPI for: " + query);
            System.out.println("=".repeat(60));
            
            try {
                long startTime = System.currentTimeMillis();
                var results = api.searchFeeds(query);
                long endTime = System.currentTimeMillis();
                
                System.out.println("Found " + results.size() + " results in " + (endTime - startTime) + "ms");
                
                for (int i = 0; i < Math.min(5, results.size()); i++) {
                    var result = results.get(i);
                    System.out.println((i + 1) + ". " + result.getTitle());
                    System.out.println("   URL: " + result.getUrl());
                    System.out.println("   Site: " + result.getSiteName());
                    System.out.println("   Score: " + result.getScore());
                    if (result.getDescription() != null && !result.getDescription().isEmpty()) {
                        System.out.println("   Description: " + result.getDescription());
                    }
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
        
        api.close();
        System.out.println("\nStandalone search engine test completed!");
    }
}