package com.ticketing.global.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Spring을 띄우지 않는 순수 단위 테스트. JwtProvider는 설정값만 있으면 혼자 동작해서 new로 만든다 (빠르다)
class JwtProviderTest {

	private static final String SECRET = "test-secret-key-for-unit-test-0123456789";

	private final JwtProvider jwtProvider = new JwtProvider(new JwtProperties(SECRET, Duration.ofHours(1), false));

	@Test
	void 만든_토큰을_풀면_회원_id가_나온다() {
		String token = jwtProvider.createToken(42L);

		assertThat(jwtProvider.parseMemberId(token)).contains(42L);
	}

	@Test
	void 내용을_조작한_토큰은_거부한다() {
		String token = jwtProvider.createToken(42L);
		// JWT는 "헤더.내용.서명" 구조. 서명 부분의 글자 하나를 바꾸면 검증에 실패해야 한다
		String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "BB" : "AA");

		assertThat(jwtProvider.parseMemberId(tampered)).isEmpty();
	}

	@Test
	void 다른_키로_서명한_토큰은_거부한다() {
		JwtProvider other = new JwtProvider(new JwtProperties("another-secret-key-0123456789-abcdefgh", Duration.ofHours(1), false));
		String token = other.createToken(42L);

		assertThat(jwtProvider.parseMemberId(token)).isEmpty();
	}

	@Test
	void 만료된_토큰은_거부한다() {
		JwtProvider expired = new JwtProvider(new JwtProperties(SECRET, Duration.ofSeconds(-1), false));
		String token = expired.createToken(42L);

		assertThat(jwtProvider.parseMemberId(token)).isEmpty();
	}

	@Test
	void 형식이_이상한_값은_거부한다() {
		assertThat(jwtProvider.parseMemberId("not-a-jwt")).isEmpty();
	}

	@Test
	void 키가_32바이트보다_짧으면_만들_때_실패한다() {
		assertThatThrownBy(() -> new JwtProvider(new JwtProperties("short", Duration.ofHours(1), false)))
				.isInstanceOf(RuntimeException.class);
	}
}
