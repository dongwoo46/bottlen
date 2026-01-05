-- V1__create_rss_feed_table.sql
CREATE TABLE rss_feed (
                          id BIGSERIAL PRIMARY KEY,
                          source TEXT NOT NULL,
                          topic TEXT NOT NULL,
                          url TEXT NOT NULL,
                          interval_seconds INTEGER NOT NULL,
                          enabled BOOLEAN NOT NULL DEFAULT TRUE,
                          last_ingested_at TIMESTAMP NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
