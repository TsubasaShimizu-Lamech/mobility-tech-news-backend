package com.example.sdvnews.bookmark;

import com.example.sdvnews.article.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/{articleId}")
    public ResponseEntity<Void> addBookmark(
            @PathVariable Long articleId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        bookmarkService.addBookmark(userId, articleId);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> removeBookmark(
            @PathVariable Long articleId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        bookmarkService.removeBookmark(userId, articleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ArticleResponse>> getBookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        Page<ArticleResponse> bookmarks = bookmarkService.getBookmarks(
                userId, PageRequest.of(page, size));
        return ResponseEntity.ok(bookmarks);
    }
}
