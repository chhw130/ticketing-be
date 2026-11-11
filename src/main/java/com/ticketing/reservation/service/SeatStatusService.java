package com.ticketing.reservation.service;

import com.ticketing.concert.domain.Schedule;
import com.ticketing.concert.domain.Seat;
import com.ticketing.concert.repository.ScheduleRepository;
import com.ticketing.concert.repository.SeatRepository;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import com.ticketing.reservation.domain.ReserveStatus;
import com.ticketing.reservation.dto.ScheduleSeatsResponse;
import com.ticketing.reservation.dto.SeatDisplayStatus;
import com.ticketing.reservation.dto.SeatStatusResponse;
import com.ticketing.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatStatusService {

	private final ScheduleRepository scheduleRepository;
	private final SeatRepository seatRepository;
	private final ReservationRepository reservationRepository;

	public ScheduleSeatsResponse findSeats(Long scheduleId) {
		return findSeats(scheduleId, LocalDateTime.now());
	}

	// 현재 시각을 받는 버전. 테스트에서 오픈 전/후 시각을 넣어 보려고 따로 연다
	public ScheduleSeatsResponse findSeats(Long scheduleId, LocalDateTime now) {
		// 쿼리 1: 회차. 없으면 404
		Schedule schedule = scheduleRepository.findById(scheduleId)
				.orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "id=" + scheduleId));

		// 쿼리 2: 이 회차의 좌석 (번호 순)
		List<Seat> seats = seatRepository.findByScheduleIdOrderBySeatNumberAsc(scheduleId);

		// 쿼리 3: 이 회차의 취소되지 않은 예매 → "팔린 좌석 id 모음"으로 바꾼다
		// getSeat().getId()는 LAZY 프록시에서 id(FK 값)만 꺼내므로 추가 쿼리가 나가지 않는다
		Set<Long> soldSeatIds = reservationRepository.findByScheduleIdAndStatusNot(scheduleId, ReserveStatus.CANCELED)
				.stream()
				.map(reservation -> reservation.getSeat().getId())
				.collect(Collectors.toSet());

		// 지금은 취소가 아닌 예매(CONFIRMED)가 있으면 판매 완료로 본다.
		// 3단계에서 선점(RESERVED)이 생기면 "선택 중" 상태를 따로 나눈다
		List<SeatStatusResponse> seatResponses = seats.stream()
				.map(seat -> SeatStatusResponse.of(seat,
						soldSeatIds.contains(seat.getId()) ? SeatDisplayStatus.SOLD : SeatDisplayStatus.AVAILABLE))
				.toList();

		return ScheduleSeatsResponse.of(schedule, schedule.isOpen(now), seatResponses);
	}
}
