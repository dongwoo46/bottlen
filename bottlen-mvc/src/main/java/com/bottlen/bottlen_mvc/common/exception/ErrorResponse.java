package com.bottlen.bottlen_mvc.common.exception;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
public class ErrorResponse {
    private final String code;     // 에러 코드 (예: USER_NOT_FOUND)
    private final String message;  // 설명
    private final int status;      // HTTP 상태 코드
    private final Object data;     // 추가 정보 (예: userId, nickname 등)

    // HttpStatus를 직접 받는 경우 → int로 변환해서 저장
    public static ErrorResponse of(String code, String message, HttpStatus status, Object data) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status.value())
                .data(data)
                .build();
    }
}