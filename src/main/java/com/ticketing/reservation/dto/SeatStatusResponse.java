package com.ticketing.reservation.dto;

import com.ticketing.concert.domain.Seat;
import com.ticketing.concert.domain.SeatGrade;

/**
 * 좌석 배치도의 좌석 하나.
 * 좌석 정보(번호, 등급, 가격)는 Seat에서, 상태는 Service가 예매 기록으로 계산해서 넘겨준다.
 */
public record SeatStatusResponse(
		Long seatId,
		Integer seatNumber,
		SeatGrade grade,
		Long price,
		SeatDisplayStatus status
) {

	public static SeatStatusResponse of(Seat seat, SeatDisplayStatus status) {
		return new SeatStatusResponse(
				seat.getId(),
				seat.getSeatNumber(),
				seat.getSeatGrade(),
				seat.getPrice(),
				status
		);
	}
}
