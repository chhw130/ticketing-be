package com.ticketing.concert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공연 회차. 공연 하나(Concert)에 회차가 여러 개 달린다 (N:1).
 * DB에서는 schedule 테이블의 concert_id 컬럼(FK)으로 연결된다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자. 외부에서 빈 회차를 못 만들게 막는다
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
	private Long id;

	// 여러 회차(Many) → 공연 하나(One). FK를 가진 쪽이라 이 필드가 concert_id 컬럼을 관리한다 (연관관계의 주인)
	// LAZY: 회차를 조회할 때 공연은 바로 가져오지 않고, 공연 값을 실제로 쓸 때 가져온다
	// (@ManyToOne의 기본값은 EAGER라서 직접 LAZY로 지정해야 한다)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "concert_id", nullable = false)
	private Concert concert;

	@Column(nullable = false)
	private LocalDateTime startAt;

	// 예매가 열리는 시각.
	@Column(nullable = false)
	private LocalDateTime ticketOpenAt;

	public Schedule(Concert concert, LocalDateTime startAt, LocalDateTime ticketOpenAt) {
		this.concert = concert;
		this.startAt = startAt;
		this.ticketOpenAt = ticketOpenAt;
	}

	public boolean isOpen(LocalDateTime now) {
		return !now.isBefore(ticketOpenAt);
	}
}
