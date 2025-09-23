package db.models;

import java.time.LocalDateTime;

public class Feed {
    private int id;
    private int userId;
    private String title;
    private String url;
    private String description;
    private String category;
    private String siteUrl;
    private LocalDateTime lastFetched;
    private LocalDateTime createdAt;
    private boolean isActive;
    private int refreshIntervalMinutes;
    private String iconUrl;

    // Constructors
    public Feed() {}

    public Feed(int userId, String title, String url, String category) {
        this.userId = userId;
        this.title = title;
        this.url = url;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
        this.refreshIntervalMinutes = 60; // Default 1 hour
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public LocalDateTime getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(LocalDateTime lastFetched) {
        this.lastFetched = lastFetched;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getRefreshIntervalMinutes() {
        return refreshIntervalMinutes;
    }

    public void setRefreshIntervalMinutes(int refreshIntervalMinutes) {
        this.refreshIntervalMinutes = refreshIntervalMinutes;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public String toString() {
        return "Feed{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", category='" + category + '\'' +
                ", lastFetched=" + lastFetched +
                ", isActive=" + isActive +
                '}';
    }
}
