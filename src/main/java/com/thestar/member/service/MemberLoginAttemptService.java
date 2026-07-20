package com.thestar.member.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 使用 Redis 紀錄會員登入失敗次數與暫時鎖定狀態。
 * 不需要修改會員資料表，登入成功後會清除該信箱的失敗紀錄。
 */
@Service
public class MemberLoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(MemberLoginAttemptService.class);

    private final StringRedisTemplate redisTemplate;
    private final int maxFailedAttempts;
    private final Duration attemptWindow;
    private final Duration lockDuration;

    public MemberLoginAttemptService(
            StringRedisTemplate redisTemplate,
            @Value("${app.member.login.max-failed-attempts:5}") int maxFailedAttempts,
            @Value("${app.member.login.attempt-window-minutes:10}") long attemptWindowMinutes,
            @Value("${app.member.login.lock-minutes:10}") long lockMinutes) {
        this.redisTemplate = redisTemplate;
        this.maxFailedAttempts = Math.max(1, maxFailedAttempts);
        this.attemptWindow = Duration.ofMinutes(Math.max(1, attemptWindowMinutes));
        this.lockDuration = Duration.ofMinutes(Math.max(1, lockMinutes));
    }

    /**
     * @return 大於 0 代表仍在鎖定中，數值為剩餘秒數；0 代表可嘗試登入。
     */
    public long getRemainingLockSeconds(String email) {
        try {
            Long remainingSeconds = redisTemplate.getExpire(lockKey(email));
            return normalizeRemainingSeconds(remainingSeconds);
        } catch (DataAccessException e) {
            // Redis 暫時無法使用時不阻斷正常登入，避免整個登入功能故障。
            log.warn("無法讀取會員登入鎖定狀態，暫時略過限制：{}", e.getMessage());
            return 0L;
        }
    }

    public LoginFailureResult recordFailure(String email) {
        try {
            String failureKey = failureKey(email);
            Long failedAttempts = redisTemplate.opsForValue().increment(failureKey);

            if (failedAttempts == null) {
                return LoginFailureResult.unavailable();
            }

            if (failedAttempts == 1L) {
                redisTemplate.expire(failureKey, attemptWindow);
            }

            if (failedAttempts >= maxFailedAttempts) {
                redisTemplate.delete(failureKey);
                redisTemplate.opsForValue().set(lockKey(email), "1", lockDuration);

                return new LoginFailureResult(
                        true,
                        0,
                        lockDuration.toSeconds(),
                        true
                );
            }

            return new LoginFailureResult(
                    false,
                    maxFailedAttempts - failedAttempts.intValue(),
                    0L,
                    true
            );
        } catch (DataAccessException e) {
            log.warn("無法寫入會員登入失敗紀錄，暫時略過限制：{}", e.getMessage());
            return LoginFailureResult.unavailable();
        }
    }

    public void clear(String email) {
        try {
            redisTemplate.delete(List.of(failureKey(email), lockKey(email)));
        } catch (DataAccessException e) {
            log.warn("無法清除會員登入失敗紀錄：{}", e.getMessage());
        }
    }

    private long normalizeRemainingSeconds(Long remainingSeconds) {
        if (remainingSeconds == null || remainingSeconds < 1L) {
            return 0L;
        }
        return remainingSeconds;
    }

    private String failureKey(String email) {
        return "member:login:failures:" + normalizeEmail(email);
    }

    private String lockKey(String email) {
        return "member:login:locked:" + normalizeEmail(email);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public record LoginFailureResult(
            boolean locked,
            int remainingAttempts,
            long lockSeconds,
            boolean trackingAvailable) {

        private static LoginFailureResult unavailable() {
            return new LoginFailureResult(false, 0, 0L, false);
        }
    }
}
