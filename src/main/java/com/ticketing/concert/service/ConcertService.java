package com.ticketing.concert.service;

import com.ticketing.concert.domain.Concert;
import com.ticketing.concert.dto.ConcertCreateRequest;
import com.ticketing.concert.dto.ConcertResponse;
import com.ticketing.concert.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor // final 필드를 받는 생성자를 만들어 준다 → 생성자 주입
@Transactional(readOnly = true) // 조회 전용 트랜잭션을 기본으로 두고, 쓰기 메서드에만 @Transactional을 따로 붙인다
public class ConcertService {

	private final ConcertRepository concertRepository;

	public List<ConcertResponse> findAll() {
		return concertRepository.findAll().stream()
				.map(ConcertResponse::from)
				.toList();
	}

	public ConcertResponse findById(Long id) {
		Concert concert = concertRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공연을 찾을 수 없습니다. id=" + id));
		return ConcertResponse.from(concert);
	}

	@Transactional
	public ConcertResponse create(ConcertCreateRequest request) {
		Concert saved = concertRepository.save(request.toEntity());
		return ConcertResponse.from(saved);
	}

	// TODO(0단계 연습): update, delete 를 직접 추가해 보기

}
