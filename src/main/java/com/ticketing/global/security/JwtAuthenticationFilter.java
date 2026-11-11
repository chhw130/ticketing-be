package com.ticketing.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 모든 요청에서 쿠키의 JWT를 꺼내 검증하고, 유효하면 "이 요청은 회원 N번"이라고 Spring Security에 알려 준다.
 * (프론트의 axios 요청 인터셉터를 서버 입구에 둔 것과 비슷하다)
 *
 * 토큰이 없거나 틀려도 여기서 막지 않는다. 막는 건 SecurityConfig의 규칙(로그인 필요한 URL인가)이 한다.
 * 그래야 공연 목록처럼 로그인 없이 보는 API는 쿠키가 깨져 있어도 그대로 동작한다.
 *
 * @Component를 붙이지 않는다: 붙이면 Spring Boot가 일반 서블릿 필터로도 한 번 더 등록해서 요청마다 두 번 돈다.
 * SecurityConfig에서 직접 만들어 Security 필터 체인에만 넣는다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		readToken(request)
				.flatMap(jwtProvider::parseMemberId)
				.ifPresent(memberId -> {
					// principal 자리에 회원 id를 넣는다 → 컨트롤러에서 @AuthenticationPrincipal Long memberId로 꺼낸다
					var authentication = new UsernamePasswordAuthenticationToken(memberId, null, List.of());
					SecurityContextHolder.getContext().setAuthentication(authentication);
				});
		chain.doFilter(request, response);
	}

	private Optional<String> readToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
				.filter(cookie -> AuthCookie.NAME.equals(cookie.getName()))
				.map(Cookie::getValue)
				.filter(value -> !value.isBlank())
				.findFirst();
	}
}
