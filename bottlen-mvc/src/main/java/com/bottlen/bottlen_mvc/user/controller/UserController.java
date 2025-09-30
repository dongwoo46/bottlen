package com.bottlen.bottlen_mvc.user.controller;
import com.bottlen.bottlen_mvc.auth.jwt.JwtUtil;
import com.bottlen.bottlen_mvc.auth.oauth.CustomOAuth2User;
import com.bottlen.bottlen_mvc.user.domain.dto.SignupRequestDto;
import com.bottlen.bottlen_mvc.user.domain.dto.UserResponseDto;
import com.bottlen.bottlen_mvc.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        Long userId = user.getUserId();
        userService.completeSignup(userId, dto);
        return ResponseEntity.ok("추가 정보 입력 완료");
    }


    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @AuthenticationPrincipal Long userId
    ) {
        log.info("currentUserId={}", userId);

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
