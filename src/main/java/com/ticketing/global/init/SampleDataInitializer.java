package com.ticketing.global.init;

import com.ticketing.concert.domain.Concert;
import com.ticketing.concert.domain.Genre;
import com.ticketing.concert.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 서버가 뜰 때 공연 테이블이 비어 있으면 샘플 데이터를 넣는다.
 * 프론트 화면을 바로 확인하기 위한 개발용 코드다.
 */
@Component
@RequiredArgsConstructor
public class SampleDataInitializer implements ApplicationRunner {

	private final ConcertRepository concertRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (concertRepository.count() > 0) {
			return;
		}

		concertRepository.saveAll(List.of(
				new Concert("2026 겨울 콘서트", "KSPO DOME", Genre.CONCERT,
						"연말을 장식할 단독 콘서트", LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 22)),
				new Concert("뮤지컬 오페라의 유령", "블루스퀘어 신한카드홀", Genre.MUSICAL,
						"오리지널 내한 공연", LocalDate.of(2027, 1, 10), LocalDate.of(2027, 3, 31)),
				new Concert("연극 햄릿", "예술의전당 CJ토월극장", Genre.THEATER,
						"셰익스피어 4대 비극", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 28))
		));
	}

}
