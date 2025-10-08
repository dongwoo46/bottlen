import os
import json
import logging


def save_json(data, path):
    """데이터가 있을 때만 JSON 파일로 저장"""
    if not data:  # 빈 리스트나 None이면 저장하지 않음
        return

    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)


def get_data_dir(module_name: str):
    """모듈 이름 기준 data 폴더 절대 경로 반환 (항상 프로젝트 루트 기준으로 계산)"""
    # 현재 utils.py 위치 기준으로 상위 디렉터리(crawler)까지 올라감
    project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), os.pardir))
    data_path = os.path.join(project_root, module_name, "data")

    os.makedirs(data_path, exist_ok=True)
    return data_path
