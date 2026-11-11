package com.ticketing.member.controller;

import com.ticketing.global.common.ApiResult;
import com.ticketing.global.security.AuthCookie;
import com.ticketing.member.dto.LoginRequest;
import com.ticketing.member.dto.LoginResult;
import com.ticketing.member.dto.MemberResponse;
import com.ticketing.member.dto.SignUpRequest;
import com.ticketing.member.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "회원가입 / 로그인 / 로그아웃 / 내 정보")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final AuthCookie authCookie;

	@Operation(summary = "회원가입")
	@ApiResponse(responseCode = "201", description = "가입 성공")
	@ApiResponse(responseCode = "400", description = "입력값 오류")
	@ApiResponse(responseCode = "409", description = "이미 사용 중인 아이디")
	@PostMapping("/auth/signup")
	public ResponseEntity<ApiResult<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.of("가입 성공", authService.signUp(request)));
	}

	// 토큰은 응답 본문이 아니라 Set-Cookie 헤더로 보낸다. 브라우저가 HttpOnly 쿠키로 저장하고 이후 요청마다 자동으로 붙인다
	@Operation(summary = "로그인", description = "성공하면 access_token 쿠키(HttpOnly)가 설정된다")
	@ApiResponse(responseCode = "200", description = "로그인 성공")
	@ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호가 틀림")
	@PostMapping("/auth/login")
	public ResponseEntity<ApiResult<MemberResponse>> login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = authService.login(request);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, authCookie.create(result.accessToken()).toString())
				.body(ApiResult.of("로그인 성공", result.member()));
	}

	// JWT는 서버에 저장하지 않으니, 로그아웃은 브라우저의 쿠키를 지우는 것이다
	@Operation(summary = "로그아웃")
	@PostMapping("/auth/logout")
	public ResponseEntity<ApiResult<Void>> logout() {
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, authCookie.expire().toString())
				.body(ApiResult.of("로그아웃 성공", null));
	}

	// 로그인이 필요한 API. 쿠키가 없거나 틀리면 SecurityConfig가 컨트롤러에 오기 전에 401로 막는다
	@Operation(summary = "내 정보 조회")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "401", description = "로그인 필요")
	@GetMapping("/members/me")
	public ApiResult<MemberResponse> me(@AuthenticationPrincipal Long memberId) {
		return ApiResult.of("조회 성공", authService.findMe(memberId));
	}
}
