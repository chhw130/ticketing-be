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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	@Transactional
	public MemberResponse signUp(SignUpRequest request) {
		// 미리 확인해서 친절한 에러를 준다. 동시에 같은 아이디로 가입하면 이 확인을 둘 다 통과할 수 있는데,
		// 그때는 DB의 UNIQUE 제약이 막는다 (GlobalExceptionHandler가 409 DATA_CONFLICT로 응답)
		if (memberRepository.existsByLoginId(request.loginId())) {
			throw new BusinessException(ErrorCode.LOGIN_ID_DUPLICATED, "loginId=" + request.loginId());
		}
		Member member = Member.signUp(request.loginId(), passwordEncoder.encode(request.password()), request.nickname());
		return MemberResponse.from(memberRepository.save(member));
	}

	public LoginResult login(LoginRequest request) {
		// 아이디가 없을 때와 비밀번호가 틀렸을 때 같은 에러를 준다.
		// 다르게 주면 "이 아이디는 가입돼 있다"는 정보가 새어 나간다
		Member member = memberRepository.findByLoginId(request.loginId())
				.filter(m -> m.getPassword() != null && passwordEncoder.matches(request.password(), m.getPassword()))
				.orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED, ""));
		return new LoginResult(MemberResponse.from(member), jwtProvider.createToken(member.getId()));
	}

	public MemberResponse findMe(Long memberId) {
		return memberRepository.findById(memberId)
				.map(MemberResponse::from)
				// 토큰은 유효한데 회원이 지워진 경우
				.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "id=" + memberId));
	}
}
