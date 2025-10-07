import requests
import json
from datetime import datetime
from logger_config import logger

BASE_URL = "https://wp.technologyreview.com/wp-json/irving/v1/data/topic_feed"

TOPIC_MAP = {
    "business": 19088,
    "climate-change": 21
}

def fetch_topic(topic: str, max_pages: int = 5):
    topic_id = TOPIC_MAP.get(topic)
    if not topic_id:
        logger.error(f"[{topic}] Invalid topic name")
        return []

    collected = []
    seen_ids = set()  # ✅ 중복 방지를 위한 ID 저장

    for page in range(1, max_pages + 1):
        params = {
            "page": page,
            "orderBy": "date",
            "topic": topic_id,
            "requestType": "topic"
        }

        try:
            res = requests.get(BASE_URL, params=params, timeout=10)
            res.raise_for_status()
            data = res.json()

            # ★ 데이터 구조가 [ { feedPosts: [...] } ] 형태이므로
            if isinstance(data, list):
                data = data[0]
        except Exception as e:
            logger.warning(f"[{topic}] page {page} 요청 실패: {e}")
            continue

        posts = data.get("feedPosts", [])
        if not posts:
            logger.info(f"[{topic}] page {page} 데이터 없음 → 중단")
            break

        new_posts = 0
        for post in posts:
            cfg = post.get("config", {})
            post_id = cfg.get("postId")

            # ✅ 중복 방지
            if not post_id or post_id in seen_ids:
                continue
            seen_ids.add(post_id)

            # ✅ 이미지 URL 추출
            image_url = None
            for ch in post.get("children", []):
                if ch.get("name") == "image":
                    image_url = ch.get("config", {}).get("url")
                    break

            collected.append({
                "title": cfg.get("hed"),
                "summary_html": cfg.get("dek"),
                "summary_text": (cfg.get("dek") or "").replace("<p>", "").replace("</p>", "").strip(),
                "link": cfg.get("link"),
                "postId": post_id,
                "image": image_url,
                "topic": topic,
                "collected_at": datetime.now().isoformat()
            })
            new_posts += 1

        logger.info(f"[{topic}] page {page}: {new_posts}개 추가됨 (총 {len(collected)})")

        # ✅ 새 데이터가 전혀 없으면 중단
        if new_posts == 0:
            logger.info(f"[{topic}] page {page} 이후 중복만 존재 → 중단")
            break

    logger.info(f"[{topic}] 총 {len(collected)}개 기사 수집 완료")
    return collected


def save_topic_json(topic: str, articles: list):
    path = f"../data/{topic}.json"
    with open(path, "w", encoding="utf-8") as f:
        json.dump(articles, f, ensure_ascii=False, indent=4)
    logger.info(f"[{topic}] 저장 완료 → {path}")
