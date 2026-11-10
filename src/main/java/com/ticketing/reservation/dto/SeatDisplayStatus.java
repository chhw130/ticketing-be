package com.ticketing.reservation.dto;

/**
 * 좌석 배치도에 보여 줄 좌석 상태. DB에 저장하지 않고, 조회할 때 예매 기록을 보고 계산한다.
 * (React의 파생 상태처럼 원본(좌석 + 예매)에서 매번 만들어 낸다)
 *
 * 예매 상태(ReserveStatus)와 헷갈리지 않게 이름을 따로 지었다.
 * - ReserveStatus: 예매 한 건의 상태 (DB에 저장)
 * - SeatDisplayStatus: 화면에서 좌석 하나가 어떻게 보이나 (응답에만 있음)
 */
public enum SeatDisplayStatus {
	AVAILABLE, // 유효한 예매가 없음 → 선택 가능
	SOLD       // 확정된 예매가 있음 → 판매 완료
	// 3단계에서 선점(RESERVED) 예매가 생기면 "다른 사람이 선택 중" 상태를 추가한다
}
