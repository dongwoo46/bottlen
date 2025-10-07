import os
import json
import logging


def get_logger():
    """콘솔 + 파일 로그 기록"""
    logger = logging.getLogger("RSSLogger")
    logger.setLevel(logging.INFO)

    if not logger.handlers:
        # === 콘솔 출력 ===
        console_handler = logging.StreamHandler()
        console_formatter = logging.Formatter("[%(asctime)s] %(levelname)s - %(message)s")
        console_handler.setFormatter(console_formatter)

        # === 파일 로그 ===
        log_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "../logs")
        os.makedirs(log_dir, exist_ok=True)
        file_path = os.path.join(log_dir, "rss_scraper.log")

        file_handler = logging.FileHandler(file_path, encoding="utf-8")
        file_formatter = logging.Formatter("[%(asctime)s] %(levelname)s - %(message)s")
        file_handler.setFormatter(file_formatter)

        # === 등록 ===
        logger.addHandler(console_handler)
        logger.addHandler(file_handler)

    return logger


def save_json(data, path):
    """데이터가 있을 때만 JSON 파일로 저장"""
    if not data:  # 빈 리스트나 None이면 저장하지 않음
        return

    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
