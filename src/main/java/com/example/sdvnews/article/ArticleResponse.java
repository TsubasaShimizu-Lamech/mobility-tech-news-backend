package com.example.sdvnews.article;

import java.time.OffsetDateTime;
import java.util.List;

public record ArticleResponse(
        Long id,
        String url,
        String title,
        String summary,
        List<String> tags,
        OffsetDateTime publishedAt,
        OffsetDateTime createdAt,
        boolean bookmarked
) {
    public static ArticleResponse from(Article article, boolean bookmarked) {
        return new ArticleResponse(
                article.getId(),
                article.getUrl(),
                article.getTitle(),
                article.getSummary(),
                article.getTags(),
                article.getPublishedAt(),
                article.getCreatedAt(),
                bookmarked
        );
    }
}
