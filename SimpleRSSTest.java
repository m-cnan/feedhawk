import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndEntry;

import java.net.URL;
import java.net.URLConnection;

public class SimpleRSSTest {
    public static void main(String[] args) {
        System.out.println("Testing RSS feed fetching from the internet...\n");
        
        // Test with BBC News RSS feed
        String feedUrl = "https://feeds.bbci.co.uk/news/world/rss.xml";
        
        try {
            System.out.println("Fetching RSS feed from: " + feedUrl);
            
            URL url = new URL(feedUrl);
            URLConnection connection = url.openConnection();
            
            // Set User-Agent to avoid being blocked
            connection.setRequestProperty("User-Agent", "FeedHawk RSS Reader 1.0");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(15000); // 15 seconds
            
            XmlReader reader = new XmlReader(connection.getInputStream());
            SyndFeed feed = new SyndFeedInput().build(reader);
            
            System.out.println("✅ Successfully fetched RSS feed!");
            System.out.println("Feed Title: " + feed.getTitle());
            System.out.println("Feed Description: " + feed.getDescription());
            System.out.println("Number of entries: " + feed.getEntries().size());
            System.out.println("\nFirst 3 articles:");
            System.out.println("-".repeat(50));
            
            int count = 0;
            for (SyndEntry entry : feed.getEntries()) {
                if (count >= 3) break;
                
                System.out.println("\n" + (count + 1) + ". " + entry.getTitle());
                System.out.println("   Published: " + entry.getPublishedDate());
                System.out.println("   Link: " + entry.getLink());
                
                if (entry.getDescription() != null) {
                    String desc = entry.getDescription().getValue();
                    if (desc.length() > 150) {
                        desc = desc.substring(0, 150) + "...";
                    }
                    // Remove HTML tags for cleaner output
                    desc = desc.replaceAll("<[^>]*>", "");
                    System.out.println("   Description: " + desc);
                }
                
                count++;
            }
            
            reader.close();
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("✅ RSS fetching from internet is WORKING!");
            System.out.println("✅ Your RSS interpreter successfully obtained feeds from the internet!");
            
        } catch (Exception e) {
            System.out.println("❌ Error fetching RSS feed: " + e.getMessage());
            System.out.println("\nPossible causes:");
            System.out.println("- No internet connection");
            System.out.println("- RSS feed URL is temporarily unavailable");
            System.out.println("- Network firewall blocking the request");
            System.out.println("- Missing Rome library dependencies");
            e.printStackTrace();
        }
    }
}