# crawler/ars_technica/main.py
from ars_technica.src.ars_technica_collector import run_cycle
from shared.bloom_filter import BloomFilter

# 초기화 대상 토픽 목록
TOPICS = [
    "main",
    "science",
    "gadgets",
    "law",
    "features",
    "cars",
    "staff_blogs",
    "business"
]

if __name__ == "__main__":
    # print("────────────────────────────────────────────")
    # print("🧠 RedisBloom 초기화 중...")
    # print("────────────────────────────────────────────")
    #
    # bloom = BloomFilter(host="localhost", port=6379)
    # base_key = "ars_technica"
    #
    # for topic in TOPICS:
    #     key = f"{base_key}:{topic}"
    #     bloom.client.delete(key)
    #     print(f"✅ {key} 초기화 완료")
    #
    #     # TTL 설정 (예: 24시간 = 86400초)
    #     bloom.client.expire(key, 86400)
    #     print(f"⏳ TTL 24시간 설정됨 → {key}")
    #
    # print("────────────────────────────────────────────")
    # print("🌱 RedisBloom 초기화 완료")
    # print("────────────────────────────────────────────")
    run_cycle()
