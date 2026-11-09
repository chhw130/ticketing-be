package com.ticketing.concert.dto;

import com.ticketing.concert.domain.Concert;
import com.ticketing.concert.domain.Genre;

import java.time.LocalDate;

/**
 * API 응답 형태. 엔티티를 그대로 내보내지 않고 DTO로 바꿔서 내보낸다.
 * (엔티티 구조가 바뀌어도 API 스펙이 흔들리지 않고, 연관관계 무한 참조도 막는다)
 */
public record ConcertResponse(
		Long id,
		String title,
		String venue,
		Genre genre,
		String description,
		LocalDate startDate,
		LocalDate endDate
) {

	public static ConcertResponse from(Concert concert) {
		return new ConcertResponse(
				concert.getId(),
				concert.getTitle(),
				concert.getVenue(),
				concert.getGenre(),
				concert.getDescription(),
				concert.getStartDate(),
				concert.getEndDate()
		);
	}

}
