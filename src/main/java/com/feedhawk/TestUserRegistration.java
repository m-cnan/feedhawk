package com.feedhawk;

import auth.AuthController;
import auth.AuthController.AuthResult;

public class TestUserRegistration {
    public static void main(String[] args) {
        System.out.println("Testing user registration...");
        
        AuthController authController = AuthController.getInstance();
        
        // Test registration
        AuthResult result = authController.registerUser(
            "testuser", 
            "test@example.com", 
            "password123", 
            "password123"
        );
        
        System.out.println("Registration result:");
        System.out.println("Success: " + result.isSuccess());
        System.out.println("Message: " + result.getMessage());
        
        if (result.isSuccess()) {
            System.out.println("User: " + result.getUser().getUsername());
            
            // Test login
            AuthResult loginResult = authController.loginUser("testuser", "password123");
            System.out.println("\nLogin test:");
            System.out.println("Success: " + loginResult.isSuccess());
            System.out.println("Message: " + loginResult.getMessage());
        }
    }
}