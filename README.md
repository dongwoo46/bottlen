# Bottlen (보틀렌)

## 1. 프로젝트 소개

Bottlen은 **실시간 뉴스·커뮤니티 데이터와 증권 데이터를 수집·분석하여  
투자자가 직관적으로 트렌드를 파악할 수 있도록 돕는 대시보드 플랫폼**입니다.

현재 금융 플랫폼(토스, 증권사 앱 등)은 단순히 기사를 나열하거나 시세만 보여주지만,  
Bottlen은 **데이터를 요약·구조화**하여 "무엇이 지금 가장 주목받는 테마인지"를  
빠르게 확인할 수 있도록 설계되었습니다.

---

## 2. 목표

- 국내외 뉴스·공시·커뮤니티·증권 데이터를 **실시간으로 수집**
- 키워드/테마별 트렌드 변화를 **요약 및 시각화**
- 사용자가 관심 테마를 구독하고 **맞춤 알림(텔레그램/슬랙)**을 받을 수 있도록 구현
- MVP 수준에서는 단순 수집·요약에 집중, 이후 AI 분석/예측 기능 확장

---

## 3. 데이터 소스

- **국내**
  - 네이버 뉴스 RSS (경제/증권)
  - DART 공시 API
  - KRX RSS 및 통계 데이터
  - 정부·기관 보도자료
  - 커뮤니티 크롤링 고려(디시인사이드, FM코리아 등)
- **해외**
  - Google News RSS
  - Yahoo Finance RSS (yfinance 라이브러리 활용)
  - Reddit API (r/stocks, r/wallstreetbets)
  - Stocktwits API (커뮤니티 종목 토론) - 신규 api 발급 사용 불가
  - SEC EDGAR API (미국 기업 공시)
  - 핵심 (영향력 90%): Reddit, Stocktwits, Twitter(X)
  - 보조 (심화): Seeking Alpha, TradingView, Investing.com
  - 지역 특화: 일본은 5ch, Yahoo! Finance Japan

---

## 4. 시스템 아키텍처

- **bottlen-webflux**

  - Spring Boot WebFlux (Kotlin, Java 21)
  - 외부 API/RSS 수집, 데이터 전처리
  - Postgres(R2DBC) + Redis(실시간 순위 캐시) 저장
  - 이후 Kafka 확장 가능 (데이터 파이프라인 분리)

- **bottlen-mvc**
  - Spring Boot MVC (Java 21)
  - 회원가입/로그인, 관심 테마 구독 관리
  - 알림 설정 (텔레그램/슬랙 Webhook)
  - 대시보드 API 및 통계 제공
  - Postgres(JPA) + Redis(세션/캐시)

---

## 5. 알림 기능

- 개인별 관심 테마 구독
- 조건 만족 시 → 텔레그램 Bot DM 발송 (무료)
- 협업/테스트용 → Slack Webhook 연동
- 추후 서비스 확장 시 → 카카오 알림톡, 자체 Push (FCM) 고려

---

## 6. 개발 로드맵

1. **MVP**
   - WebFlux로 외부 데이터 수집/저장
   - MVC로 회원가입/로그인, 관심 테마 설정
   - 텔레그램 Bot 알림
   - 기본 대시보드 API
   - OpenSearch/Elasticsearch 도입 (검색/트렌드 조회 최적화)
2. **Phase 2**
   - Kafka 도입 (데이터 파이프라인 안정화)
   - AI 기반 요약/트렌드 예측 기능
3. **Phase 3**
   - 모바일 앱 및 Push 알림
   - 고급 분석(투자자별 매매 통계, 머신러닝 기반 투자 시그널)

---

## 7. 기술 스택

- **Backend**
  - Spring Boot 3.5.x (WebFlux, MVC)
  - Kotlin (WebFlux), Java (MVC)
- **Database**
  - PostgreSQL (R2DBC, JPA)
  - Redis (Reactive, 세션/캐시)
- **Infra**
  - Docker, Docker Compose, OpenSearch
  - (추후) Kafka,
- **Integration**
  - Telegram Bot API
  - Slack Webhook
