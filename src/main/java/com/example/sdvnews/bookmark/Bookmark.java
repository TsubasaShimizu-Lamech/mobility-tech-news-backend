package com.example.sdvnews.bookmark;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookmarks")
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Bookmark() {}

    public Bookmark(UUID userId, Long articleId) {
        this.userId = userId;
        this.articleId = articleId;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public UUID getUserId() { return userId; }
    public Long getArticleId() { return articleId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
