package com.example.sdvnews.article;

import com.example.sdvnews.bookmark.BookmarkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ArticleService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ArticleRepository articleRepository;
    private final BookmarkRepository bookmarkRepository;

    public ArticleService(ArticleRepository articleRepository, BookmarkRepository bookmarkRepository) {
        this.articleRepository = articleRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    public Page<ArticleResponse> findAll(int page, int size, UUID userId) {
        int clampedSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, clampedSize);
        Set<Long> bookmarkedIds = bookmarkRepository.findArticleIdsByUserId(userId)
                .stream().collect(Collectors.toUnmodifiableSet());
        return articleRepository.findAllByOrderByPublishedAtDesc(pageable)
                .map(article -> ArticleResponse.from(article, bookmarkedIds.contains(article.getId())));
    }
}
