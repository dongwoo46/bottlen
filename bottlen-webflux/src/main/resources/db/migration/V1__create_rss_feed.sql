-- V1__create_rss_feed_table.sql

CREATE TABLE rss_feed (
                          id BIGSERIAL PRIMARY KEY,

    -- 식별 / 관리용
                          source VARCHAR(100) NOT NULL,
                          topic  VARCHAR(50)  NOT NULL,
                          url    VARCHAR(1000) NOT NULL,

    -- 스케줄링
                          interval_seconds INTEGER NOT NULL,
                          enabled BOOLEAN NOT NULL DEFAULT TRUE,
                          last_ingested_at TIMESTAMP NULL,

    -- 메타데이터
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 1. 스케줄러 핵심 인덱스
-- 활성 feed 중 마지막 실행 시각 기준 탐색
CREATE INDEX idx_rss_feed_enabled_last_ingested
    ON rss_feed (last_ingested_at)
    WHERE enabled = TRUE;

-- 2. source 단위 조회 / 운영용
CREATE INDEX idx_rss_feed_source
    ON rss_feed (source);

-- 3. 중복 feed 방지 (정합성 핵심)
CREATE UNIQUE INDEX idx_rss_feed_source_topic_url
    ON rss_feed (source, topic, url);
