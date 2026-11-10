package com.ticketing.global.init;

import com.ticketing.concert.domain.Concert;
import com.ticketing.concert.domain.Genre;
import com.ticketing.concert.domain.Schedule;
import com.ticketing.concert.domain.Seat;
import com.ticketing.concert.domain.SeatGrade;
import com.ticketing.concert.repository.ConcertRepository;
import com.ticketing.concert.repository.ScheduleRepository;
import com.ticketing.concert.repository.SeatRepository;
import com.ticketing.member.domain.Member;
import com.ticketing.member.repository.MemberRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SampleDataInitializer implements ApplicationRunner {

	private static final int SEATS_PER_SCHEDULE = 40;

	private final ConcertRepository concertRepository;
	private final ScheduleRepository scheduleRepository;
	private final SeatRepository seatRepository;
	private final MemberRepository memberRepository;
	private final ReservationRepository reservationRepository;

	@Override
	@Transactional // 전부 한 트랜잭션으로 묶어서, 중간에 실패하면 반쯤 들어간 데이터가 남지 않게 한다
	public void run(ApplicationArguments args) {
		List<Concert> concerts = initConcerts();
		List<Schedule> schedules = initSchedulesAndSeats(concerts);
		List<Member> members = initMembers();
		initReservations(schedules, members);
	}

	private List<Concert> initConcerts() {
		if (concertRepository.count() > 0) {
			return concertRepository.findAll();
		}
		return concertRepository.saveAll(List.of(
				new Concert("2026 겨울 콘서트", "KSPO DOME", Genre.CONCERT,
						"연말을 장식할 단독 콘서트", LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 22)),
				new Concert("뮤지컬 오페라의 유령", "블루스퀘어 신한카드홀", Genre.MUSICAL,
						"오리지널 내한 공연", LocalDate.of(2027, 1, 10), LocalDate.of(2027, 3, 31)),
				new Concert("연극 햄릿", "예술의전당 CJ토월극장", Genre.THEATER,
						"셰익스피어 4대 비극", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 2, 28))
		));
	}

	/**
	 * 공연마다 회차 2개를 만들고, 회차마다 좌석 40개를 만든다.
	 * - 1회차: 예매 오픈됨 (어제 20시에 오픈) → 좌석 배치도에서 바로 예매해 볼 수 있다
	 * - 2회차: 예매 오픈 전 (공연 2주 전 20시에 오픈) → "오픈 전" 화면을 확인할 수 있다
	 */
	private List<Schedule> initSchedulesAndSeats(List<Concert> concerts) {
		if (scheduleRepository.count() > 0) {
			return scheduleRepository.findAll();
		}

		LocalDateTime openedAt = LocalDate.now().minusDays(1).atTime(20, 0);
		List<Schedule> schedules = new ArrayList<>();
		for (Concert concert : concerts) {
			LocalDateTime firstStartAt = concert.getStartDate().atTime(LocalTime.of(19, 0));
			LocalDateTime secondStartAt = firstStartAt.plusDays(1);

			schedules.add(new Schedule(concert, firstStartAt, openedAt));
			schedules.add(new Schedule(concert, secondStartAt, secondStartAt.minusWeeks(2).withHour(20)));
		}
		List<Schedule> saved = scheduleRepository.saveAll(schedules);

		List<Seat> seats = new ArrayList<>();
		for (Schedule schedule : saved) {
			for (int number = 1; number <= SEATS_PER_SCHEDULE; number++) {
				SeatGrade grade = gradeOf(number);
				seats.add(new Seat(schedule, number, grade, priceOf(grade)));
			}
		}
		// 좌석 240개를 하나씩 save()하지 않고 리스트로 모아서 저장한다
		seatRepository.saveAll(seats);
		return saved;
	}

	// 좌석 번호로 등급을 정한다 (결정 사항: 1~10 VIP, 11~20 S, 21~30 A, 31~40 B)
	// 지금은 샘플 데이터에서만 좌석을 만들어서 여기에 둔다. 좌석을 만드는 곳이 늘어나면 한곳으로 옮긴다
	private SeatGrade gradeOf(int seatNumber) {
		if (seatNumber <= 10) return SeatGrade.VIP;
		if (seatNumber <= 20) return SeatGrade.S;
		if (seatNumber <= 30) return SeatGrade.A;
		return SeatGrade.B;
	}

	private Long priceOf(SeatGrade grade) {
		return switch (grade) {
			case VIP -> 150_000L;
			case S -> 120_000L;
			case A -> 90_000L;
			case B -> 60_000L;
		};
	}

	private List<Member> initMembers() {
		if (memberRepository.count() > 0) {
			return memberRepository.findAll();
		}
		List<Member> members = new ArrayList<>();
		members.add(new Member("테스터", false)); // X-User-Id: 1 로 쓸 사람 회원
		for (int i = 1; i <= 5; i++) {
			members.add(new Member("봇" + i, true));
		}
		return memberRepository.saveAll(members);
	}

	/**
	 * 첫 공연의 1회차(오픈된 회차)에 봇 예매 몇 건을 넣어서, 좌석 배치도에 "판매 완료" 좌석이 보이게 한다.
	 */
	private void initReservations(List<Schedule> schedules, List<Member> members) {
		if (reservationRepository.count() > 0 || schedules.isEmpty()) {
			return;
		}
		Schedule openedSchedule = schedules.getFirst();
		List<Seat> seats = seatRepository.findAll().stream()
				.filter(seat -> seat.getSchedule().getId().equals(openedSchedule.getId()))
				.toList();
		List<Member> bots = members.stream().filter(Member::isBot).toList();

		// VIP 2석, S 1석, A 1석이 이미 팔린 상태
		int[] soldSeatNumbers = {1, 2, 15, 22};
		List<Reservation> reservations = new ArrayList<>();
		for (int i = 0; i < soldSeatNumbers.length; i++) {
			int seatNumber = soldSeatNumbers[i];
			Seat seat = seats.stream().filter(s -> s.getSeatNumber() == seatNumber).findFirst().orElseThrow();
			Member bot = bots.get(i % bots.size());
			// 회차는 좌석에서 꺼내 넘긴다 → 좌석의 회차와 예매의 회차가 어긋나지 않는다
			reservations.add(new Reservation(bot, seat, seat.getSchedule()));
		}
		reservationRepository.saveAll(reservations);
	}
}
