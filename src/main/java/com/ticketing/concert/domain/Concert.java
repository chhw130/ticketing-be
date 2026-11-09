package com.ticketing.concert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 공연. DB의 concert 테이블과 1:1로 대응한다.
 * 1단계에서 회차(schedule), 좌석(seat)이 이 엔티티에 연관관계로 붙는다.
 * seat -> schedule -> concert
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA가 쓰는 기본 생성자. 외부에서 빈 객체를 못 만들게 막는다
public class Concert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
	private Long id;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, length = 100)
	private String venue;

	@Enumerated(EnumType.STRING) // 숫자(ORDINAL) 대신 이름으로 저장해야 enum 순서가 바뀌어도 안전하다
	@Column(nullable = false, length = 20)
	private Genre genre;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	public Concert(String title, String venue, Genre genre, String description, LocalDate startDate, LocalDate endDate) {
		this.title = title;
		this.venue = venue;
		this.genre = genre;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void update(String title, String venue, Genre genre, String description, LocalDate startDate, LocalDate endDate) {
		this.title = title;
		this.venue = venue;
		this.genre = genre;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
