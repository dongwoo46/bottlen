package com.bottlen.bottlen_mvc.user.controller;
import com.bottlen.bottlen_mvc.auth.jwt.JwtUtil;
import com.bottlen.bottlen_mvc.user.domain.dto.SignupRequestDto;
import com.bottlen.bottlen_mvc.user.domain.dto.UserResponseDto;
import com.bottlen.bottlen_mvc.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 추가 정보 입력 (닉네임, 전화번호 등)
     */
    @PostMapping("/complete-signup")
    public ResponseEntity<String> completeSignup(
            @RequestBody SignupRequestDto dto,
            Authentication authentication
    ) {
        // JwtAuthenticationFilter에서 userId를 Principal에 넣었다고 가정
        Long userId = (Long) authentication.getPrincipal();

        // 서비스 호출 → DB 업데이트
        userService.completeSignup(userId, dto);

        return ResponseEntity.ok("추가 정보 입력 완료");
    }

    /**
     * 닉네임으로 유저 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal(); // principal을 userId로 넣었음

        log.info("authentication.getPrincipal()={}", authentication.getPrincipal());

        UserResponseDto user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("Authorization".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
