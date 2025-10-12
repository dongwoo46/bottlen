import os
import time
import feedparser
from bs4 import BeautifulSoup
from shared.utils import save_json, get_data_dir
from shared.logger_config import logger
from shared.bloom_filter import BloomFilter
from shared.hash_utils import generate_hash_id
import json

# ───────── 기본 설정 (SeekingAlpha 전용)
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = get_data_dir("seeking_alpha")  # ✅ 데이터 저장 디렉토리

GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"

# ───────── RedisBloom 초기화
bloom = BloomFilter(host="localhost", port=6379)
bloom.ensure_filter("seekingalpha")  # ✅ SeekingAlpha 네임스페이스 지정

# ───────── feeds.json 로드
def load_feeds():
    """SeekingAlpha RSS 피드 URL 목록 로드"""
    path = os.path.join(BASE_DIR, "../config/feeds.json")
    with open(path, "r", encoding="utf-8") as f:
        cfg = json.load(f)
    return cfg.get("seekingalpha", {})  # ✅ seekingalpha 키 사용

# ───────── HTML 정제
def clean_html(raw_html: str) -> str:
    """HTML 태그 제거 및 텍스트 정제"""
    if not raw_html:
        return ""
    soup = BeautifulSoup(raw_html, "html.parser")
    text = soup.get_text(separator=" ", strip=True)
    return " ".join(text.split())

# ───────── RSS 피드 파싱
def parse_feed(topic: str, url: str):
    """SeekingAlpha RSS 피드 파싱 + RedisBloom 중복 제거"""
    try:
        feed = feedparser.parse(url)
        if not feed.entries:
            logger.warning(f"[{topic}] SeekingAlpha RSS 빈 피드: {url}")
            return [], 0

        articles = []
        duplicate_count = 0

        for entry in feed.entries:
            link = entry.get("link", "")
            title = entry.get("title", "")
            if not link or not title:
                continue

            # 고유 해시 생성
            article_hash = generate_hash_id(link, title)

            # ✅ RedisBloom 중복 체크
            if not bloom.add(f"seekingalpha:{topic}", article_hash):
                duplicate_count += 1
                continue

            summary_raw = entry.get("summary", "")
            summary = clean_html(summary_raw)

            content_html = ""
            if "content" in entry and entry.content:
                content_html = entry.content[0].get("value", "")
            content = clean_html(content_html)

            categories = [tag.term for tag in entry.get("tags", [])] if "tags" in entry else []
            author = entry.get("author", "") if "author" in entry else ""

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
                "collected_at": time.strftime("%Y-%m-%dT%H:%M:%S")
            })

        return articles, duplicate_count

    except Exception as e:
        logger.error(f"[{topic}] SeekingAlpha RSS 파싱 오류: {e}")
        return [], 0

# ───────── 전체 실행 주기
def run_cycle():
    """SeekingAlpha 전체 피드 수집"""
    feeds = load_feeds()
    main_url = feeds.get("main")
    topics = feeds.get("topics", {})

    success_topics, empty_topics = [], []
    duplicate_stats = {}

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"{CYAN}SeekingAlpha Feed Parsing | {time.strftime('%Y-%m-%d %H:%M:%S')}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    # ───────── main 피드
    try:
        articles, dup = parse_feed("main", main_url)
        duplicate_stats["main"] = dup
        if articles:
            save_json(articles, os.path.join(DATA_DIR, "main.json"))
            success_topics.append(f"main({len(articles)})")
        else:
            empty_topics.append("main")
    except Exception as e:
        logger.error(f"[main] SeekingAlpha 피드 오류: {e}")
        empty_topics.append("main")

    # ───────── 토픽별 피드
    for topic, url in topics.items():
        time.sleep(1)
        try:
            articles, dup = parse_feed(topic, url)
            duplicate_stats[topic] = dup
            if articles:
                save_json(articles, os.path.join(DATA_DIR, f"{topic}.json"))
                success_topics.append(f"{topic}({len(articles)})")
            else:
                empty_topics.append(topic)
        except Exception as e:
            logger.error(f"[{topic}] SeekingAlpha RSS 오류: {e}")
            empty_topics.append(topic)

    # ───────── 결과 출력
    print(f"{GREEN}✔ Success".ljust(15) + f"{', '.join(success_topics) or '-'}{RESET}")
    print(f"{YELLOW}⚠ Empty".ljust(15) + f"{', '.join(empty_topics) or '-'}{RESET}")
    print(f"{CYAN}🧠 Duplicates{RESET}".ljust(15))
    for topic, c in duplicate_stats.items():
        if c > 0:
            print(f"   {topic}: {YELLOW}{c}{RESET} 중복")

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"✅ Completed | {GREEN}{len(success_topics)} 성공{RESET} | {YELLOW}{len(empty_topics)} 실패{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    return {"success": success_topics, "empty": empty_topics, "duplicates": duplicate_stats}
