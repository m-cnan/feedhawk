import rss.RSSFetcher;
import rss.FeedParser;

public class TestRSSFetching {

    public static void main(String[] args) {
        System.out.println("Testing RSS feed fetching from internet...");

        // Test with a few popular RSS feeds
        String[] testFeeds = {
            "https://feeds.bbci.co.uk/news/rss.xml",
            "https://rss.cnn.com/rss/edition.rss", 
            "https://feeds.reuters.com/reuters/topNews"
        };

        FeedParser parser = new FeedParser();

        for (String feedUrl : testFeeds) {
            System.out.println("");
            System.out.println("==================================================");
            System.out.println("Testing feed: " + feedUrl);
            System.out.println("==================================================");

            try {
                // Test using RSSFetcher static method
                System.out.println("Method 1: Using RSSFetcher.fetchAndPrint()");
                RSSFetcher.fetchAndPrint(feedUrl);

                System.out.println("");
                System.out.println("Method 2: Using FeedParser.parseFeed()");
                FeedParser.ParseResult result = parser.parseFeed(feedUrl);

                if (result.isSuccess()) {
                    System.out.println("✅ Feed parsed successfully with FeedParser!");
                    if (result.getFeed() != null) {
                        System.out.println("Feed title: " + result.getFeed().getTitle());
                        System.out.println("Feed URL: " + result.getFeed().getUrl());
                    }
                    System.out.println("Number of articles: " + result.getArticles().size());
                    System.out.println("Message: " + result.getMessage());
                } else {
                    System.out.println("❌ FeedParser failed: " + result.getMessage());
                }

            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("");
        System.out.println("==================================================");
        System.out.println("RSS feed testing completed!");
        System.out.println("==================================================");
    }
}