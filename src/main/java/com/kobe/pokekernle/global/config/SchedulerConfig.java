package com.kobe.pokekernle.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * packageName    : com.kobe.pokekernle.global.config
 * fileName       : SchedulerConfig
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // 스케줄러 관련 세부 설정(스레드 풀 등)이 필요하면 여기서 설정
}
