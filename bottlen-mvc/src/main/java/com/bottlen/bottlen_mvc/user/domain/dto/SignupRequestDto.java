package com.bottlen.bottlen_mvc.user.domain.dto;


import lombok.Data;

@Data
public class SignupRequestDto {
    private String nickname;
    private String phone;
    private String profileImageUrl;
}