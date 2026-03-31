package com.example.sdvnews.batch;

import com.example.sdvnews.article.Article;
import com.example.sdvnews.article.ArticleRepository;
import com.rometools.modules.mediarss.MediaEntryModule;
import com.rometools.modules.mediarss.types.MediaContent;
import com.rometools.modules.mediarss.types.Thumbnail;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
public class RssFetchService {

    private static final Logger log = LoggerFactory.getLogger(RssFetchService.class);

    private final RssFeedProperties rssFeedProperties;
    private final ArticleRepository articleRepository;

    public RssFetchService(RssFeedProperties rssFeedProperties, ArticleRepository articleRepository) {
        this.rssFeedProperties = rssFeedProperties;
        this.articleRepository = articleRepository;
    }

    @Transactional
    public FetchResult fetchAll() {
        int saved = 0;
        int skipped = 0;

        for (String feedUrl : rssFeedProperties.feeds()) {
            try {
                FetchResult result = fetchFeed(feedUrl);
                saved += result.saved();
                skipped += result.skipped();
            } catch (Exception e) {
                log.error("Failed to fetch RSS feed: {}", feedUrl, e);
            }
        }

        log.info("RSS fetch completed. saved={}, skipped={}", saved, skipped);
        return new FetchResult(saved, skipped);
    }

    private FetchResult fetchFeed(String feedUrl) throws Exception {
        log.info("Fetching RSS feed: {}", feedUrl);
        int saved = 0;
        int skipped = 0;

        SyndFeedInput input = new SyndFeedInput();
        try (XmlReader reader = new XmlReader(new URL(feedUrl))) {
            SyndFeed feed = input.build(reader);

            for (SyndEntry entry : feed.getEntries()) {
                String url = resolveUrl(entry);
                if (url == null) {
                    skipped++;
                    continue;
                }

                String imageUrl = resolveImageUrl(entry);
                var existing = articleRepository.findByUrl(url);
                if (existing.isPresent()) {
                    // imageUrlがnullの既存記事は更新
                    if (existing.get().getImageUrl() == null && imageUrl != null) {
                        existing.get().setImageUrl(imageUrl);
                        articleRepository.save(existing.get());
                        saved++;
                    } else {
                        skipped++;
                    }
                    continue;
                }

                Article article = new Article(
                        url,
                        entry.getTitle() != null ? entry.getTitle().trim() : "(no title)",
                        null,        // summary: フェーズ2でGeminiが設定
                        List.of(),   // tags: フェーズ2でGeminiが設定
                        imageUrl,
                        toOffsetDateTime(entry.getPublishedDate())
                );
                articleRepository.save(article);
                saved++;
            }
        }

        return new FetchResult(saved, skipped);
    }

    private String resolveImageUrl(SyndEntry entry) {
        // 1. media:thumbnail / media:content（多くの日本メディアはこれを使用）
        MediaEntryModule media = (MediaEntryModule) entry.getModule(MediaEntryModule.URI);
        if (media != null) {
            if (media.getMetadata() != null && media.getMetadata().getThumbnail() != null
                    && media.getMetadata().getThumbnail().length > 0) {
                Thumbnail thumb = media.getMetadata().getThumbnail()[0];
                if (thumb.getUrl() != null) return thumb.getUrl().toString();
            }
            if (media.getMediaContents() != null && media.getMediaContents().length > 0) {
                for (MediaContent mc : media.getMediaContents()) {
                    if (mc.getReference() != null && mc.getType() != null && mc.getType().startsWith("image")) {
                        return mc.getReference().toString();
                    }
                }
            }
        }
        // 2. enclosure（podcastや一部フィードが使用）
        if (entry.getEnclosures() != null) {
            for (SyndEnclosure enc : entry.getEnclosures()) {
                if (enc.getType() != null && enc.getType().startsWith("image")) {
                    return enc.getUrl();
                }
            }
        }
        // 3. description内の<img src="...">（多くの日本メディアはここに画像を埋め込む）
        if (entry.getDescription() != null && entry.getDescription().getValue() != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("<img[^>]+src=[\"']([^\"']+)[\"']")
                    .matcher(entry.getDescription().getValue());
            if (m.find()) return m.group(1);
        }
        return null;
    }

    private String resolveUrl(SyndEntry entry) {
        if (entry.getLink() != null && !entry.getLink().isBlank()) {
            return entry.getLink().trim();
        }
        if (entry.getUri() != null && !entry.getUri().isBlank()) {
            return entry.getUri().trim();
        }
        return null;
    }

    private OffsetDateTime toOffsetDateTime(Date date) {
        if (date == null) {
            return OffsetDateTime.now(ZoneOffset.UTC);
        }
        return date.toInstant().atOffset(ZoneOffset.UTC);
    }
}
