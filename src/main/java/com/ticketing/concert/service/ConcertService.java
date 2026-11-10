package com.ticketing.concert.service;

import com.ticketing.concert.domain.Concert;
import com.ticketing.concert.dto.ConcertCreateRequest;
import com.ticketing.concert.dto.ConcertResponse;
import com.ticketing.concert.repository.ConcertRepository;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
				.orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND, "id=" + id));
		return ConcertResponse.from(concert);
	}

	@Transactional
	public ConcertResponse create(ConcertCreateRequest request) {
		Concert saved = concertRepository.save(request.toEntity());
		return ConcertResponse.from(saved);
	}

	// 클래스 기본값이 readOnly라서, 쓰기 메서드에는 @Transactional을 따로 붙여야 변경이 DB에 반영된다
	@Transactional
	public ConcertResponse update(Long id, ConcertCreateRequest request){
		Concert concert = concertRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND, "id=" + id));

		//dto타입을 전체 넘겨 받아도 되지만,,, entity가 Dto타입까지 알게 되어버림.
		concert.update(request.title(), request.venue(), request.genre(), request.description(), request.startDate(), request.endDate());

		// save()를 부르지 않아도 트랜잭션이 끝날 때 변경 감지(dirty checking)로 UPDATE가 나간다.
		// concert 객체는 이미 바뀐 값이라 다시 조회할 필요 없이 그대로 DTO로 바꾼다
		return ConcertResponse.from(concert);
	}

	@Transactional
	public void delete(Long id){
		concertRepository.findById(id)
				.orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND, "id=" + id));

		concertRepository.deleteById(id);
	}


}
