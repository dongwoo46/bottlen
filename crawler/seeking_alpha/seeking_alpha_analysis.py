#!/usr/bin/env python3
# seeking_alpha_continuous_crawl.py
import requests
import json
import time
import os

BASE_URL = (
    "https://seekingalpha.com/api/v3/trending/personalized/trending"
    "?fields[article]=publishOn,author,title,primaryTickers,secondaryTickers,summary,sentiments"
    "&filter[category]=trending-analysis"
    "&include=author,primaryTickers,secondaryTickers,sentiments"
    "&isMounting=false&page[size]=20&page[number]={page}"
)

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)",
    "Accept": "application/json, text/plain, */*",
    "Referer": "https://seekingalpha.com/trending-analysis",
    "X-Requested-With": "XMLHttpRequest",
}

FILE_PATH = "sa_analysis_data.json"


def parse_cookie_string(cookie_str: str):
    cookies = {}
    for part in cookie_str.split("; "):
        if "=" in part:
            name, value = part.split("=", 1)
            cookies[name] = value
    return cookies


def load_existing_articles(path=FILE_PATH):
    if not os.path.exists(path):
        print(f"[*] No existing file. New file will be created: {path}")
        return []
    try:
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    except json.JSONDecodeError:
        print("[!] Corrupted JSON file. Starting fresh.")
        return []
    except Exception as e:
        print(f"[!] Error loading file: {e}")
        return []


def save_articles(articles, path=FILE_PATH):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(articles, f, ensure_ascii=False, indent=2)


def extract_article_data(data, included_by_key):
    """필요한 데이터만 뽑아서 dict로 리턴"""
    art_id = data.get("id")
    attrs = data.get("attributes", {}) or {}
    rel = data.get("relationships", {}) or {}

    title = attrs.get("title")
    publish_on = attrs.get("publishOn")
    summary = attrs.get("summary")

    # Author
    author = None
    author_ref = rel.get("author", {}).get("data")
    if author_ref:
        key = (str(author_ref.get("id")), author_ref.get("type"))
        inc = included_by_key.get(key)
        if inc:
            author = inc.get("attributes", {}).get("nick")

    # Tickers
    tickers = []
    for tk in rel.get("primaryTickers", {}).get("data", []):
        key = (str(tk.get("id")), tk.get("type"))
        inc = included_by_key.get(key)
        if inc:
            tickers.append(inc.get("attributes", {}).get("name"))

    # Sentiments (Bullish / Bearish 등)
    sentiments = []
    for sref in rel.get("sentiments", {}).get("data", []):
        key = (str(sref.get("id")), sref.get("type"))
        s_inc = included_by_key.get(key)
        if s_inc:
            sentiments.append(s_inc.get("attributes", {}).get("type"))

    return {
        "id": art_id,
        "title": title,
        "publishOn": publish_on,
        "author": author,
        "tickers": tickers,
        "summary": summary,
        "sentiments": sentiments,
        "link": "https://seekingalpha.com" + (data.get("links", {}).get("self") or "")
    }


def main():
    print("[*] Paste your cookie string from browser (document.cookie):")
    cookie_str = input().strip()
    cookies = parse_cookie_string(cookie_str)

    session = requests.Session()
    session.headers.update(HEADERS)
    session.cookies.update(cookies)

    # 기존 저장된 데이터 불러오기
    all_articles = load_existing_articles()
    seen_ids = {art["id"] for art in all_articles}

    latest_publishOn = None
    if all_articles:
        dates = [art.get("publishOn") for art in all_articles if art.get("publishOn")]
        if dates:
            latest_publishOn = max(dates)

    print(f"[*] Loaded {len(all_articles)} articles, latest_publishOn={latest_publishOn}")

    while True:
        page = 1
        new_articles = []
        stop_crawl = False

        while True:
            url = BASE_URL.format(page=page)
            resp = session.get(url)
            print(f"[*] Page {page} → Status {resp.status_code}")

            if resp.status_code == 403:
                print("[!] Cookie expired or unauthorized. Stop crawling.")
                save_articles(all_articles)
                return

            if resp.status_code != 200:
                break

            data = resp.json()
            articles = data.get("data", [])
            included = data.get("included", [])
            included_by_key = {(str(i.get("id")), i.get("type")): i for i in included}

            if not articles:
                break

            added_in_page = 0
            for art in articles:
                art_id = art.get("id")
                publish_on = art.get("attributes", {}).get("publishOn")

                if art_id in seen_ids:
                    continue
                if latest_publishOn and publish_on and publish_on <= latest_publishOn:
                    stop_crawl = True
                    break

                parsed = extract_article_data(art, included_by_key)
                seen_ids.add(art_id)
                all_articles.append(parsed)
                new_articles.append(parsed)
                added_in_page += 1

            if stop_crawl or added_in_page == 0:
                break
            page += 1

        if new_articles:
            dates = [a["publishOn"] for a in new_articles if a.get("publishOn")]
            if dates:
                latest_publishOn = max(dates)

            save_articles(all_articles)
            print(f"[+] {len(new_articles)} new articles added. Total={len(all_articles)}")
        else:
            print("[*] No new articles this round.")

        time.sleep(60)  # 1분 주기


if __name__ == "__main__":
    main()
