package com.ticketing.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 모든 컨트롤러에서 던져진 예외를 한곳에서 받아 ErrorResponse 모양으로 바꾼다.
 * 프론트의 axios interceptor나 Error Boundary처럼, 각 API마다 try/catch를 쓰지 않게 해 준다.
 *
 * 메시지를 "정하는" 곳은 Service(ErrorCode 선택)이고, 여기서는 응답 "모양"만 통일한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// Service에서 직접 던진 업무 예외 (없는 공연 등)
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
		ErrorCode errorCode = e.getErrorCode();
		return ResponseEntity.status(errorCode.getStatus())
				.body(ErrorResponse.of(errorCode, e.getMessage()));
	}

	// @Valid 검증 실패. 컨트롤러 메서드에 들어가기 "전에" 터져서 컨트롤러의 try/catch로는 잡을 수 없다
	// 예: "title: 공백일 수 없습니다, venue: 공백일 수 없습니다"
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(this::formatFieldError)
				.collect(Collectors.joining(", "));
		return badRequest(message);
	}

	// JSON 문법이 틀렸거나, enum에 없는 값(genre: "ROCK")이나 잘못된 날짜를 보냈을 때
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
		return badRequest("요청 본문(JSON)을 읽을 수 없습니다. 형식과 값을 확인하세요.");
	}

	// 경로 변수 타입이 틀렸을 때. 예: /api/concerts/abc
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		return badRequest(e.getName() + " 값이 올바르지 않습니다: " + e.getValue());
	}

	// 없는 URL. 아래의 Exception 처리에 걸려 500이 되지 않도록 따로 404로 돌려준다
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
		return toResponse(ErrorCode.NOT_FOUND);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		return toResponse(ErrorCode.METHOD_NOT_ALLOWED);
	}

	// DB 제약 위반. 예: 회차가 달린 공연 삭제(FK), 2단계의 중복 예매(UNIQUE)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
		log.warn("DB 제약 위반: {}", e.getMostSpecificCause().getMessage());
		return toResponse(ErrorCode.DATA_CONFLICT);
	}

	// 위에서 처리하지 못한 모든 예외. 원인은 로그에만 남기고, 응답에는 내부 정보를 노출하지 않는다
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
		log.error("처리하지 못한 예외", e);
		return toResponse(ErrorCode.INTERNAL_ERROR);
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}

	private ResponseEntity<ErrorResponse> badRequest(String message) {
		return ResponseEntity.status(ErrorCode.INVALID_INPUT.getStatus())
				.body(ErrorResponse.of(ErrorCode.INVALID_INPUT, message));
	}

	private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode) {
		return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode));
	}
}
