package com.ticketing.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 서비스에서 일어날 수 있는 에러를 한곳에 모아 둔 목록.
 * 각 값이 HTTP 상태 코드와 기본 메시지를 함께 가진다.
 * 응답의 "code"에는 enum 이름(CONCERT_NOT_FOUND 등)이 그대로 들어가서, 프론트는 메시지 대신 code로 분기하면 된다.
 *
 * 새 에러가 필요하면 여기에 한 줄 추가하고, Service에서 throw new BusinessException(ErrorCode.XXX)로 던진다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// 공통
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
	NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
	DATA_CONFLICT(HttpStatus.CONFLICT, "다른 데이터와 충돌해서 처리할 수 없습니다."),
	INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

	// 공연
	CONCERT_NOT_FOUND(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다."),

	//스케줄
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "스케쥴을 찾을 수 없습니다."),

	// 회원
	LOGIN_ID_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
	LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),

	// 예매
	RESERVATION_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 예매입니다.");



	private final HttpStatus status;
	private final String message;
}
