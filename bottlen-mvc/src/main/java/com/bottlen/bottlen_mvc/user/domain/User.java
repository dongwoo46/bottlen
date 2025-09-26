package com.bottlen.bottlen_mvc.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "providerId"})
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

    @Column(unique = true)
    private String email;

    private String nickname;

    private String profileImageUrl;

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

    /**
     * 프로필 정보 변경
     */
    public void updateProfile(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }

    /**
     * 역할 변경
     */
    public void changeRole(Role role) {
        this.role = role;
    }

    /**
     * 소셜 로그인 신규 사용자 팩토리
     */
    public static User createOAuthUser(AuthProvider provider, String providerId, String email, String nickname) {
        return User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .role(Role.ROLE_USER)
                .build();
    }
}
