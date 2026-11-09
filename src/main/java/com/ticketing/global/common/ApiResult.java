package com.ticketing.global.common;

/**
 * 모든 성공 응답을 { "message": ..., "data": ... } 모양으로 감싸는 공통 래퍼.
 * 응답 모양이 하나로 통일되면 프론트에서 어떤 API든 res.data로 꺼낼 수 있다.
 *
 * <T>는 TS 제네릭과 같다. ApiResult<ConcertResponse>는 TS의
 * type ApiResult<T> = { message: string; data: T } 에 ConcertResponse를 넣은 것.
 *
 * 이름을 ApiResponse로 짓지 않은 이유: Swagger의 @ApiResponse 어노테이션과 이름이 겹쳐 import가 꼬인다.
 */
public record ApiResult<T>(String message, T data) {

	// new ApiResult<>(...) 대신 ApiResult.of(...)로 짧게 만들기 위한 정적 팩토리 메서드
	public static <T> ApiResult<T> of(String message, T data) {
		return new ApiResult<>(message, data);
	}
}
