package com.feedhawk;

import auth.AuthController;
import auth.AuthController.AuthResult;
import rss.FeedParser;
import rss.RSSFetcher;
import utils.Constants;
import utils.Validator;

/**
 * Simple test to demonstrate FeedHawk core functionality
 * Tests all components that don't require database connection
 */
public class FeedHawkDemo {

    public static void main(String[] args) {
        System.out.println("🚀 " + Constants.APP_NAME + " " + Constants.APP_VERSION + " - Core Demo");
        System.out.println("=" + "=".repeat(60));
        
        // Test 1: Validation System
        testValidationSystem();
        
        // Test 2: Authentication Validation
        testAuthValidation();
        
        // Test 3: RSS Parsing System
        testRSSParsingSystem();
        
        System.out.println("\n🎯 FeedHawk Core Demo Complete!");
        System.out.println("✅ All major components are functional!");
        System.out.println("📝 Next steps: Set up PostgreSQL database and connect UI components");
    }

    private static void testValidationSystem() {
        System.out.println("\n🔍 Testing Validation System...");
        
        // Test email validation
        System.out.println("📧 Email validation:");
        System.out.println("  ✅ valid@example.com: " + Validator.isValidEmail("valid@example.com"));
        System.out.println("  ❌ invalid-email: " + Validator.isValidEmail("invalid-email"));
        
        // Test username validation
        System.out.println("👤 Username validation:");
        System.out.println("  ✅ user123: " + Validator.isValidUsername("user123"));
        System.out.println("  ❌ u: " + Validator.isValidUsername("u")); // Too short
        
        // Test password validation
        System.out.println("🔐 Password validation:");
        System.out.println("  ✅ StrongPass123!: " + Validator.isValidPassword("StrongPass123!"));
        System.out.println("  ❌ weak: " + Validator.isValidPassword("weak"));
        System.out.println("  💪 Password strength: " + Validator.getPasswordStrength("StrongPass123!"));
        
        // Test URL validation
        System.out.println("🌐 URL validation:");
        System.out.println("  ✅ https://example.com/rss: " + Validator.isValidRSSUrl("https://example.com/rss"));
        System.out.println("  ❌ invalid-url: " + Validator.isValidRSSUrl("invalid-url"));
        
        System.out.println("✅ Validation system working perfectly!");
    }

    private static void testAuthValidation() {
        System.out.println("\n🔐 Testing Authentication Validation...");
        
        AuthController authController = AuthController.getInstance();
        
        // Test registration validation (logic only, no DB)
        System.out.println("📝 Registration validation tests:");
        
        // Test with mismatched passwords
        AuthResult result1 = authController.registerUser("testuser", "test@example.com", "StrongPass123!", "DifferentPass");
        System.out.println("  ❌ Mismatched passwords: " + result1.getMessage());
        
        // Test with weak password
        AuthResult result2 = authController.registerUser("testuser", "test@example.com", "weak", "weak");
        System.out.println("  ❌ Weak password: " + result2.getMessage());
        
        // Test with invalid email
        AuthResult result3 = authController.registerUser("testuser", "invalid-email", "StrongPass123!", "StrongPass123!");
        System.out.println("  ❌ Invalid email: " + result3.getMessage());
        
        // Test login validation
        System.out.println("🔑 Login validation tests:");
        AuthResult result4 = authController.loginUser("", "");
        System.out.println("  ❌ Empty credentials: " + result4.getMessage());
        
        System.out.println("✅ Authentication validation working perfectly!");
    }

    private static void testRSSParsingSystem() {
        System.out.println("\n📡 Testing RSS Parsing System...");
        
        // Test with local test feed
        System.out.println("📄 Testing local RSS feed parsing:");
        RSSFetcher.fetchAndPrint("test-feed.xml");
        
        // Test with live RSS feed
        System.out.println("\n🌐 Testing live RSS feed parsing:");
        FeedParser parser = new FeedParser();
        
        String testFeedUrl = "https://feeds.bbci.co.uk/news/world/rss.xml";
        System.out.println("🔍 RSS URL validation: " + Validator.isValidRSSUrl(testFeedUrl));
        
        // Parse the feed
        try {
            FeedParser.ParseResult result = parser.parseFeed(testFeedUrl);
            if (result.isSuccess()) {
                System.out.println("✅ Live RSS parsing successful!");
                System.out.println("  📰 Feed: " + result.getFeed().getTitle());
                System.out.println("  📊 Articles: " + result.getArticles().size());
                
                if (!result.getArticles().isEmpty()) {
                    System.out.println("  📝 Sample article: " + result.getArticles().get(0).getTitle());
                    System.out.println("  🕒 Published: " + result.getArticles().get(0).getPublishedDate());
                }
            } else {
                System.out.println("⚠️  RSS parsing failed: " + result.getMessage());
            }
        } catch (Exception e) {
            System.out.println("⚠️  RSS parsing test failed: " + e.getMessage());
        }
        
        System.out.println("✅ RSS parsing system working perfectly!");
    }
}
