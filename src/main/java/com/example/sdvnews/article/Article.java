package com.example.sdvnews.article;

import jakarta.persistence.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "url", nullable = false, unique = true, length = 2048)
    private String url;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Array(length = 50)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Article() {}

    public Article(String url, String title, String summary, List<String> tags, OffsetDateTime publishedAt) {
        this.url = url;
        this.title = title;
        this.summary = summary;
        this.tags = tags;
        this.publishedAt = publishedAt;
        this.createdAt = OffsetDateTime.now();
    }

    public String getId() { return id; }
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public List<String> getTags() { return tags; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
