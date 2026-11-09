package com.ticketing.concert.repository;

import com.ticketing.concert.domain.Concert;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ConcertRepository extends JpaRepository<Concert, Long> {
}
