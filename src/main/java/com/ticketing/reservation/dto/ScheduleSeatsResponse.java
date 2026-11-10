package com.ticketing.reservation.dto;

import com.ticketing.concert.domain.Schedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /api/schedules/{scheduleId}/seats 의 응답. 회차 정보 + 좌석 배치도 전체.
 *
 * 좌석(concert)과 예매(reservation)를 둘 다 써야 하는 응답이라 reservation 패키지에 둔다.
 * concert 쪽에 두면 concert가 reservation을 알게 되어 의존 방향(reservation → concert)이 거꾸로 된다.
 */
public record ScheduleSeatsResponse(
		Long scheduleId,
		Long concertId,
		LocalDateTime startAt,
		LocalDateTime ticketOpenAt,
		boolean open, // 지금 예매할 수 있나. 프론트는 false면 좌석을 전부 선택 불가로 그린다
		List<SeatStatusResponse> seats
) {

	public static ScheduleSeatsResponse of(Schedule schedule, boolean open, List<SeatStatusResponse> seats) {
		return new ScheduleSeatsResponse(
				schedule.getId(),
				// LAZY 프록시에서 id만 꺼내는 건 추가 쿼리가 나가지 않는다 (FK 값으로 이미 알고 있음)
				schedule.getConcert().getId(),
				schedule.getStartAt(),
				schedule.getTicketOpenAt(),
				open,
				seats
		);
	}
}
