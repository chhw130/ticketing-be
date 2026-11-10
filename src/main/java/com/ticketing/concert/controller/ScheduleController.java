package com.ticketing.concert.controller;

import com.ticketing.concert.dto.ScheduleResponse;
import com.ticketing.concert.service.ScheduleService;
import com.ticketing.global.common.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회차 하나를 자기 id로 가리키는 API.
 * 공연의 회차 "목록"은 부모 아래 경로(GET /api/concerts/{concertId}/schedules)라서 ConcertController에 있다.
 * 규칙: 목록은 부모 아래에 한 단계만 중첩하고, 하나를 가리킬 때는 자기 id로 접근한다.
 */
@Tag(name = "회차", description = "공연 회차 조회") // Swagger UI에서 API를 묶는 그룹 이름
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

	private final ScheduleService scheduleService;

	@Operation(summary = "회차 상세 조회")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "해당 id의 회차가 없음")
	@GetMapping("/{id}")
	public ApiResult<ScheduleResponse> findById(@Parameter(description = "회차 id", example = "1") @PathVariable Long id) {
		return ApiResult.of("조회 성공", scheduleService.findById(id));
	}
}
