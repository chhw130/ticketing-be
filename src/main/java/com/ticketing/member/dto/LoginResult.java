package com.ticketing.member.dto;

// Service → Controller로 넘기는 로그인 결과. 토큰은 Controller가 쿠키에 담고, 응답 본문에는 회원 정보만 보낸다
public record LoginResult(MemberResponse member, String accessToken) {
}
