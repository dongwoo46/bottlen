import hashlib

def generate_hash_id(*args) -> str:
    """
    여러 필드를 기반으로 고유 해시 ID 생성
    사용 예: generate_hash_id(link, title, published)
    """
    raw_id = "|".join(map(str, args))
    return hashlib.sha256(raw_id.encode("utf-8")).hexdigest()
