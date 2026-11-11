package com.ticketing.global.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * JWT를 만들고 검증한다.
 * JWT = "누구인지(회원 id)"를 담은 문자열 + 서버만 아는 키로 만든 서명.
 * 서명 덕분에 누군가 내용을 바꾸면 검증에서 바로 걸린다. 서버는 세션을 저장하지 않아도 된다 (stateless)
 */
@Component
public class JwtProvider {

	private final SecretKey key;
	private final JwtProperties properties;

	public JwtProvider(JwtProperties properties) {
		this.properties = properties;
		// 키가 32바이트보다 짧으면 여기서 바로 예외가 나서, 약한 키로 서버가 뜨는 것을 막는다
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String createToken(Long memberId) {
		Date now = new Date();
		return Jwts.builder()
				.subject(String.valueOf(memberId)) // sub: 토큰 주인 (회원 id)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + properties.expiration().toMillis()))
				.signWith(key)
				.compact();
	}

	// 서명이 틀렸거나, 만료됐거나, 형식이 이상하면 빈 값을 돌려준다
	public Optional<Long> parseMemberId(String token) {
		try {
			String subject = Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject();
			return Optional.of(Long.valueOf(subject));
		} catch (JwtException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
