package com.ticketing.concert.repository;

import com.ticketing.concert.domain.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 인터페이스만 선언하면 Spring Data JPA가 구현체를 만들어 준다.
 * findAll, findById, save, deleteById 같은 기본 메서드가 이미 들어 있다.
 */
public interface ConcertRepository extends JpaRepository<Concert, Long> {
}
