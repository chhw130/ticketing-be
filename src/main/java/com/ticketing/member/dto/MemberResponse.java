package com.ticketing.member.dto;

import com.ticketing.member.domain.Member;

// 비밀번호는 암호화된 값이라도 응답에 넣지 않는다
public record MemberResponse(Long id, String loginId, String nickname) {

	public static MemberResponse from(Member member) {
		return new MemberResponse(member.getId(), member.getLoginId(), member.getNickname());
	}
}
