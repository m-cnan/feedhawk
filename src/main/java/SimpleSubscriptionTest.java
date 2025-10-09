import db.FeedDAO;

public class SimpleSubscriptionTest {
    public static void main(String[] args) {
        try {
            System.out.println("Testing Subscription Counts...");
            
            FeedDAO feedDAO = new FeedDAO();
            
            // Test subscription count for specific lists
            int homeListCount = feedDAO.getSubscriptionCount(1); // Home list
            System.out.println("Home list subscription count: " + homeListCount);
            
            int techListCount = feedDAO.getSubscriptionCount(2); // Tech News list  
            System.out.println("Tech News list subscription count: " + techListCount);
            
            System.out.println("✅ Test completed!");
            System.exit(0);
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}