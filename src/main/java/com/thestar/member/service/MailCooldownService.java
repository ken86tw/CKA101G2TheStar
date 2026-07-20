package com.thestar.member.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MailCooldownService {

    // 同一信箱、同一寄信類型，60 秒內只能寄一次
    private static final Duration COOLDOWN =
            Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    public MailCooldownService(
            StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 嘗試取得寄信資格。
     *
     * @return 0 代表可以寄信，大於 0 代表剩餘等待秒數
     */
    public long acquire(String email, String mailType) {
        String key = buildKey(email, mailType);

        Boolean acquired = redisTemplate
                .opsForValue()
                .setIfAbsent(
                        key,
                        "1",
                        COOLDOWN
                );

        if (Boolean.TRUE.equals(acquired)) {
            return 0L;
        }

        Long remainingSeconds =
                redisTemplate.getExpire(key);

        if (remainingSeconds == null
                || remainingSeconds < 1) {
            return 1L;
        }

        return remainingSeconds;
    }

    /**
     * 寄信失敗時，刪除冷卻紀錄，
     * 讓使用者可以立即重新嘗試。
     */
    public void release(
            String email,
            String mailType) {

        redisTemplate.delete(
                buildKey(email, mailType)
        );
    }

    private String buildKey(
            String email,
            String mailType) {

        String normalizedEmail =
                email == null
                        ? ""
                        : email.trim().toLowerCase();

        String normalizedType =
                mailType == null
                        ? "MAIL"
                        : mailType.trim().toUpperCase();

        return "mail:cooldown:"
                + normalizedType
                + ":"
                + normalizedEmail;
    }
}