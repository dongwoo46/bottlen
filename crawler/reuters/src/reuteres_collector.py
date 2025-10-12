import os
import time
import feedparser
from bs4 import BeautifulSoup
from shared.utils import save_json, get_data_dir
from shared.logger_config import logger
from shared.bloom_filter import BloomFilter
from shared.hash_utils import generate_hash_id
import json

# ───────────────────────────────
# 기본 설정 (Reuters 전용)
# ───────────────────────────────
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = get_data_dir("reuters")

GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"

# ───────── RedisBloom 초기화
bloom = BloomFilter(host="localhost", port=6379)
bloom.ensure_filter("reuters")

# ───────── feeds.json 로드
def load_feeds():
    """Reuters RSS 피드 URL 목록 로드"""
    path = os.path.join(BASE_DIR, "../config/feeds.json")
    with open(path, "r", encoding="utf-8") as f:
        cfg = json.load(f)
    return cfg.get("reuters", {})

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
    """Reuters RSS 피드 파싱 + RedisBloom 중복 제거"""
    try:
        feed = feedparser.parse(url)
        if not feed.entries:
            logger.warning(f"[{topic}] Reuters RSS 빈 피드: {url}")
            return [], 0

        articles = []
        duplicate_count = 0

        for entry in feed.entries:
            title_raw = entry.get("title", "").strip()
            link = entry.get("link", "").strip()
            if not title_raw or not link:
                continue

            # 제목 정제 (HTML entity 제거)
            title = clean_html(title_raw)

            # 고유 해시 생성
            article_hash = generate_hash_id(link, title)

            # ✅ RedisBloom 중복 체크
            if not bloom.add(f"reuters:{topic}", article_hash):
                duplicate_count += 1
                continue

            # 요약(summary)
            summary_raw = entry.get("summary", "") or entry.get("description", "")
            summary = clean_html(summary_raw)

            # Reuters는 content 거의 없음
            content = ""

            # 작성자 기본값
            author = entry.get("author", "") or "Reuters"

            # 발행일 (published → updated → 현재시간)
            published = (
                    entry.get("published", "")
                    or entry.get("updated", "")
                    or time.strftime("%Y-%m-%dT%H:%M:%S")
            )

            # 카테고리 (존재하지 않으면 빈 배열)
            categories = [tag.term for tag in entry.get("tags", [])] if "tags" in entry else []

            articles.append({
                "id": article_hash,
                "topic": topic,
                "title": title,
                "link": link,
                "summary": summary,
                "content": content,
                "published": published,
                "author": author,
                "categories": categories,
                "collected_at": time.strftime("%Y-%m-%dT%H:%M:%S")
            })

        return articles, duplicate_count

    except Exception as e:
        logger.error(f"[{topic}] Reuters RSS 파싱 오류: {e}")
        return [], 0

# ───────── 전체 실행 주기
def run_cycle():
    """Reuters 전체 피드 수집"""
    feeds = load_feeds()
    main_url = feeds.get("main")
    topics = feeds.get("topics", {})

    success_topics, empty_topics = [], []
    duplicate_stats = {}

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"{CYAN}Reuters Feed Parsing | {time.strftime('%Y-%m-%d %H:%M:%S')}{RESET}")
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
        logger.error(f"[main] Reuters 피드 오류: {e}")
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
            logger.error(f"[{topic}] Reuters RSS 오류: {e}")
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
