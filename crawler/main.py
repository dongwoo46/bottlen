import time
from shared.logger_config import logger

# ───────────────────────────────────────────────
# 모듈 import
# ───────────────────────────────────────────────
from mit_tech.src.mit_collector import run_cycle as run_mit
from ieee_spectrum.src.ieee_collector import run_cycle as run_ieee

# ───────────────────────────────────────────────
# 공통 설정
# ───────────────────────────────────────────────
INTERVAL = 3600  # 1시간마다 갱신
GREEN = "\033[92m"
YELLOW = "\033[93m"
RESET = "\033[0m"


def main():
    while True:
        try:
            logger.info("====== 🌐 Global Crawler Cycle Start ======")

            # MIT Tech Review 수집
            logger.info("[MIT Tech] 수집 시작")
            run_mit()

            # IEEE Spectrum 수집
            logger.info("[IEEE Spectrum] 수집 시작")
            run_ieee()


            logger.info("====== ✅ Global Cycle Completed ======")
            print(f"{GREEN}✔ All cycles completed. Waiting for next...{RESET}")
            time.sleep(INTERVAL)

        except KeyboardInterrupt:
            print(f"{YELLOW}🛑 User stopped the crawler.{RESET}")
            logger.info("Crawler stopped by user.")
            break
        except Exception as e:
            logger.error(f"❌ Global error: {e}")
            time.sleep(30)


if __name__ == "__main__":
    main()
