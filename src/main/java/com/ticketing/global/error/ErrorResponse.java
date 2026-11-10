package com.ticketing.global.error;

/**
 * 모든 에러 응답의 모양: { "code": "CONCERT_NOT_FOUND", "message": "공연을 찾을 수 없습니다. id=3" }
 * 성공 응답(ApiResult)과 짝을 이룬다. 프론트는 res.ok가 false면 이 모양으로 읽으면 된다.
 */
public record ErrorResponse(String code, String message) {

	public static ErrorResponse of(ErrorCode errorCode, String message) {
		return new ErrorResponse(errorCode.name(), message);
	}

	public static ErrorResponse of(ErrorCode errorCode) {
		return of(errorCode, errorCode.getMessage());
	}
}
