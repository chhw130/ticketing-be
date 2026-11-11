package com.ticketing.member.service;

import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import com.ticketing.global.security.JwtProvider;
import com.ticketing.member.domain.Member;
import com.ticketing.member.dto.LoginRequest;
import com.ticketing.member.dto.LoginResult;
import com.ticketing.member.dto.MemberResponse;
import com.ticketing.member.dto.SignUpRequest;
import com.ticketing.member.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AuthServiceTest {

	@Autowired AuthService authService;
	@Autowired MemberRepository memberRepository;
	@Autowired JwtProvider jwtProvider;

	// 테스트마다 다른 아이디를 써서, 이전 실행에서 지우지 못한 데이터와 겹치지 않게 한다
	private final String loginId = "t" + System.nanoTime() % 1_000_000_000L;
	private final List<Long> createdIds = new ArrayList<>();

	@AfterEach
	void cleanUp() {
		memberRepository.deleteAllById(createdIds);
	}

	@Test
	void 가입하면_비밀번호는_암호화되어_저장된다() {
		MemberResponse response = signUp(loginId, "password123");

		Member saved = memberRepository.findById(response.id()).orElseThrow();
		assertThat(saved.getLoginId()).isEqualTo(loginId);
		assertThat(saved.getPassword()).isNotEqualTo("password123"); // 원문이 그대로 저장되면 안 된다
		assertThat(saved.getPassword()).startsWith("$2");             // BCrypt 해시는 $2a$, $2b$ 등으로 시작한다
		assertThat(saved.isBot()).isFalse();
	}

	@Test
	void 같은_아이디로_가입하면_LOGIN_ID_DUPLICATED() {
		signUp(loginId, "password123");

		assertThatThrownBy(() -> authService.signUp(new SignUpRequest(loginId, "other-pass1", "다른사람")))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.LOGIN_ID_DUPLICATED);
	}

	@Test
	void 로그인에_성공하면_그_회원의_토큰을_돌려준다() {
		MemberResponse member = signUp(loginId, "password123");

		LoginResult result = authService.login(new LoginRequest(loginId, "password123"));

		assertThat(result.member().id()).isEqualTo(member.id());
		// 토큰을 다시 풀면 같은 회원 id가 나와야 한다
		assertThat(jwtProvider.parseMemberId(result.accessToken())).contains(member.id());
	}

	@Test
	void 비밀번호가_틀리면_LOGIN_FAILED() {
		signUp(loginId, "password123");

		assertThatThrownBy(() -> authService.login(new LoginRequest(loginId, "wrong-password")))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.LOGIN_FAILED);
	}

	@Test
	void 없는_아이디도_같은_LOGIN_FAILED() {
		// 아이디가 없을 때와 비밀번호가 틀릴 때 에러가 같아야 "가입된 아이디인지"가 새어 나가지 않는다
		assertThatThrownBy(() -> authService.login(new LoginRequest("no-such-user-xyz", "password123")))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.LOGIN_FAILED);
	}

	// 헬퍼 함수
	private MemberResponse signUp(String loginId, String password) {
		MemberResponse response = authService.signUp(new SignUpRequest(loginId, password, "테스트회원"));
		createdIds.add(response.id());
		return response;
	}
}
