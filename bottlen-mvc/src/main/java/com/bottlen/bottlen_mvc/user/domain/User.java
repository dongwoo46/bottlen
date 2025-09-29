package com.bottlen.bottlen_mvc.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(columnList = "provider, providerId") // 조회 최적화용 인덱스
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OAuth 제공자 (GOOGLE, NAVER, KAKAO 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    // 제공자 내부 유저 고유 ID
    @Column(nullable = false)
    private String providerId;

    @Column(unique = true, nullable = true)
    private String email;  // 유니크 제거, nullable 허용 가능

    // 닉네임은 추가정보 입력 단계에서 저장
    @Column(unique = true, nullable = true, length = 50)
    private String nickname;

    private String profileImageUrl;

    // 휴대전화번호 (인증 후 저장)
    @Column(unique = true, nullable = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.role == null) {
            this.role = Role.ROLE_USER;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- 도메인 메서드 ---

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateExtraInfo(String nickname, String profileImageUrl, String phone) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.phone = phone;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public static User createOAuthUser(AuthProvider provider, String providerId, String email) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .role(Role.ROLE_USER)
                .build();
    }
}

