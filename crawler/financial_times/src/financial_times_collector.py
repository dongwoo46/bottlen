import os
import time
import feedparser
from bs4 import BeautifulSoup
from shared.utils import save_json, get_data_dir
from shared.logger_config import logger
from shared.bloom_filter import BloomFilter
from shared.hash_utils import generate_hash_id
import json

# ───────── 기본 설정 ─────────
SOURCE_NAME = "financial_times"
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = get_data_dir(SOURCE_NAME)

GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"

# ───────── RedisBloom 초기화 ─────────
bloom = BloomFilter(host="localhost", port=6379)
bloom.ensure_filter(SOURCE_NAME)


# ───────── feeds.json 로드 ─────────
def load_feeds():
    """feeds.json에서 Financial Times 관련 피드 로드"""
    path = os.path.join(BASE_DIR, "../config/feeds.json")
    with open(path, "r", encoding="utf-8") as f:
        cfg = json.load(f)
    return cfg.get(SOURCE_NAME, {})


# ───────── HTML 정제 ─────────
def clean_html(raw_html: str) -> str:
    """HTML 태그 제거 및 공백 정리"""
    if not raw_html:
        return ""
    soup = BeautifulSoup(raw_html, "html.parser")
    return " ".join(soup.get_text(separator=" ", strip=True).split())


# ───────── RSS 파싱 ─────────
def parse_feed(topic: str, url: str):
    """RSS 피드 파싱 및 중복 필터링"""
    try:
        feed = feedparser.parse(url)
        if not feed.entries:
            logger.warning(f"[{topic}] Financial Times RSS 빈 피드: {url}")
            return [], 0

        articles = []
        duplicate_count = 0

        for entry in feed.entries:
            title, link = entry.get("title", ""), entry.get("link", "")
            if not title or not link:
                continue

            # 해시 생성 (중복 식별)
            article_hash = generate_hash_id(link, title)

            # ✅ RedisBloom 중복 체크
            if not bloom.add(f"{SOURCE_NAME}:{topic}", article_hash):
                duplicate_count += 1
                continue

            # 본문 및 요약 처리
            summary = clean_html(entry.get("summary", ""))
            content = ""
            if "content" in entry and entry.content:
                content = clean_html(entry.content[0].get("value", ""))

            author = entry.get("author", "")
            if not author and "dc_creator" in entry:
                author = entry.get("dc_creator")

            categories = [tag.term for tag in entry.get("tags", [])] if "tags" in entry else []

            articles.append({
                "id": article_hash,
                "topic": topic,
                "title": title,
                "link": link,
                "summary": summary,
                "content": content,
                "published": entry.get("published", ""),
                "author": author,
                "categories": categories,
                "source": "Financial Times",
                "collected_at": time.strftime("%Y-%m-%dT%H:%M:%S")
            })

        return articles, duplicate_count

    except Exception as e:
        logger.error(f"[{topic}] Financial Times RSS 파싱 오류: {e}")
        return [], 0


# ───────── 실행 루프 ─────────
def run_cycle():
    """Financial Times RSS 전체 주기 수집"""
    feeds = load_feeds()
    main_url = feeds.get("main")
    topics = feeds.get("topics", {})

    success_topics, empty_topics = [], []
    duplicate_stats = {}

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"{CYAN}Financial Times Feed Parsing | {time.strftime('%Y-%m-%d %H:%M:%S')}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    all_topics = [("main", main_url)] + list(topics.items())

    for topic, url in all_topics:
        time.sleep(1)
        try:
            articles, dup = parse_feed(topic, url)
            duplicate_stats[topic] = dup

            # ✅ 중복만 있어도 통계에 포함
            if articles or dup > 0:
                save_json(articles, os.path.join(DATA_DIR, f"{topic}.json"))
                success_topics.append(f"{topic}({len(articles)} new, {dup} dup)")
            else:
                empty_topics.append(topic)

        except Exception as e:
            logger.error(f"[{topic}] Financial Times RSS 오류: {e}")
            empty_topics.append(topic)

    # ───────── 결과 출력 ─────────
    print(f"{GREEN}✔ Success".ljust(15) + f"{', '.join(success_topics) or '-'}{RESET}")
    print(f"{YELLOW}⚠ Empty".ljust(15) + f"{', '.join(empty_topics) or '-'}{RESET}")

    print(f"{CYAN}🧠 Duplicates".ljust(15))
    has_duplicates = False
    for topic, c in duplicate_stats.items():
        if c > 0:
            has_duplicates = True
            print(f"   {topic}: {YELLOW}{c}{RESET} 중복")
    if not has_duplicates:
        print("   (중복 없음)")

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"✅ Completed | {GREEN}{len(success_topics)} 성공{RESET} | {YELLOW}{len(empty_topics)} 실패{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    return {"success": success_topics, "empty": empty_topics, "duplicates": duplicate_stats}
