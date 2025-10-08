# shared/bloom_filter.py
import os
import logging
from redisbloom.client import Client

logger = logging.getLogger(__name__)

class BloomFilter:
    """
    RedisBloom 기반 중복 필터 유틸.
    모든 Collector에서 공통으로 사용 가능.
    """

    def __init__(self, host="localhost", port=6379, key_prefix="rss_seen"):
        """
        :param host: Redis 서버 호스트
        :param port: Redis 서버 포트
        :param key_prefix: 각 Collector 구분용 Prefix
        """
        self.client = Client(host=host, port=port)
        self.key_prefix = key_prefix

    def get_key(self, topic: str) -> str:
        """
        토픽별 Bloom Key 이름 생성
        예: rss_seen:ars_technica, rss_seen:ieee 등
        """
        return f"{self.key_prefix}:{topic}"

    def exists(self, topic: str, value: str) -> bool:
        """
        값 존재 여부 확인
        """
        try:
            key = self.get_key(topic)
            return self.client.bfExists(key, value)
        except Exception as e:
            logger.error(f"BloomFilter.exists() 실패: {e}")
            return False

    def add(self, key: str, value: str) -> bool:
        """True → 새 데이터 / False → 이미 존재"""
        try:
            result = self.client.bfAdd(key, value)
            return result == 1
        except Exception as e:
            # 필터가 없을 수도 있으니 reserve 후 재시도
            try:
                self.client.bfReserve(key, 0.001, 100000)
                result = self.client.bfAdd(key, value)
                return result == 1
            except Exception as e2:
                print(f"[BloomFilter.add] 오류: {e2}")
                return False

    def ensure_filter(self, topic: str, capacity=100000, error_rate=0.001):
        """
        필터 존재 보장 (없으면 새로 생성)
        초기화할 때 호출하면 안전함.
        """
        key = self.get_key(topic)
        try:
            # 이미 존재할 경우 오류 발생하지 않음
            self.client.bfReserve(key, error_rate, capacity)
        except Exception:
            # 이미 존재하면 무시
            pass
