package com.bottlen.bottlen_mvc.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateNicknameException extends BusinessException {
    public DuplicateNicknameException(String nickname) {
        super("DUPLICATE_NICKNAME",
                "이미 사용 중인 닉네임입니다.",
                HttpStatus.CONFLICT,
                nickname);
    }
}
