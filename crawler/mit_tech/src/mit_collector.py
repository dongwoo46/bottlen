import os
import json
import time
from rss_parser import parse_feed
from shared.utils import save_json, get_data_dir
from shared.logger_config import logger
from topic_collector import fetch_topic
from shared.bloom_filter import BloomFilter          # ✅ RedisBloom
from shared.hash_utils import generate_hash_id       # ✅ 고유 해시 생성기

# ───────────────────────────────
# 기본 설정
# ───────────────────────────────
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = get_data_dir("mit_tech")

GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"

# ✅ RedisBloom 초기화
bloom = BloomFilter(host="localhost", port=6379)
bloom.ensure_filter("mit_tech")

# ───────────────────────────────
# feeds.json 로드
# ───────────────────────────────
def load_feeds():
    """MIT Tech Review RSS/Topic URL 목록 로드"""
    path = os.path.join(BASE_DIR, "../config/feeds.json")
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)

# ───────────────────────────────
# RSS + API 피드 중복 필터링 로직
# ───────────────────────────────
def filter_new_articles(topic: str, articles: list):
    """
    각 기사 리스트에서 RedisBloom 기반으로 중복 제거 수행.
    고유 해시 ID(link + title)를 생성하여 중복 필터링함.
    """
    new_articles = []
    duplicate_count = 0

    for article in articles:
        link = article.get("link", "")
        title = article.get("title", "")
        if not link or not title:
            continue

        # ✅ 고유 해시 생성
        article_hash = generate_hash_id(link, title)

        # ✅ RedisBloom 중복 체크
        if not bloom.add(f"mit_tech:{topic}", article_hash):
            duplicate_count += 1
            continue

        # 해시를 id로 추가
        article["id"] = article_hash
        new_articles.append(article)

    return new_articles, duplicate_count

# ───────────────────────────────
# 한 사이클 실행
# ───────────────────────────────
def run_cycle():
    """MIT Tech Review 피드 및 토픽 크롤링 1회 실행"""
    feeds = load_feeds()
    success_topics, empty_topics = [], []
    duplicate_stats = {}

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"{CYAN}MIT Tech Review Feed Parsing | {time.strftime('%Y-%m-%d %H:%M:%S')}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    # ------------------------------------------------------
    # ① 메인 피드
    # ------------------------------------------------------
    try:
        articles = parse_feed("main", feeds["main"])
        articles, dup_count = filter_new_articles("main", articles)
        duplicate_stats["main"] = dup_count

        if articles:
            save_json(articles, os.path.join(DATA_DIR, "main.json"))
            success_topics.append(f"main({len(articles)})")
        else:
            empty_topics.append("main")
    except Exception as e:
        logger.error(f"[main] RSS feed error: {e}")
        empty_topics.append("main")

    # ------------------------------------------------------
    # ② RSS 기반 토픽 피드
    # ------------------------------------------------------
    for topic, url in feeds["topics"].items():
        time.sleep(1)
        try:
            articles = parse_feed(topic, url)
            articles, dup_count = filter_new_articles(topic, articles)
            duplicate_stats[topic] = dup_count

            if articles:
                save_json(articles, os.path.join(DATA_DIR, f"{topic}.json"))
                success_topics.append(f"{topic}({len(articles)})")
            else:
                empty_topics.append(topic)
        except Exception as e:
            logger.error(f"[{topic}] RSS feed error: {e}")
            empty_topics.append(topic)

    # ------------------------------------------------------
    # ③ API 기반 토픽 (business, climate-change)
    # ------------------------------------------------------
    for topic in ["business", "climate-change"]:
        time.sleep(1)
        try:
            articles = fetch_topic(topic, max_pages=5)
            articles, dup_count = filter_new_articles(topic, articles)
            duplicate_stats[topic] = dup_count

            if articles:
                save_json(articles, os.path.join(DATA_DIR, f"{topic}.json"))
                success_topics.append(f"{topic}({len(articles)})")
            else:
                empty_topics.append(topic)
        except Exception as e:
            logger.error(f"[{topic}] API 수집 실패: {e}")
            empty_topics.append(topic)

    # ------------------------------------------------------
    # 결과 출력
    # ------------------------------------------------------
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
