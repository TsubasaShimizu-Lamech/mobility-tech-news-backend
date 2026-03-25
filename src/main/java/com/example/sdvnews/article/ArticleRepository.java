package com.example.sdvnews.article;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, String> {

    Page<Article> findAllByOrderByPublishedAtDesc(Pageable pageable);

    Optional<Article> findByUrl(String url);

    boolean existsByUrl(String url);
}
