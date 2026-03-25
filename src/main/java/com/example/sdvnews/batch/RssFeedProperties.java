package com.example.sdvnews.batch;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "rss")
public record RssFeedProperties(List<String> feeds) {}
