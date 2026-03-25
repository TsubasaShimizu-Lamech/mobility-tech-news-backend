package com.example.sdvnews.article;

import java.time.OffsetDateTime;
import java.util.List;

public record ArticleResponse(
        String id,
        String url,
        String title,
        String summary,
        List<String> tags,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt
) {
    static ArticleResponse from(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getUrl(),
                article.getTitle(),
                article.getSummary(),
                article.getTags(),
                article.getPublishedAt(),
                article.getCreatedAt()
        );
    }
}
