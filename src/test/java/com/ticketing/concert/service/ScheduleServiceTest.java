package com.ticketing.concert.service;

import com.ticketing.concert.domain.Concert;
import com.ticketing.concert.domain.Genre;
import com.ticketing.concert.domain.Schedule;
import com.ticketing.concert.dto.ScheduleResponse;
import com.ticketing.concert.repository.ConcertRepository;
import com.ticketing.concert.repository.ScheduleRepository;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ScheduleServiceTest {

	@Autowired
	ScheduleService scheduleService;

	@Autowired
	ScheduleRepository scheduleRepository;

	@Autowired
	ConcertRepository concertRepository;

	private static final Long NOT_EXIST_ID = Long.MAX_VALUE;

	private final List<Long> createdScheduleIds = new ArrayList<>();
	private final List<Long> createdConcertIds = new ArrayList<>();

	@AfterEach
	void cleanUp() {
		scheduleRepository.deleteAllById(createdScheduleIds);
		concertRepository.deleteAllById(createdConcertIds);
	}

	@Test
	void findById() {
		// given
		Concert concert = createConcert();
		Schedule schedule = createSchedule(concert, LocalDateTime.of(2027, 5, 1, 19, 0));

		// when
		ScheduleResponse found = scheduleService.findById(schedule.getId());

		// then
		assertThat(found.id()).isEqualTo(schedule.getId());
		assertThat(found.concertId()).isEqualTo(concert.getId());
	}

	@Test
	void findById_없는_id면_SCHEDULE_NOT_FOUND() {
		assertThatThrownBy(() -> scheduleService.findById(NOT_EXIST_ID))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
	}

	@Test
	void findByConcertId_시작_시각_순으로_그_공연의_회차만_돌려준다() {
		// given: 일부러 늦은 회차를 먼저 저장한다
		Concert concert = createConcert();
		Schedule later = createSchedule(concert, LocalDateTime.of(2027, 5, 2, 19, 0));
		Schedule earlier = createSchedule(concert, LocalDateTime.of(2027, 5, 1, 19, 0));
		Concert other = createConcert();
		createSchedule(other, LocalDateTime.of(2027, 5, 1, 19, 0));

		// when
		List<ScheduleResponse> schedules = scheduleService.findByConcertId(concert.getId());

		// then: 다른 공연의 회차는 빠지고, 시작 시각 순으로 정렬된다
		assertThat(schedules).extracting(ScheduleResponse::id)
				.containsExactly(earlier.getId(), later.getId());
	}

	@Test
	void findByConcertId_회차가_없는_공연이면_빈_목록() {
		// given: 공연은 있지만 회차는 하나도 없다
		Concert concert = createConcert();

		// when
		List<ScheduleResponse> schedules = scheduleService.findByConcertId(concert.getId());

		// then: "공연이 없음"(404)과 달리 에러가 아니라 빈 목록이다
		assertThat(schedules).isEmpty();
	}

	@Test
	void findByConcertId_없는_공연이면_CONCERT_NOT_FOUND() {
		assertThatThrownBy(() -> scheduleService.findByConcertId(NOT_EXIST_ID))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.CONCERT_NOT_FOUND);
	}

	// 헬퍼 함수
	private Concert createConcert() {
		Concert concert = concertRepository.save(new Concert("회차 테스트 공연", "강남", Genre.CONCERT, null,
				LocalDate.of(2027, 5, 1), LocalDate.of(2027, 5, 2)));
		createdConcertIds.add(concert.getId());
		return concert;
	}

	private Schedule createSchedule(Concert concert, LocalDateTime startAt) {
		Schedule schedule = scheduleRepository.save(new Schedule(concert, startAt, startAt.minusWeeks(2)));
		createdScheduleIds.add(schedule.getId());
		return schedule;
	}
}
