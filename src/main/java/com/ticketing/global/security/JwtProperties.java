package com.ticketing.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * application.yml의 jwt.* 값을 묶어 받는 설정 객체. (프론트의 process.env를 타입이 있는 객체로 받는 것과 비슷)
 *
 * @param secret       토큰 서명 키. HS256이라 32바이트(영문 32자) 이상이어야 한다. 운영에서는 환경 변수 JWT_SECRET으로 바꾼다
 * @param expiration   토큰 유효 시간. 쿠키 만료 시간도 같게 맞춘다
 * @param cookieSecure true면 https에서만 쿠키를 보낸다. 로컬(http)에서는 false여야 쿠키가 저장된다
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, Duration expiration, boolean cookieSecure) {
}
