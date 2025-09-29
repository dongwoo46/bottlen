package com.bottlen.bottlen_mvc.user.service;

import com.bottlen.bottlen_mvc.infrastructure.redis.RedisService;
import com.bottlen.bottlen_mvc.user.domain.User;
import com.bottlen.bottlen_mvc.user.domain.dto.SignupRequestDto;
import com.bottlen.bottlen_mvc.user.domain.dto.UserResponseDto;
import com.bottlen.bottlen_mvc.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RedisService redisService;

    /**
     * 회원가입 추가정보 입력
     */
    public void completeSignup(Long userId, SignupRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 닉네임 중복 체크
        if (userRepository.findByNickname(dto.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        // 추가 정보 저장
        user.updateExtraInfo(dto.getNickname(), dto.getProfileImageUrl(), dto.getPhone());
        userRepository.save(user);

    }

    public UserResponseDto findById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> new UserResponseDto(user.getId(), user.getNickname(), user.getEmail()))
                .orElse(null);
    }

    /**
     * 인증 코드 발송 (랜덤 6자리)
     */
    public void sendVerificationCode(String phone) {
        String code = String.format("%06d", new Random().nextInt(999999));

        // 실제 환경에서는 SMS API 연동 필요 (예: Naver SENS, Twilio 등)
        System.out.println("DEBUG: 인증코드 " + code + " 발송 (phone=" + phone + ")");

        // Redis에 저장 (5분간 유효)
        String redisKey = "phone:code:" + phone;
        redisService.set(redisKey, code, Duration.ofMinutes(5));
    }

    /**
     * 인증 코드 검증
     */
    public boolean verifyCode(String phone, String code) {
        String redisKey = "phone:code:" + phone;
        String savedCode = redisService.get(redisKey);

        if (savedCode != null && savedCode.equals(code)) {
            // 성공 → 인증 완료 플래그 저장 (10분 유효)
            redisService.set("phone:verified:" + phone, "true", Duration.ofMinutes(10));
            redisService.delete(redisKey); // 인증코드 제거
            return true;
        }
        return false;
    }
}
