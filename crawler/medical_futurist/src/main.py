import time
from medical_collector import run_cycle

INTERVAL = 3600  # 1시간
YELLOW = "\033[93m"
RESET = "\033[0m"


def main():
    while True:
        try:
            run_cycle()  # 내부에서 모든 로그 출력 처리
            print(f"🕒 Waiting {INTERVAL // 60} minutes for next update...\n")
            time.sleep(INTERVAL)
        except KeyboardInterrupt:
            print(f"\n{YELLOW}🛑 Stopped by user.{RESET}")
            break


if __name__ == "__main__":
    main()
