package com.bottlen.bottlen_mvc.common.exception;


import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Long userId) {
        super("USER_NOT_FOUND",
                "해당 사용자를 찾을 수 없습니다. userId=" + userId,
                HttpStatus.NOT_FOUND,
                userId);
    }
}
