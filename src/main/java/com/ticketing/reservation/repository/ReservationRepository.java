package com.ticketing.reservation.repository;

import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReserveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	List<Reservation> findByScheduleIdAndStatusNot(Long scheduleId, ReserveStatus status);
}
