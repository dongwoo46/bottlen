-- V2__seed_rss_feed.sql
INSERT INTO rss_feed (source, topic, url, interval_seconds, enabled)
VALUES
    ('ars_technica', 'TECHNOLOGY', 'https://feeds.arstechnica.com/arstechnica/index', 600, TRUE),
    ('ars_technica', 'SCIENCE',    'https://feeds.arstechnica.com/arstechnica/science', 600, TRUE),
    ('ars_technica', 'LAW',        'https://feeds.arstechnica.com/arstechnica/tech-policy', 600, TRUE),
    ('ars_technica', 'FEATURES',   'https://feeds.arstechnica.com/arstechnica/features', 600, TRUE),
    ('ars_technica', 'CARS',       'https://feeds.arstechnica.com/arstechnica/cars', 600, TRUE),
    ('ars_technica', 'FINANCE',    'https://feeds.arstechnica.com/arstechnica/business', 600, TRUE);
