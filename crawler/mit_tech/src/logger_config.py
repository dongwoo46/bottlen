import os
import logging

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
LOG_DIR = os.path.join(BASE_DIR, "../logs")
os.makedirs(LOG_DIR, exist_ok=True)

LOG_PATH = os.path.join(LOG_DIR, "rss_scraper.log")


def get_logger(name: str = "rss_scraper") -> logging.Logger:
    """
    공통 로거 인스턴스 반환.
    모든 모듈에서 동일한 로거를 가져가며, 중복 핸들러 추가 방지.
    """
    logger = logging.getLogger(name)
    logger.setLevel(logging.ERROR)

    if not logger.handlers:
        file_handler = logging.FileHandler(LOG_PATH, encoding="utf-8")
        formatter = logging.Formatter("[%(asctime)s] %(levelname)s - %(message)s")
        file_handler.setFormatter(formatter)
        logger.addHandler(file_handler)

    return logger


# 모듈 import 시 자동으로 공용 logger 제공
logger = get_logger()
