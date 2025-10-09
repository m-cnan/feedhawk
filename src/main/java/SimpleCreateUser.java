import auth.AuthController;
import auth.AuthController.AuthResult;

public class SimpleCreateUser {
    public static void main(String[] args) {
        try {
            System.out.println("Creating test user...");
            
            AuthController authController = AuthController.getInstance();
            
            // Create a test user
            AuthResult result = authController.registerUser("testuser", "test@example.com", "password123", "password123");
            
            if (result.isSuccess()) {
                System.out.println("✅ Test user created successfully!");
                System.out.println("Username: testuser");
                System.out.println("Email: test@example.com");
                System.out.println("Password: password123");
                
                // Test login immediately
                AuthResult loginResult = authController.loginUser("testuser", "password123");
                if (loginResult.isSuccess()) {
                    System.out.println("✅ Login test successful!");
                } else {
                    System.out.println("❌ Login test failed: " + loginResult.getMessage());
                }
            } else {
                System.out.println("❌ Failed to create test user: " + result.getMessage());
            }
            
            System.exit(0); // Force exit to prevent hanging
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}