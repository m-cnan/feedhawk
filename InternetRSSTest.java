import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class InternetRSSTest {
    public static void main(String[] args) {
        System.out.println("Testing RSS feed fetching capability from the internet...\n");
        
        // Test with BBC News RSS feed
        String feedUrl = "https://feeds.bbci.co.uk/news/world/rss.xml";
        
        try {
            System.out.println("Attempting to fetch RSS feed from: " + feedUrl);
            
            URL url = new URL(feedUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set request properties
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "FeedHawk RSS Reader 1.0");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(15000); // 15 seconds
            
            int responseCode = connection.getResponseCode();
            System.out.println("HTTP Response Code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder content = new StringBuilder();
                int lineCount = 0;
                
                System.out.println("\n✅ Successfully connected to RSS feed!");
                System.out.println("Content-Type: " + connection.getContentType());
                System.out.println("Content-Length: " + connection.getContentLength());
                System.out.println("\nFirst 10 lines of RSS content:");
                System.out.println("-".repeat(60));
                
                while ((line = reader.readLine()) != null && lineCount < 10) {
                    System.out.println(line);
                    content.append(line).append("\n");
                    lineCount++;
                }
                
                reader.close();
                
                // Check if content looks like RSS/XML
                String contentStr = content.toString().toLowerCase();
                boolean isRSS = contentStr.contains("<rss") || contentStr.contains("<feed") || 
                               contentStr.contains("<channel") || contentStr.contains("<?xml");
                
                System.out.println("-".repeat(60));
                if (isRSS) {
                    System.out.println("✅ Content appears to be valid RSS/XML format!");
                    System.out.println("✅ Your RSS interpreter CAN obtain feeds from the internet!");
                    System.out.println("✅ Internet connectivity and RSS fetching capability confirmed!");
                } else {
                    System.out.println("⚠️  Content doesn't appear to be RSS/XML format");
                }
                
            } else {
                System.out.println("❌ HTTP request failed with response code: " + responseCode);
                System.out.println("❌ Unable to fetch RSS feed from the internet");
            }
            
        } catch (java.net.UnknownHostException e) {
            System.out.println("❌ Network error: Unable to resolve host");
            System.out.println("❌ Check your internet connection");
        } catch (java.net.ConnectException e) {
            System.out.println("❌ Connection error: Unable to connect to RSS server");
            System.out.println("❌ The RSS server might be down or blocking requests");
        } catch (java.net.SocketTimeoutException e) {
            System.out.println("❌ Timeout error: Request took too long");
            System.out.println("❌ The RSS server is responding too slowly");
        } catch (Exception e) {
            System.out.println("❌ Error fetching RSS feed: " + e.getMessage());
            System.out.println("❌ Type: " + e.getClass().getSimpleName());
            e.printStackTrace();
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Test completed. Check results above.");
    }
}