package com.bottlen.bottlen_mvc.auth.oauth.dto;

public interface OAuth2Res {

    // 제공자 이름 (예: "google", "naver")
    String getProvider();

    // 제공자 내부에서 유저를 식별하기 위한 고유 ID
    String getProviderId();

    // 사용자 이메일 (일부 제공자는 null일 수 있음)
    String getEmail();

    // 사용자 이름 (닉네임 또는 실명)
    String getName();

    // 휴대전화 번호 (선택 사항 - 일부 플랫폼에서 제공되지 않을 수 있음)
    String getPhoneNumber(); // ← 메서드 이름 camelCase로 수정
}
