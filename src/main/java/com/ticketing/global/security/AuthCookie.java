package com.ticketing.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 토큰을 담는 쿠키를 만든다. 쿠키 옵션을 한곳에 모아 로그인·로그아웃이 같은 설정을 쓰게 한다.
 * - HttpOnly: 브라우저 JS(document.cookie)로 읽을 수 없다 → XSS로 토큰이 털리지 않는다
 * - SameSite=Lax: 다른 사이트에서 보낸 POST 요청에는 쿠키가 붙지 않는다 → CSRF를 막는다
 * - Path=/: 모든 경로의 요청에 붙는다
 */
@Component
@RequiredArgsConstructor
public class AuthCookie {

	public static final String NAME = "access_token";

	private final JwtProperties properties;

	public ResponseCookie create(String token) {
		return base(token).maxAge(properties.expiration()).build();
	}

	// 로그아웃: 같은 이름의 쿠키를 만료 시간 0으로 덮어써서 브라우저가 지우게 한다
	public ResponseCookie expire() {
		return base("").maxAge(Duration.ZERO).build();
	}

	private ResponseCookie.ResponseCookieBuilder base(String value) {
		return ResponseCookie.from(NAME, value)
				.httpOnly(true)
				.secure(properties.cookieSecure())
				.sameSite("Lax")
				.path("/");
	}
}
