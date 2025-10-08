import logging
import os
from datetime import datetime

# 로그 저장 디렉터리
LOG_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "logs")
os.makedirs(LOG_DIR, exist_ok=True)

# 로그 파일 이름 (날짜별)
LOG_FILE = os.path.join(LOG_DIR, f"crawler_{datetime.now().strftime('%Y%m%d')}.log")

# 로그 포맷
LOG_FORMAT = "[%(asctime)s] [%(levelname)s] [%(name)s] %(message)s"
DATE_FORMAT = "%Y-%m-%d %H:%M:%S"

# 전역 logger
logger = logging.getLogger("crawler")
logger.setLevel(logging.INFO)

# 핸들러 중복 방지
if not logger.handlers:
    # 콘솔 출력
    console_handler = logging.StreamHandler()
    console_handler.setFormatter(logging.Formatter(LOG_FORMAT, DATE_FORMAT))

    # 파일 출력
    file_handler = logging.FileHandler(LOG_FILE, encoding="utf-8")
    file_handler.setFormatter(logging.Formatter(LOG_FORMAT, DATE_FORMAT))

    logger.addHandler(console_handler)
    logger.addHandler(file_handler)

# 사용 예시:
# from shared.logger_config import logger
# logger.info("크롤러 시작")
