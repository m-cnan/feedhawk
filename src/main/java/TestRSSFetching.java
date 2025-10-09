import rss.RSSFetcher;import rss.RSSFetcher;

import rss.FeedParser;import rss.FeedParser;



public class TestRSSFetching {public class TestRSSFetching {

    public static void main(String[] args) {    public static void main(String[] args) {

        System.out.println("Testing RSS feed fetching from internet...");        System.out.println("Testing RSS feed fetching from internet...");

                

        // Test with popular RSS feeds        // Test with a few popular RSS feeds

        String[] testFeeds = {        String[] testFeeds = {

            "https://feeds.bbci.co.uk/news/rss.xml",            "https://feeds.bbci.co.uk/news/rss.xml",

            "https://rss.cnn.com/rss/edition.rss"            "https://rss.cnn.com/rss/edition.rss", 

        };            "https://feeds.reuters.com/reuters/topNews"

                };

        FeedParser parser = new FeedParser();        

                FeedParser parser = new FeedParser();

        for (String feedUrl : testFeeds) {        

            System.out.println("");        for (String feedUrl : testFeeds) {

            System.out.println("Testing feed: " + feedUrl);            System.out.println("");

            System.out.println("==================================================");            System.out.println("==================================================");

                        System.out.println("Testing feed: " + feedUrl);

            try {            System.out.println("==================================================");

                // Test using RSSFetcher static method            

                System.out.println("Method 1: Using RSSFetcher.fetchAndPrint()");            try {

                RSSFetcher.fetchAndPrint(feedUrl);                // Test using RSSFetcher static method

                                System.out.println("Method 1: Using RSSFetcher.fetchAndPrint()");

                System.out.println("");                RSSFetcher.fetchAndPrint(feedUrl);

                System.out.println("Method 2: Using FeedParser.parseFeed()");                

                FeedParser.ParseResult result = parser.parseFeed(feedUrl);                System.out.println("");

                                System.out.println("Method 2: Using FeedParser.parseFeed()");

                if (result.isSuccess()) {                FeedParser.ParseResult result = parser.parseFeed(feedUrl);

                    System.out.println("Feed parsed successfully with FeedParser!");                

                    if (result.getFeed() != null) {                if (result.isSuccess()) {

                        System.out.println("Feed title: " + result.getFeed().getTitle());                    System.out.println("✅ Feed parsed successfully with FeedParser!");

                        System.out.println("Feed URL: " + result.getFeed().getUrl());                    if (result.getFeed() != null) {

                    }                        System.out.println("Feed title: " + result.getFeed().getTitle());

                    System.out.println("Number of articles: " + result.getArticles().size());                        System.out.println("Feed URL: " + result.getFeed().getUrl());

                    System.out.println("Message: " + result.getMessage());                    }

                } else {                    System.out.println("Number of articles: " + result.getArticles().size());

                    System.out.println("FeedParser failed: " + result.getMessage());                    System.out.println("Message: " + result.getMessage());

                }                } else {

                                    System.out.println("❌ FeedParser failed: " + result.getMessage());

            } catch (Exception e) {                }

                System.out.println("Error: " + e.getMessage());                

                e.printStackTrace();            } catch (Exception e) {

            }                System.out.println("❌ Error: " + e.getMessage());

        }                e.printStackTrace();

                    }

        System.out.println("");        }

        System.out.println("RSS feed testing completed!");        

    }        System.out.println("");

}        System.out.println("==================================================");
        System.out.println("RSS feed testing completed!");
        System.out.println("==================================================");
    }
}
        
        }
        
        System.out.println("\n==================================================");
        System.out.println("RSS feed testing completed!");
        System.out.println("==================================================");
    }
}