package com.ticketing.reservation.controller;

import com.ticketing.global.common.ApiResult;
import com.ticketing.reservation.dto.ScheduleSeatsResponse;
import com.ticketing.reservation.service.SeatStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "좌석", description = "회차별 좌석 현황")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class SeatStatusController {

	private final SeatStatusService seatStatusService;

	@Operation(summary = "회차별 좌석 현황 조회", description = "좌석마다 AVAILABLE(선택 가능) / SOLD(판매 완료)를 계산해서 돌려준다. open이 false면 아직 예매 오픈 전이다")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "해당 id의 회차가 없음")
	@GetMapping("/{id}/seats")
	public ApiResult<ScheduleSeatsResponse> findSeats(@Parameter(description = "회차 id", example = "15") @PathVariable Long id) {
		return ApiResult.of("조회 성공", seatStatusService.findSeats(id));
	}
}
