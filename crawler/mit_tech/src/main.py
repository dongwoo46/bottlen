import os
import json
import time
from rss_parser import parse_feed
from utils import save_json
from logger_config import logger
from topic_collector import fetch_topic, save_topic_json

# ───────────────────────────────
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
INTERVAL = 3600  # 1시간
# ───────────────────────────────
GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"


# ----------------------------------------------------------
# 설정 파일 로드
# ----------------------------------------------------------
def load_feeds():
    path = os.path.join(BASE_DIR, "../config/feeds.json")
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


# ----------------------------------------------------------
# 실행 루프
# ----------------------------------------------------------
def run_cycle():
    feeds = load_feeds()
    success_topics = []
    empty_topics = []

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"{CYAN}MIT Tech Review Feed Parsing | {time.strftime('%Y-%m-%d %H:%M:%S')}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    # ------------------------------------------------------
    # ① main 피드
    # ------------------------------------------------------
    try:
        articles = parse_feed("main", feeds["main"])
        if articles:
            save_json(articles, os.path.join(BASE_DIR, "../data/main.json"))
            success_topics.append(f"main({len(articles)})")
        else:
            empty_topics.append("main")
    except Exception as e:
        logger.error(f"[main] RSS feed error: {e}")
        empty_topics.append("main")

    # ------------------------------------------------------
    # ② 나머지 topic 피드들 (RSS)
    # ------------------------------------------------------
    for topic, url in feeds["topics"].items():
        time.sleep(1)  # 요청 간 간격
        try:
            articles = parse_feed(topic, url)
            if articles:
                save_json(articles, os.path.join(BASE_DIR, f"../data/{topic}.json"))
                success_topics.append(f"{topic}({len(articles)})")
            else:
                empty_topics.append(topic)
        except Exception as e:
            logger.error(f"[{topic}] RSS feed error: {e}")
            empty_topics.append(topic)

    # ------------------------------------------------------
    # ③ API 기반 (business, climate-change)
    # ------------------------------------------------------
    for topic in ["business", "climate-change"]:
        time.sleep(1)
        try:
            articles = fetch_topic(topic, max_pages=5)
            if articles:
                save_topic_json(topic, articles)
                success_topics.append(f"{topic}({len(articles)})")
            else:
                empty_topics.append(topic)
        except Exception as e:
            logger.error(f"[{topic}] API 수집 실패: {e}")
            empty_topics.append(topic)

    # ------------------------------------------------------
    # 콘솔 요약
    # ------------------------------------------------------
    print(f"{GREEN}✔ Success".ljust(15) + f"{', '.join(success_topics) if success_topics else '-'}{RESET}")
    print(f"{YELLOW}⚠ Empty".ljust(15) + f"{', '.join(empty_topics) if empty_topics else '-'}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"✅ Completed | {GREEN}{len(success_topics)} 성공{RESET} | {YELLOW}{len(empty_topics)} 실패{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")



# ----------------------------------------------------------
# 메인 루프
# ----------------------------------------------------------
def main():
    while True:
        try:
            run_cycle()
            print(f"🕒 Waiting {INTERVAL // 60} minutes for next update...\n")
            time.sleep(INTERVAL)
        except KeyboardInterrupt:
            print(f"\n{YELLOW}🛑 Stopped by user.{RESET}")
            break
        except Exception as e:
            logger.error(f"Unexpected error: {e}")
            print(f"{YELLOW}⚠ Error occurred: {e}{RESET}")
            time.sleep(30)


if __name__ == "__main__":
    main()
