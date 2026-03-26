CREATE TABLE bookmarks (
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID NOT NULL,
    article_id BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(user_id, article_id)
);
CREATE INDEX idx_bookmarks_user_id ON bookmarks(user_id);
