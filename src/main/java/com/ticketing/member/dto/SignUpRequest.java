package com.ticketing.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
		@Schema(example = "tester01")
		@NotBlank
		@Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "영문과 숫자 4~20자여야 합니다.")
		String loginId,

		@Schema(example = "password123")
		@NotBlank
		@Size(min = 8, max = 64, message = "8~64자여야 합니다.")
		String password,

		@Schema(example = "티켓팅연습생")
		@NotBlank
		@Size(max = 15)
		String nickname
) {
}
