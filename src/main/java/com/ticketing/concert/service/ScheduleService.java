package com.ticketing.concert.service;

import com.ticketing.concert.domain.Schedule;
import com.ticketing.concert.dto.ScheduleResponse;
import com.ticketing.concert.repository.ConcertRepository;
import com.ticketing.concert.repository.ScheduleRepository;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ConcertRepository concertRepository;

    public ScheduleResponse findById(Long id){
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND, "id=" + id));

        return ScheduleResponse.from(schedule);
    }

    public List<ScheduleResponse> findByConcertId(Long concertId){
        if (!concertRepository.existsById(concertId)) {
            throw new BusinessException(ErrorCode.CONCERT_NOT_FOUND, "id=" + concertId);
        }
        return scheduleRepository.findByConcertIdOrderByStartAtAsc(concertId).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

}
