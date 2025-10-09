import db.FeedDAO;
import db.models.User;
import auth.AuthController;

public class TestSubscriptionCounts {
    public static void main(String[] args) {
        try {
            System.out.println("Testing Subscription Counts Display...");
            System.out.println("=======================================");
            
            FeedDAO feedDAO = new FeedDAO();
            AuthController authController = AuthController.getInstance();
            
            // Get testuser
            User testUser = authController.getCurrentUser();
            if (testUser == null) {
                // Try to login with testuser
                var result = authController.loginUser("testuser", "password123");
                if (result.isSuccess()) {
                    testUser = authController.getCurrentUser();
                    System.out.println("✅ Logged in as: " + testUser.getUsername());
                } else {
                    System.out.println("❌ Could not login as testuser");
                    return;
                }
            }
            
            // Get user lists with subscription counts
            var userLists = feedDAO.getUserLists(testUser.getId());
            
            System.out.println("\n📂 User Lists with Subscription Counts:");
            System.out.println("---------------------------------------");
            
            for (FeedDAO.UserList list : userLists) {
                System.out.println(list.toString());
                System.out.println("   List ID: " + list.getId());
                System.out.println("   Subscription Count: " + list.getSubscriptionCount());
                System.out.println("   Is Default: " + list.isDefault());
                System.out.println();
            }
            
            System.out.println("✅ Subscription count display test completed!");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}