package com.ticketing.global.security;

import com.ticketing.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * 어떤 URL이 로그인이 필요한지, 인증을 어떤 방식으로 하는지 정한다.
 * 기본값은 "로그인 필요"이고, 로그인 없이 열어 둘 URL만 permitAll로 나열한다 (빠뜨려도 열리지 않는 쪽이 안전하다).
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtProvider jwtProvider) throws Exception {
		http
				// 세션 대신 JWT 쿠키로 인증한다 → 서버가 로그인 상태를 저장하지 않는다
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Spring 기본 로그인 폼·브라우저 팝업 인증은 쓰지 않는다 (로그인은 /api/auth/login JSON API로)
				.formLogin(form -> form.disable())
				.httpBasic(basic -> basic.disable())
				// CSRF 토큰은 끈다. 쿠키가 SameSite=Lax라 다른 사이트에서 보낸 POST에는 쿠키가 붙지 않고,
				// 프론트는 rewrites로 같은 출처에서만 호출하기 때문이다
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/**").permitAll()
						// 0단계 공연 API는 지금처럼 로그인 없이 쓴다 (관리자 권한은 아직 없음)
						.requestMatchers("/api/concerts/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/schedules/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
						// 에러 응답을 그리는 경로. 막으면 진짜 에러 대신 401이 나간다
						.requestMatchers("/error").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(e -> e.authenticationEntryPoint(unauthorizedEntryPoint()))
				// 아이디/비밀번호 필터보다 먼저 JWT 쿠키를 읽는다
				.addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	// 비밀번호 암호화. BCrypt는 같은 비밀번호도 매번 다른 값으로 암호화하고(salt), 일부러 느리게 만들어 무차별 대입을 어렵게 한다
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// 로그인이 필요한 URL에 토큰 없이 오면 401 + 공통 에러 모양 { code, message }로 응답한다
	// (이 단계는 컨트롤러에 도착하기 전이라 GlobalExceptionHandler가 잡지 못해서 따로 둔다)
	private AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, exception) -> {
			ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.getWriter().write(
					"{\"code\":\"" + errorCode.name() + "\",\"message\":\"" + errorCode.getMessage() + "\"}");
		};
	}
}
