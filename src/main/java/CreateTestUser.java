import auth.AuthController;
import auth.AuthController.AuthResult;

public class CreateTestUser {
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
            } else {
                System.out.println("❌ Failed to create test user: " + result.getMessage());
            }
            
            // Try to login with the test user
            System.out.println("\nTesting login...");
            AuthResult loginResult = authController.loginUser("testuser", "password123");
            
            if (loginResult.isSuccess()) {
                System.out.println("✅ Login test successful!");
            } else {
                System.out.println("❌ Login test failed: " + loginResult.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}