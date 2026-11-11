package com.ticketing.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@Schema(example = "tester01") @NotBlank String loginId,
		@Schema(example = "password123") @NotBlank String password
) {
}
