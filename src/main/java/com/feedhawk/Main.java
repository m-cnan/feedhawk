package com.feedhawk;

import rss.RSSFetcher;

public class Main {
    public static void main(String[] args) {
        System.out.println("🚀 FeedHawk RSS Reader Test");
        System.out.println("===============================\n");

        // First test with local file to verify Rome library works
        System.out.println("Step 1: Testing with local RSS file...");
        String localFeed = "src/main/resources/test-feed.xml";
        RSSFetcher.fetchAndPrint(localFeed);

        System.out.println("\n" + "=".repeat(50) + "\n");

        // Then test with a reliable remote RSS feed
        System.out.println("Step 2: Testing with remote RSS feed...");
        String remoteFeed = "https://feeds.bbci.co.uk/news/world/rss.xml";
        RSSFetcher.fetchAndPrint(remoteFeed);
    }
}
