package com.ticketing.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI 상단에 보이는 문서 제목과 설명.
 * 각 API의 내용은 컨트롤러와 DTO의 어노테이션(@Operation, @Schema)을 읽어서 자동으로 채워진다.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("티켓팅 연습 시뮬레이터 API")
						.description("공연 조회, 예매, 대기열 API")
						.version("v0"));
	}

}
