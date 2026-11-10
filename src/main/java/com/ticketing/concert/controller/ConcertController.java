package com.ticketing.concert.controller;

import com.ticketing.concert.dto.ConcertCreateRequest;
import com.ticketing.concert.dto.ConcertResponse;
import com.ticketing.concert.dto.ScheduleResponse;
import com.ticketing.concert.service.ConcertService;
import com.ticketing.concert.service.ScheduleService;
import com.ticketing.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * HTTP 요청을 받아 Service에 넘기고 결과를 JSON으로 돌려준다.
 * Next.js의 Route Handler(app/api/.../route.ts)와 비슷한 역할이다.
 *
 * 성공 응답은 모두 ApiResult로 감싸 { "message": ..., "data": ... } 모양으로 통일한다.
 * 참고: @ApiResponse의 description은 Swagger 문서에만 보이고 실제 응답에는 들어가지 않는다.
 */
@Tag(name = "공연", description = "공연 조회/등록/수정") // Swagger UI에서 API를 묶는 그룹 이름
@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

	private final ConcertService concertService;
	private final ScheduleService scheduleService; // 공연의 회차 목록을 위해. 같은 concert 패키지라 의존 방향 문제가 없다

	@Operation(summary = "공연 목록 조회")
	@ApiResponse(responseCode = "200", description = "공연 리스트 조회")
	@ApiResponse(responseCode = "204", description = "공연이 존재하지 않습니다.")
	@GetMapping
	public ApiResult<List<ConcertResponse>> findAll() {
		return ApiResult.of("조회 성공", concertService.findAll());
	}

	@Operation(summary = "공연 상세 조회")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "해당 id의 공연이 없음")
	@GetMapping("/{id}")
	public ApiResult<ConcertResponse> findById(@Parameter(description = "공연 id", example = "1") @PathVariable Long id) {
		return ApiResult.of("조회 성공", concertService.findById(id));
	}

	@Operation(summary = "공연의 회차 목록 조회", description = "그 공연의 회차를 시작 시각 순으로 돌려준다")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "해당 id의 공연이 없음")
	@GetMapping("/{id}/schedules")
	public ApiResult<List<ScheduleResponse>> findSchedules(@Parameter(description = "공연 id", example = "1") @PathVariable Long id) {
		return ApiResult.of("조회 성공", scheduleService.findByConcertId(id));
	}

	@Operation(summary = "공연 수정")
	@ApiResponse(responseCode = "200", description = "수정 성공")
	@ApiResponse(responseCode = "400", description = "필수 값 누락, 길이 초과 등 입력값 오류")
	@ApiResponse(responseCode = "404", description = "해당 id의 공연이 없음")
	@PutMapping("/{id}")
	public ApiResult<ConcertResponse> update(@Parameter(description = "공연 id", example = "1") @PathVariable Long id,
											 @Valid @RequestBody ConcertCreateRequest request) {
		return ApiResult.of("수정 성공", concertService.update(id, request));
	}

	@Operation(summary = "공연 등록")
	@ApiResponse(responseCode = "201", description = "등록 성공.")
	@ApiResponse(responseCode = "400", description = "필수 값 누락, 길이 초과 등 입력값 오류")
	@PostMapping
	public ResponseEntity<ApiResult<ConcertResponse>> create(@Valid @RequestBody ConcertCreateRequest request) {
		ConcertResponse created = concertService.create(request);
		// 201 상태 코드와 Location 헤더가 필요해서 ResponseEntity로 한 번 더 감싼다
		return ResponseEntity.created(URI.create("/api/concerts/" + created.id()))
				.body(ApiResult.of("등록 성공", created));
	}

	@Operation(summary = "공연 삭제")
	@ApiResponse(responseCode = "200", description = "삭제 성공")
	@ApiResponse(responseCode = "404", description = "존재하지 않는 ID입니다.")
	@DeleteMapping("/{id}")
	public ApiResult<Long> delete(@PathVariable Long id) {
		concertService.delete(id);
		return ApiResult.of("공연 삭제", id);
	}
}
