package com.example.sdvnews.bookmark;

import com.example.sdvnews.article.Article;
import com.example.sdvnews.article.ArticleRepository;
import com.example.sdvnews.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository, ArticleRepository articleRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional
    public void addBookmark(UUID userId, Long articleId) {
        if (bookmarkRepository.existsByUserIdAndArticleId(userId, articleId)) {
            return;
        }
        bookmarkRepository.save(new Bookmark(userId, articleId));
    }

    @Transactional
    public void removeBookmark(UUID userId, Long articleId) {
        bookmarkRepository.deleteByUserIdAndArticleId(userId, articleId);
    }

    public Page<ArticleResponse> getBookmarks(UUID userId, Pageable pageable) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(bookmark -> {
                    Article article = articleRepository.findById(bookmark.getArticleId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Article not found: " + bookmark.getArticleId()));
                    return ArticleResponse.from(article, true);
                });
    }
}
