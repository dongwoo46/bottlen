package com.bottlen.bottlen_mvc.user.repository;

import com.bottlen.bottlen_mvc.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
