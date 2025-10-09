package rss;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.net.URLConnection;

/**
 * Simple standalone test for RSS fetching from the internet
 */
public class StandaloneRSSTest {
    
    public static void main(String[] args) {
        System.out.println("=== FeedHawk RSS Internet Connectivity Test ===\n");
        
        String feedUrl = "https://feeds.bbci.co.uk/news/world/rss.xml";
        
        try {
            System.out.println("Testing RSS fetching from: " + feedUrl);
            
            URL url = new URL(feedUrl);
            URLConnection connection = url.openConnection();
            
            // Set User-Agent to avoid being blocked
            connection.setRequestProperty("User-Agent", "FeedHawk RSS Reader 1.0");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(15000); // 15 seconds
            
            XmlReader reader = new XmlReader(connection.getInputStream());
            SyndFeed feed = new SyndFeedInput().build(reader);
            
            System.out.println("✅ SUCCESS: RSS feed fetched from internet!");
            System.out.println("Feed Title: " + feed.getTitle());
            System.out.println("Feed Description: " + feed.getDescription());
            System.out.println("Number of entries: " + feed.getEntries().size());
            System.out.println("\nFirst 3 articles:");
            System.out.println("-".repeat(60));
            
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
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ CONFIRMED: Your RSS interpreter CAN obtain feeds from the internet!");
            System.out.println("✅ Rome library is working properly with Maven dependencies!");
            System.out.println("✅ Network connectivity and RSS parsing are both functional!");
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: Failed to fetch RSS feed");
            System.out.println("Error: " + e.getMessage());
            System.out.println("Type: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
    }
}