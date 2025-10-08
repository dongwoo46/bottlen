import os
import json
import time
import feedparser
from bs4 import BeautifulSoup
from shared.utils import save_json, get_data_dir
from shared.logger_config import logger

# ───────────────────────────────
# 기본 설정
# ───────────────────────────────
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = get_data_dir("ieee_spectrum")  # 모듈별 data 디렉터리 절대경로

GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"


# ───────────────────────────────
# feeds.json 로드
# ───────────────────────────────
def load_feeds():
    """IEEE Spectrum RSS 피드 URL 목록 로드"""
    path = os.path.join(BASE_DIR, "../config/feeds.json")
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


# ───────────────────────────────
# HTML 정제
# ───────────────────────────────
def clean_html(raw_html: str) -> str:
    """HTML 태그 제거 및 텍스트 정제"""
    if not raw_html:
        return ""
    soup = BeautifulSoup(raw_html, "html.parser")
    text = soup.get_text(separator=" ", strip=True)
    return " ".join(text.split())  # 불필요한 공백 제거


# ───────────────────────────────
# RSS 파싱
# ───────────────────────────────
def parse_feed(topic: str, url: str):
    """IEEE Spectrum RSS 피드 파싱"""
    try:
        feed = feedparser.parse(url)
        if not feed.entries:
            logger.warning(f"[{topic}] RSS 피드 비어있음: {url}")
            return []

        articles = []
        for entry in feed.entries:
            summary_raw = entry.get("summary", "")
            summary_text = clean_html(summary_raw)

            articles.append({
                "title": entry.get("title"),
                "link": entry.get("link"),
                "summary": summary_text,  # ✅ 정제된 텍스트만 유지
                "published": entry.get("published", ""),
                "author": entry.get("author", ""),
                "topic": topic,
                "collected_at": time.strftime("%Y-%m-%dT%H:%M:%S")
            })

        return articles
    except Exception as e:
        logger.error(f"[{topic}] RSS 파싱 실패: {e}")
        return []


# ───────────────────────────────
# 수집 루프 (한 사이클)
# ───────────────────────────────
def run_cycle():
    """IEEE Spectrum RSS 전체 수집 1회 실행"""
    feeds = load_feeds()
    success_topics, empty_topics = [], []

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"{CYAN}IEEE Spectrum Feed Parsing | {time.strftime('%Y-%m-%d %H:%M:%S')}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    # ------------------------------------------------------
    # main 피드
    # ------------------------------------------------------
    try:
        main_articles = parse_feed("main", feeds["main"])
        if main_articles:
            save_json(main_articles, os.path.join(DATA_DIR, "main.json"))
            success_topics.append(f"main({len(main_articles)})")
        else:
            empty_topics.append("main")
    except Exception as e:
        logger.error(f"[main] 수집 실패: {e}")
        empty_topics.append("main")

    # ------------------------------------------------------
    # 나머지 토픽 피드
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
    # 결과 요약
    # ------------------------------------------------------
    print(f"{GREEN}✔ Success".ljust(15) + f"{', '.join(success_topics) or '-'}{RESET}")
    print(f"{YELLOW}⚠ Empty".ljust(15) + f"{', '.join(empty_topics) or '-'}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"✅ Completed | {GREEN}{len(success_topics)} 성공{RESET} | {YELLOW}{len(empty_topics)} 실패{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    return {"success": success_topics, "empty": empty_topics}
