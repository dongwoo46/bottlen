import os
import time
import requests
from bs4 import BeautifulSoup
from shared.utils import save_json, get_data_dir
from shared.logger_config import logger
from shared.bloom_filter import BloomFilter
from shared.hash_utils import generate_hash_id

# ───────── 기본 설정
BASE_URL = "https://www.businessinsider.com"

BUSINESS_SECTIONS = [
    "strategy", "economy", "finance", "retail", "media", "real-estate",
]

TECH_SECTIONS = [
    "artificial-intelligence", "enterprise", "transportation", "startups",
]


LIFESTYLE_SECTIONS = [
    "entertainment", "travel", "food", "health",
]

POLITICS_SECTIONS = [
    "defense", "law",
]

ALL_SECTIONS = (
        BUSINESS_SECTIONS
        + TECH_SECTIONS
        + LIFESTYLE_SECTIONS
        + POLITICS_SECTIONS
)

DATA_DIR = get_data_dir("business_insider")

GREEN = "\033[92m"
YELLOW = "\033[93m"
BLUE = "\033[94m"
CYAN = "\033[96m"
RESET = "\033[0m"

# ───────── RedisBloom 초기화
bloom = BloomFilter(host="localhost", port=6379)


# ───────── 페이지 요청
def fetch_page(section: str, page: int, retries: int = 3):
    url = f"{BASE_URL}/{section}?page={page}"
    headers = {"User-Agent": "Mozilla/5.0"}
    for attempt in range(1, retries + 1):
        try:
            res = requests.get(url, headers=headers, timeout=10)
            if res.status_code == 200:
                return res.text
            else:
                logger.warning(f"[{section}] {page}페이지 상태코드: {res.status_code}")
        except requests.RequestException as e:
            logger.warning(f"[{section}] {page}페이지 요청 실패 (시도 {attempt}/{retries}): {e}")
        time.sleep(1.5 * attempt)
    return None


# ───────── HTML 파서
def parse_articles(page_html: str, topic: str):
    soup = BeautifulSoup(page_html, "html.parser")
    articles = []
    dup_count = 0

    for article in soup.select("article[data-component-type='tout']"):
        title_tag = article.select_one(".tout-title-link")
        summary_tag = article.select_one(".tout-copy")
        time_tag = article.select_one(".tout-read-time")

        if not title_tag:
            continue

        title = title_tag.get_text(strip=True)
        link = title_tag.get("href", "")
        if not link:
            continue
        if link.startswith("/"):
            link = BASE_URL + link

        # ───── BloomFilter 중복 검사
        article_hash = generate_hash_id(link, title)
        if not bloom.add(f"bi:{topic}", article_hash):
            dup_count += 1
            continue

        summary = summary_tag.get_text(strip=True) if summary_tag else ""
        read_time = time_tag.get_text(strip=True) if time_tag else ""

        articles.append({
            "id": article_hash,
            "topic": topic,
            "title": title,
            "link": link,
            "summary": summary,
            "read_time": read_time,
            "collected_at": time.strftime("%Y-%m-%dT%H:%M:%S"),
        })

    return articles, dup_count


# ───────── 섹션 단위 크롤러
def crawl_section(section: str, max_pages: int = 5):
    bloom.ensure_filter(f"bi:{section}")

    success_pages, empty_pages = [], []
    duplicate_stats = {}
    all_articles = []
    stop_due_to_duplicates = False

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"{CYAN}Business Insider | Section: {section} | {time.strftime('%Y-%m-%d %H:%M:%S')}{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    for page in range(1, max_pages + 1):
        html = fetch_page(section, page)
        if not html:
            empty_pages.append(f"page_{page}")
            continue

        articles, dup_count = parse_articles(html, section)
        duplicate_stats[f"page_{page}"] = dup_count

        if articles:
            success_pages.append(f"page_{page}({len(articles)})")
            all_articles.extend(articles)
        else:
            empty_pages.append(f"page_{page}")

        if dup_count > 0:
            print(f"{YELLOW}⚠ BloomFilter hit detected at page {page}. Stop {section}.{RESET}")
            stop_due_to_duplicates = True
            break

        time.sleep(1)

    if all_articles:
        save_path = os.path.join(DATA_DIR, f"{section}.json")
        save_json(all_articles, save_path)
        logger.info(f"[{section}] {len(all_articles)}개 기사 저장 완료: {save_path}")

    print(f"{GREEN}✔ Success".ljust(15) + f"{', '.join(success_pages) or '-'}{RESET}")
    print(f"{YELLOW}⚠ Empty".ljust(15) + f"{', '.join(empty_pages) or '-'}{RESET}")
    print(f"{CYAN}🧠 Duplicates{RESET}")
    for topic, count in duplicate_stats.items():
        if count > 0:
            print(f"   {topic}: {YELLOW}{count}{RESET} 중복")

    print(f"{BLUE}────────────────────────────────────────────{RESET}")
    print(f"✅ Completed | {GREEN}{len(success_pages)} 성공{RESET} | {YELLOW}{len(empty_pages)} 실패{RESET}")
    if stop_due_to_duplicates:
        print(f"{YELLOW}⏹ Stopped early due to BloomFilter hits (existing articles).{RESET}")
    print(f"{BLUE}────────────────────────────────────────────{RESET}")

    return {
        "section": section,
        "success": success_pages,
        "empty": empty_pages,
        "duplicates": duplicate_stats,
        "stopped": stop_due_to_duplicates
    }


# ───────── 전체 섹션 순회
def run_all_sections(max_pages: int = 5):
    print(f"{CYAN}🚀 Starting Business Insider Full Crawl ({len(ALL_SECTIONS)} sections){RESET}")
    results = []

    for section in ALL_SECTIONS:
        result = crawl_section(section, max_pages)
        results.append(result)
        time.sleep(2)

    print(f"{GREEN}✅ All sections completed!{RESET}")
    print(f"{CYAN}────────────────────────────────────────────{RESET}")
    for res in results:
        print(f"{res['section']}: {len(res['success'])} 성공, {len(res['empty'])} 실패, stopped={res['stopped']}")
    print(f"{CYAN}────────────────────────────────────────────{RESET}")
    return results


if __name__ == "__main__":
    run_all_sections(max_pages=10)
