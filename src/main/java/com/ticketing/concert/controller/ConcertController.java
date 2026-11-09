package com.ticketing.concert.controller;

import com.ticketing.concert.dto.ConcertCreateRequest;
import com.ticketing.concert.dto.ConcertResponse;
import com.ticketing.concert.service.ConcertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * HTTP 요청을 받아 Service에 넘기고 결과를 JSON으로 돌려준다.
 * Next.js의 Route Handler(app/api/.../route.ts)와 비슷한 역할이다.
 */
@Tag(name = "공연", description = "공연 조회/등록") // Swagger UI에서 API를 묶는 그룹 이름
@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

	private final ConcertService concertService;

	@Operation(summary = "공연 목록 조회")
	@GetMapping
	public List<ConcertResponse> findAll() {
		return concertService.findAll();
	}

	@Operation(summary = "공연 상세 조회")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "해당 id의 공연이 없음")
	@GetMapping("/{id}")
	public ConcertResponse findById(@Parameter(description = "공연 id", example = "1") @PathVariable Long id) {
		return concertService.findById(id);
	}

	@Operation(summary = "공연 등록")
	@ApiResponse(responseCode = "201", description = "등록 성공. Location 헤더에 새 공연 주소가 담긴다")
	@ApiResponse(responseCode = "400", description = "필수 값 누락, 길이 초과 등 입력값 오류")
	@PostMapping
	public ResponseEntity<ConcertResponse> create(@Valid @RequestBody ConcertCreateRequest request) {
		ConcertResponse created = concertService.create(request);
		return ResponseEntity.created(URI.create("/api/concerts/" + created.id())).body(created);
	}

}
