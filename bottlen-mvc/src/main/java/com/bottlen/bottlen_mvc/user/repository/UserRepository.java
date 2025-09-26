package com.bottlen.bottlen_mvc.user.repository;

import com.bottlen.bottlen_mvc.user.domain.AuthProvider;
import com.bottlen.bottlen_mvc.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // JPA 쿼리 메서드: provider와 providerId로 사용자 조회
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
