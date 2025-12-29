package com.kobe.pokekernle.global.config.rate;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * packageName    : com.kobe.pokekernle.global.config.rate
 * fileName       : RateLimitService
 * author         : kobe
 * date           : 2025. 12. 29.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 29.        kobe       최초 생성
 */

@Service
public class RateLimitService {

    // IP별 버킷 저장 (In-Memory)
    private final Map<String, Bucket> ipBucket = new ConcurrentHashMap<>();

    // 이메일별 버킷 저장
    private final Map<String, Bucket> emailBucket = new ConcurrentHashMap<>();

    /**
     * IP 기반 Rate Limit 채크
     * @param ip IP 주소
     * @return true면 허용, false면 제한 초과
     */
    public boolean tryConsumeByIp(String ip) {
        String key = "register:ip:" + ip;
        Bucket bucket = ipBucket.computeIfAbsent(key, k -> createIpBucket());
        return bucket.tryConsume(1);
    }

    /**
     * 이메일 기반 Rate Limit 체크
     * @param
     * @return true면 허용, false면 제한 초과
     */
    public boolean tryConsumeByEmail(String email) {
        String key = "register:email:" + email.toLowerCase(); // 이메일은 소문자로 정규화
        Bucket bucket = emailBucket.computeIfAbsent(key, k -> createEmailBucket());
        return bucket.tryConsume(1);
    }

    /**
     * IP 기반 버킷 생성: 시간당 10회 제한
     */
    private Bucket createIpBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofHours(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * 이메일 기반 버킷 생성: 시간당 3회 제한 (더 엄격)
     */
    private Bucket createEmailBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(3)
                .refillIntervally(3, Duration.ofHours(1))
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * 버킷 제거 (메모리 정리용)
     */
    public void resetIpBucket(String ip) {
        ipBucket.remove("register:ip:" + ip);
    }

    public void resetEmailBucket(String email) {
        emailBucket.remove("register:email:" + email.toLowerCase());
    }
}
