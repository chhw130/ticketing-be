package com.ticketing.concert.service;

import com.ticketing.concert.domain.Genre;
import com.ticketing.concert.dto.ConcertCreateRequest;
import com.ticketing.concert.dto.ConcertResponse;
import com.ticketing.concert.repository.ConcertRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ConcertServiceTest {

	@Autowired
	ConcertRepository concertRepository;

	@Autowired
	ConcertService concertService;

	// 절대 존재하지 않을 id. "없는 공연" 테스트에 쓴다
	private static final Long NOT_EXIST_ID = Long.MAX_VALUE;

	private final List<Long> createdIds = new ArrayList<>();

	@AfterEach // 각 테스트가 끝날 때마다 실행된다
	void cleanUp() {
		concertRepository.deleteAllById(createdIds);
	}

	@Test
	void findAll() {
		// given: 공연 2개를 등록한다
		ConcertResponse created1 = createConcert("록 페스티벌 1일차");
		ConcertResponse created2 = createConcert("록 페스티벌 2일차");

		// when: 목록을 조회한다
		List<ConcertResponse> concerts = concertService.findAll();

		// then: 방금 등록한 2개가 목록에 들어 있다 (샘플 데이터가 섞여 있어도 상관없다)
		assertThat(concerts).contains(created1, created2);
	}

	@Test
	void findById() {
		// given
		ConcertResponse created = createConcert("상세 조회 테스트");

		// when
		ConcertResponse found = concertService.findById(created.id());

		// then: record라서 필드 값이 전부 같으면 isEqualTo가 통과한다
		assertThat(found).isEqualTo(created);
	}

	@Test
	void create() {
		// when
		ConcertResponse created = createConcert("등록 테스트");

		// then: id가 발급되고, DB에 실제로 저장돼 있다
		assertThat(created.id()).isNotNull();
		assertThat(concertRepository.existsById(created.id())).isTrue();
	}

	@Test
	void update() {
		// given
		ConcertResponse created = createConcert("수정 전 제목");

		// when
		concertService.update(created.id(), request("수정 후 제목"));

		// then: 반환값이 아니라 DB에서 다시 읽어서 확인한다 (@Transactional 누락을 잡기 위해)
		assertThat(concertService.findById(created.id()).title()).isEqualTo("수정 후 제목");
	}

	@Test
	void delete() {
		// given
		ConcertResponse created = createConcert("삭제 테스트");

		// when
		concertService.delete(created.id());

		// then: DB에서 사라졌다
		assertThat(concertRepository.existsById(created.id())).isFalse();
	}



	// 헬퍼 함수
	private ConcertResponse createConcert(String title) {
		ConcertResponse created = concertService.create(request(title));
		createdIds.add(created.id());
		return created;
	}

	private ConcertCreateRequest request(String title) {
		return new ConcertCreateRequest(title, "강남", Genre.CONCERT, "록 페스티벌",
				LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 13));
	}

}
