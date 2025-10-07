import time
import requests
import feedparser
from logger_config import logger


def parse_feed(topic: str, url: str):
    """RSS 피드 파서"""
    headers = {
        "Cache-Control": "no-cache",
        "Pragma": "no-cache",
        "User-Agent": "Mozilla/5.0 (MITFeedCollector/1.0)"
    }

    try:
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()
        xml_data = response.text
    except Exception as e:
        logger.error(f"❌ Failed to fetch RSS: {topic} ({e})")
        return []

    feed = feedparser.parse(xml_data)
    articles = []

    for entry in feed.entries:
        articles.append({
            "topic": topic,
            "title": entry.title,
            "link": entry.link,
            "summary": getattr(entry, "summary", ""),
            "published": getattr(entry, "published", ""),
            "guid": getattr(entry, "id", entry.link),
            "fetched_at": time.strftime("%Y-%m-%dT%H:%M:%S"),
        })

    return articles
