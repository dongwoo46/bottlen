import os
import json
import time
import feedparser
from bs4 import BeautifulSoup
from shared.utils import save_json, get_data_dir
from shared.logger_config import logger
from shared.bloom_filter import BloomFilter
from shared.hash_utils import generate_hash_id  # ✅ 해시 유틸 사용

# ───────────────────────────────
# 기본 설정
# ───────────────────────────────
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CONFIG_DIR = os.path.join(BASE_DIR, "../config")
DATA_DIR = get_data_dir("ars_technica")

GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"

# ✅ RedisBloom 초기화
bloom = BloomFilter(host="localhost", port=6379)
bloom.ensure_filter("ars_technica")

# ───────────────────────────────
# feeds.json 로드
# ───────────────────────────────
def load_feeds():
    path = os.path.join(CONFIG_DIR, "feeds.json")
    with open(path, "r", encoding="utf-8") as f:
        feeds = json.load(f)
        return feeds.get("ars_technica", {})


# ───────────────────────────────
# HTML 정제
# ───────────────────────────────
def clean_html(raw_html: str) -> str:
    if not raw_html:
        return ""
    soup = BeautifulSoup(raw_html, "html.parser")
    text = soup.get_text(separator=" ", strip=True)
    return " ".join(text.split())


# ───────────────────────────────
# RSS 파싱
# ───────────────────────────────
def parse_feed(topic: str, url: str):
    """Ars Technica RSS 파싱 + RedisBloom 중복제거"""
    try:
        feed = feedparser.parse(url)
        if not feed.entries:
            logger.warning(f"[{topic}] RSS 피드 비어있음: {url}")
            return [], 0

        articles = []
        duplicate_count = 0

        for entry in feed.entries:
            link = entry.get("link", "")
            title = entry.get("title", "")
            if not link or not title:
                continue

            # ✅ 해시 생성 (link + title)
            article_hash = generate_hash_id(link, title)

            # ✅ Bloom Filter 중복 체크
            if not bloom.add(f"ars_technica:{topic}", article_hash):
                duplicate_count += 1
                continue

            # 요약
            summary_raw = entry.get("summary", "")
            summary_text = clean_html(summary_raw)

            # 본문 일부
            content_html = ""
            if "content" in entry and entry.content:
                content_html = entry.content[0].get("value", "")
            elif "summary_detail" in entry:
                content_html = entry.summary_detail.get("value", "")
            content_text = clean_html(content_html)

            # 대표 이미지
            image_url = None
            if "media_content" in entry:
                image_url = entry.media_content[0].get("url")

            # 카테고리
            categories = [tag.term for tag in entry.get("tags", [])] if "tags" in entry else []

            # 작성자
            author = entry.get("author", "")
            if not author and "dc_creator" in entry:
                author = entry.get("dc_creator")

            articles.append({
                "id": article_hash,  # ✅ 고유 ID 포함
                "title": title,
                "link": link,
                "summary": summary_text,
                "content": content_text,
                "published": entry.get("published", ""),
                "author": author,
                "categories": categories,
                "image_url": image_url,
                "topic": topic,
                "collected_at": time.strftime("%Y-%m-%dT%H:%M:%S")
            })

        return articles, duplicate_count

    except Exception as e:
        logger.error(f"[{topic}] RSS 파싱 실패: {e}")
        return [], 0


# ───────────────────────────────
# 수집 루프
# ───────────────────────────────
def run_cycle():
    feeds = load_feeds()
    success_topics, empty_topics = [], []
    duplicate_stats = {}

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"{CYAN}Ars Technica Feed Parsing | {time.strftime('%Y-%m-%d %H:%M:%S')}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    # main 피드
    try:
        main_articles, dup_count = parse_feed("main", feeds.get("main", ""))
        duplicate_stats["main"] = dup_count
        if main_articles:
            save_json(main_articles, os.path.join(DATA_DIR, "main.json"))
            success_topics.append(f"main({len(main_articles)})")
        else:
            empty_topics.append("main")
    except Exception as e:
        logger.error(f"[main] 수집 실패: {e}")
        empty_topics.append("main")

    # 나머지 토픽 피드
    topics = feeds.get("topics", {})
    for topic, url in topics.items():
        time.sleep(1)
        try:
            articles, dup_count = parse_feed(topic, url)
            duplicate_stats[topic] = dup_count
            if articles:
                save_json(articles, os.path.join(DATA_DIR, f"{topic}.json"))
                success_topics.append(f"{topic}({len(articles)})")
            else:
                empty_topics.append(topic)
        except Exception as e:
            logger.error(f"[{topic}] RSS feed error: {e}")
            empty_topics.append(topic)

    # 결과 출력
    print(f"{GREEN}✔ Success".ljust(15) + f"{', '.join(success_topics) or '-'}{RESET}")
    print(f"{YELLOW}⚠ Empty".ljust(15) + f"{', '.join(empty_topics) or '-'}{RESET}")
    print(f"{CYAN}🧠 Duplicates{RESET}".ljust(15))
    for topic, count in duplicate_stats.items():
        if count > 0:
            print(f"   {topic}: {YELLOW}{count}{RESET} 중복 건수")

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"✅ Completed | {GREEN}{len(success_topics)} 성공{RESET} | {YELLOW}{len(empty_topics)} 실패{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    return {"success": success_topics, "empty": empty_topics, "duplicates": duplicate_stats}
