import os
from dotenv import load_dotenv
from telethon import TelegramClient
from telethon.errors import UsernameInvalidError, UsernameNotOccupiedError, ChannelPrivateError

load_dotenv()

api_id = int(os.getenv("TELEGRAM_API_ID"))
api_hash = os.getenv("TELEGRAM_API_HASH")
session_name = os.getenv("SESSION_NAME", "bottlen_telegram")

client = TelegramClient(session_name, api_id, api_hash)


async def fetch_channel_messages(channel_username: str, limit: int = 20):
    """텔레그램 채널 최근 메시지 가져오기"""
    try:
        entity = await client.get_entity(channel_username)
        print(f"\n✅ 성공: {channel_username}")
        async for message in client.iter_messages(entity, limit=limit):
            if message.text:
                print("────────────────────────────────────────────")
                print(f"[{channel_username}] {message.date}")
                print(message.text[:300])  # 300자까지만 미리보기
        return True
    except (UsernameInvalidError, UsernameNotOccupiedError):
        print(f"❌ 실패 (채널 존재 안 함 또는 이름 오류): {channel_username}")
        return False
    except ChannelPrivateError:
        print(f"⚠️ 실패 (비공개 채널 또는 초대 필요): {channel_username}")
        return False
    except Exception as e:
        print(f"❌ 실패 ({channel_username}): {e}")
        return False


async def main():
    channels = [
        "StockPro_Online",              # ✅ https://t.me/StockPro_Online
        "fxstreetforex",                # ✅ https://t.me/fxstreetforex
        "top_tradingsignals",           # ✅ https://t.me/top_tradingsignals
        "altsignals",                   # ✅ https://t.me/altsignals
        "equity99",                     # ✅ https://t.me/equity99
        "fbsanalytics",                 # ✅ https://t.me/fbsanalytics
        "TheFinancialExpressOnline",    # ✅ https://t.me/TheFinancialExpressOnline
        "marketfeed",                   # ✅ https://t.me/marketfeed
        "wolfoftrading",                # ✅ https://t.me/wolfoftrading
    ]

    success_list, fail_list = [], []

    for ch in channels:
        ok = await fetch_channel_messages(ch, limit=5)
        (success_list if ok else fail_list).append(ch)

    print("\n==============================")
    print("📊 결과 요약")
    print(f"✅ 성공한 채널: {len(success_list)}개")
    for s in success_list:
        print(f"  - {s}")
    print(f"❌ 실패한 채널: {len(fail_list)}개")
    for f in fail_list:
        print(f"  - {f}")
    print("==============================")


if __name__ == "__main__":
    with client:
        client.loop.run_until_complete(main())
