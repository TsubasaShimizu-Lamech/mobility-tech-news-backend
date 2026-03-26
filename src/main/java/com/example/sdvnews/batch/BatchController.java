package com.example.sdvnews.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/batch")
public class BatchController {

    private static final Logger log = LoggerFactory.getLogger(BatchController.class);

    private final String internalBatchSecret;
    private final boolean batchSecretRequired;
    private final RssFetchService rssFetchService;

    public BatchController(
            @Value("${internal.batch-secret}") String internalBatchSecret,
            @Value("${internal.batch-secret-required}") boolean batchSecretRequired,
            RssFetchService rssFetchService
    ) {
        this.internalBatchSecret = internalBatchSecret;
        this.batchSecretRequired = batchSecretRequired;
        this.rssFetchService = rssFetchService;
    }

    @PostMapping("/fetch")
    public ResponseEntity<FetchResult> fetch(
            @RequestHeader(value = "X-Internal-Secret", required = false) String secret
    ) {
        if (!batchSecretRequired) {
            log.warn("Batch secret check is DISABLED. Do not use this in production.");
        } else if (!internalBatchSecret.equals(secret)) {
            log.warn("Unauthorized batch fetch attempt.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        FetchResult result = rssFetchService.fetchAll();
        return ResponseEntity.ok(result);
    }
}
