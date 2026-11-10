package com.ticketing.concert.dto;

import com.ticketing.concert.domain.Schedule;

import java.time.LocalDateTime;

/**
 * 회차 정보만 담은 응답 (공연 상세의 회차 목록 등).
 * 좌석·예매를 몰라도 되는 정보라 concert 패키지에 둔다.
 * 좌석 배치도처럼 예매 여부까지 필요한 응답은 reservation/dto/ScheduleSeatsResponse를 쓴다.
 */
public record ScheduleResponse(
		Long id,
		Long concertId,
		LocalDateTime startAt,
		LocalDateTime ticketOpenAt
) {

	public static ScheduleResponse from(Schedule schedule) {
		return new ScheduleResponse(
				schedule.getId(),
				// LAZY 프록시에서 id만 꺼내는 건 추가 쿼리가 나가지 않는다 (FK 값으로 이미 알고 있음)
				schedule.getConcert().getId(),
				schedule.getStartAt(),
				schedule.getTicketOpenAt()
		);
	}
}
