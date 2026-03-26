package com.example.sdvnews.bookmark;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    boolean existsByUserIdAndArticleId(UUID userId, Long articleId);

    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.userId = :userId AND b.articleId = :articleId")
    void deleteByUserIdAndArticleId(@Param("userId") UUID userId, @Param("articleId") Long articleId);

    @Query("SELECT b.articleId FROM Bookmark b WHERE b.userId = :userId")
    List<Long> findArticleIdsByUserId(@Param("userId") UUID userId);
}
