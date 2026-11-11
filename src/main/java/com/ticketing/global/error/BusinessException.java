package com.ticketing.global.error;

import lombok.Getter;

/**
 * Service에서 "업무 규칙상 안 되는 상황"일 때 던지는 예외. (프론트의 throw new ApiError(code, message)와 같은 역할)
 * 어떤 에러인지는 ErrorCode로 정하고, 응답 JSON으로 바꾸는 일은 GlobalExceptionHandler가 한곳에서 한다.
 *
 * RuntimeException을 상속해서 메서드마다 throws를 적지 않아도 되고,
 * @Transactional 안에서 던지면 트랜잭션이 롤백된다.
 */
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	// 기본 메시지 뒤에 상황별 정보를 붙이고 싶을 때 쓴다. 예: "공연을 찾을 수 없습니다. id=3"
	public BusinessException(ErrorCode errorCode, String detail) {
		// detail이 비어 있으면 기본 메시지만 쓴다 (예: 로그인 실패는 아이디를 메시지에 넣지 않는다)
		super(detail == null || detail.isBlank() ? errorCode.getMessage() : errorCode.getMessage() + " " + detail);
		this.errorCode = errorCode;
	}
}
