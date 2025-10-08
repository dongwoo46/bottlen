import os
import json
import time
from rss_parser import parse_feed
from shared.utils import save_json, get_data_dir
from shared.logger_config import logger
from topic_collector import fetch_topic, save_topic_json

# ───────────────────────────────
# 기본 설정
# ───────────────────────────────
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = get_data_dir("mit_tech")  # 모듈별 data 디렉터리 절대경로

GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"


# ───────────────────────────────
# feeds.json 로드
# ───────────────────────────────
def load_feeds():
    """MIT Tech Review RSS/Topic URL 목록 로드"""
    path = os.path.join(BASE_DIR, "../config/feeds.json")
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


# ───────────────────────────────
# 한 사이클 실행
# ───────────────────────────────
def run_cycle():
    """MIT Tech Review 피드 및 토픽 크롤링 1회 실행"""
    feeds = load_feeds()
    success_topics, empty_topics = [], []

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"{CYAN}MIT Tech Review Feed Parsing | {time.strftime('%Y-%m-%d %H:%M:%S')}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    # ------------------------------------------------------
    # ① 메인 피드
    # ------------------------------------------------------
    try:
        articles = parse_feed("main", feeds["main"])
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
    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"✅ Completed | {GREEN}{len(success_topics)} 성공{RESET} | {YELLOW}{len(empty_topics)} 실패{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    return {"success": success_topics, "empty": empty_topics}
