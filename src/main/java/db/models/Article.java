package db.models;

import java.time.LocalDateTime;

public class Article {
    private int id;
    private int feedId;
    private String title;
    private String url;
    private String description;
    private String content;
    private String author;
    private LocalDateTime publishedDate;
    private LocalDateTime createdAt;
    private boolean isRead;
    private boolean isSaved; // Simplified: just "Save" instead of bookmark/read-later
    private String guid; // Unique identifier from RSS feed

    // Constructors
    public Article() {}

    public Article(int feedId, String title, String url, String description, String guid) {
        this.feedId = feedId;
        this.title = title;
        this.url = url;
        this.description = description;
        this.guid = guid;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.isSaved = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    // Alias methods for bookmark functionality (using isSaved internally)
    public boolean isBookmarked() {
        return isSaved;
    }

    public void setBookmarked(boolean bookmarked) {
        this.isSaved = bookmarked;
    }

    // Alias methods for read-later functionality (using isSaved internally)
    public boolean isReadLater() {
        return isSaved;
    }

    public void setReadLater(boolean readLater) {
        this.isSaved = readLater;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", author='" + author + '\'' +
                ", publishedDate=" + publishedDate +
                ", isRead=" + isRead +
                ", isSaved=" + isSaved +
                '}';
    }
}
