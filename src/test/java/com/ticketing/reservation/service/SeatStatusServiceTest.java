package com.ticketing.reservation.service;

import com.ticketing.concert.domain.Concert;
import com.ticketing.concert.domain.Genre;
import com.ticketing.concert.domain.Schedule;
import com.ticketing.concert.domain.Seat;
import com.ticketing.concert.domain.SeatGrade;
import com.ticketing.concert.repository.ConcertRepository;
import com.ticketing.concert.repository.ScheduleRepository;
import com.ticketing.concert.repository.SeatRepository;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import com.ticketing.member.domain.Member;
import com.ticketing.member.repository.MemberRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.dto.ScheduleSeatsResponse;
import com.ticketing.reservation.dto.SeatDisplayStatus;
import com.ticketing.reservation.dto.SeatStatusResponse;
import com.ticketing.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
class SeatStatusServiceTest {

	@Autowired SeatStatusService seatStatusService;
	@Autowired ConcertRepository concertRepository;
	@Autowired ScheduleRepository scheduleRepository;
	@Autowired SeatRepository seatRepository;
	@Autowired MemberRepository memberRepository;
	@Autowired ReservationRepository reservationRepository;

	private static final LocalDateTime OPEN_AT = LocalDateTime.of(2027, 4, 17, 20, 0);

	private Concert concert;
	private Schedule schedule;
	private Seat seat1;
	private Seat seat2;
	private Seat seat3;
	private Member member;
	private final List<Reservation> createdReservations = new ArrayList<>();

	@BeforeEach // 각 테스트 직전에 실행된다 (@AfterEach의 반대)
	void setUp() {
		concert = concertRepository.save(new Concert("좌석 현황 테스트 공연", "강남", Genre.CONCERT, null,
				LocalDate.of(2027, 5, 1), LocalDate.of(2027, 5, 1)));
		schedule = scheduleRepository.save(new Schedule(concert, LocalDateTime.of(2027, 5, 1, 19, 0), OPEN_AT));
		// 일부러 번호 순서를 섞어서 저장한다 → 응답이 번호 순으로 정렬되는지 확인하려고
		seat3 = seatRepository.save(new Seat(schedule, 3, SeatGrade.S, 120_000L));
		seat1 = seatRepository.save(new Seat(schedule, 1, SeatGrade.VIP, 150_000L));
		seat2 = seatRepository.save(new Seat(schedule, 2, SeatGrade.VIP, 150_000L));
		member = memberRepository.save(new Member("좌석테스터", false));
	}

	@AfterEach // FK 때문에 참조하는 쪽부터 지운다: 예매 → 좌석 → 회차 → 공연, 회원
	void cleanUp() {
		reservationRepository.deleteAll(createdReservations);
		seatRepository.deleteAll(List.of(seat1, seat2, seat3));
		scheduleRepository.delete(schedule);
		concertRepository.delete(concert);
		memberRepository.delete(member);
	}

	@Test
	void 예매가_없으면_모든_좌석이_AVAILABLE이고_번호_순이다() {
		ScheduleSeatsResponse response = seatStatusService.findSeats(schedule.getId(), OPEN_AT);

		assertThat(response.seats()).extracting(SeatStatusResponse::seatNumber).containsExactly(1, 2, 3);
		assertThat(response.seats()).extracting(SeatStatusResponse::status).containsOnly(SeatDisplayStatus.AVAILABLE);
	}

	@Test
	void 예매된_좌석만_SOLD다() {
		// given: 2번 좌석만 예매
		saveReservation(new Reservation(member, seat2, schedule));

		// when
		ScheduleSeatsResponse response = seatStatusService.findSeats(schedule.getId(), OPEN_AT);

		// then
		assertThat(response.seats()).extracting(SeatStatusResponse::status)
				.containsExactly(SeatDisplayStatus.AVAILABLE, SeatDisplayStatus.SOLD, SeatDisplayStatus.AVAILABLE);
	}

	@Test
	void 취소된_예매는_AVAILABLE로_돌아간다() {
		// given: 1번 좌석을 예매했다가 취소
		Reservation reservation = new Reservation(member, seat1, schedule);
		reservation.cancel();
		saveReservation(reservation);

		// when
		ScheduleSeatsResponse response = seatStatusService.findSeats(schedule.getId(), OPEN_AT);

		// then
		assertThat(response.seats().getFirst().status()).isEqualTo(SeatDisplayStatus.AVAILABLE);
	}

	@Test
	void 오픈_시각_전이면_open이_false_이후면_true다() {
		assertThat(seatStatusService.findSeats(schedule.getId(), OPEN_AT.minusSeconds(1)).open()).isFalse();
		assertThat(seatStatusService.findSeats(schedule.getId(), OPEN_AT).open()).isTrue(); // 정각부터 오픈
	}

	@Test
	void 없는_회차면_SCHEDULE_NOT_FOUND() {
		assertThatThrownBy(() -> seatStatusService.findSeats(Long.MAX_VALUE))
				.isInstanceOf(BusinessException.class)
				.extracting("errorCode")
				.isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
	}

	// 헬퍼 함수: 저장하고 정리 목록에 넣는다
	private void saveReservation(Reservation reservation) {
		createdReservations.add(reservationRepository.save(reservation));
	}
}
