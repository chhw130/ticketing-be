package com.ticketing.concert.dto;

import com.ticketing.concert.domain.Concert;
import com.ticketing.concert.domain.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ConcertCreateRequest(
		@Schema(description = "공연명", example = "2027 봄 콘서트")
		@NotBlank @Size(max = 100) String title,

		@Schema(description = "공연장", example = "올림픽공원 올림픽홀")
		@NotBlank @Size(max = 100) String venue,

		@Schema(description = "장르", example = "CONCERT")
		@NotNull Genre genre,

		@Schema(description = "공연 소개 (선택)", example = "봄맞이 단독 공연")
		String description,

		@Schema(description = "공연 시작일", example = "2027-04-01")
		@NotNull LocalDate startDate,

		@Schema(description = "공연 종료일", example = "2027-04-03")
		@NotNull LocalDate endDate
) {

	public Concert toEntity() {
		return new Concert(title, venue, genre, description, startDate, endDate);
	}

}
