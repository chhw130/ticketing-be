package com.ticketing.concert.repository;

import com.ticketing.concert.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	// 쿼리 메서드: 이름을 보고 Spring Data가 JPQL을 만들어 준다
	// → select s from Schedule s where s.concert.id = ?1 order by s.startAt asc
	List<Schedule> findByConcertIdOrderByStartAtAsc(Long concertId);
}
