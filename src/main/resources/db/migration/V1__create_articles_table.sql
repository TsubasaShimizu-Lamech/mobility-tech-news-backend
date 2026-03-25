CREATE TABLE articles (
    id           BIGSERIAL    PRIMARY KEY,
    url          TEXT         NOT NULL UNIQUE,
    title        TEXT         NOT NULL,
    summary      TEXT,
    tags         TEXT[]       NOT NULL DEFAULT '{}',
    published_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_articles_published_at ON articles (published_at DESC);
CREATE INDEX idx_articles_tags ON articles USING GIN (tags);
