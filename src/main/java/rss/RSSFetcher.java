package rss;

import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndEntry;

import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;

public class RSSFetcher {

    public static void fetchAndPrint(String feedUrl) {
        try {
            System.out.println("Fetching RSS feed from: " + feedUrl);

            XmlReader reader;

            // Check if it's a resource file (no protocol and no path separators)
            if (!feedUrl.startsWith("http") && !feedUrl.startsWith("file://") && !feedUrl.contains("/")) {
                System.out.println("Reading resource file...");
                InputStream inputStream = RSSFetcher.class.getClassLoader().getResourceAsStream(feedUrl);
                if (inputStream == null) {
                    throw new RuntimeException("Resource not found: " + feedUrl);
                }
                reader = new XmlReader(inputStream);
            }
            // Check if it's a local file path
            else if (feedUrl.startsWith("file://") || new File(feedUrl).exists()) {
                System.out.println("Reading local file...");
                InputStream inputStream = new FileInputStream(feedUrl);
                reader = new XmlReader(inputStream);
            } else {
                // Handle remote URL
                URL url = new URL(feedUrl);
                URLConnection connection = url.openConnection();

                // Set User-Agent to avoid being blocked
                connection.setRequestProperty("User-Agent", "FeedHawk RSS Reader 1.0");
                connection.setConnectTimeout(5000); // 5 seconds
                connection.setReadTimeout(10000); // 10 seconds

                reader = new XmlReader(connection.getInputStream());
            }

            SyndFeed feed = new SyndFeedInput().build(reader);

            System.out.println("✅ Successfully fetched feed!");
            System.out.println("Feed Title: " + feed.getTitle());
            System.out.println("Feed Description: " + feed.getDescription());
            System.out.println("Number of entries: " + feed.getEntries().size());
            System.out.println("---------------------------------");

            int count = 0;
            for (SyndEntry entry : feed.getEntries()) {
                if (count >= 3) break; // Show only first 3 entries

                System.out.println("📰 Title: " + entry.getTitle());
                System.out.println("🔗 Link: " + entry.getLink());
                System.out.println("📅 Published: " + entry.getPublishedDate());
                if (entry.getDescription() != null) {
                    String desc = entry.getDescription().getValue();
                    if (desc.length() > 100) {
                        desc = desc.substring(0, 100) + "...";
                    }
                    System.out.println("📝 Description: " + desc);
                }
                System.out.println("---------------------------------");
                count++;
            }

            reader.close();

        } catch (Exception e) {
            System.out.println("❌ Error fetching feed: " + e.getMessage());
            System.out.println("💡 This could be due to:");
            System.out.println("   - Network connectivity issues");
            System.out.println("   - Invalid or temporarily unavailable RSS feed");
            System.out.println("   - Feed server blocking requests");
            e.printStackTrace();
        }
    }
}
