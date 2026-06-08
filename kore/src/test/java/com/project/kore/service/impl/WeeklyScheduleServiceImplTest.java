package com.project.kore.service.impl;

import com.project.kore.model.User;
import com.project.kore.model.WeeklySchedule;
import com.project.kore.repository.WeeklyScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyScheduleServiceImplTest {

    @Mock
    private WeeklyScheduleRepository weeklyScheduleRepository;

    @InjectMocks
    private WeeklyScheduleServiceImpl weeklyScheduleService;

    private User professional;
    private WeeklySchedule mondaySchedule;
    private WeeklySchedule wednesdaySchedule;

    @BeforeEach
    void setUp() {
        professional = new User();
        professional.setId(1L);
        professional.setEmail("pt@test.com");

        mondaySchedule = new WeeklySchedule();
        mondaySchedule.setId(1L);
        mondaySchedule.setProfessional(professional);
        mondaySchedule.setDayOfWeek(DayOfWeek.MONDAY);
        mondaySchedule.setStartTime(LocalTime.of(9, 0));
        mondaySchedule.setEndTime(LocalTime.of(13, 0));

        wednesdaySchedule = new WeeklySchedule();
        wednesdaySchedule.setId(2L);
        wednesdaySchedule.setProfessional(professional);
        wednesdaySchedule.setDayOfWeek(DayOfWeek.WEDNESDAY);
        wednesdaySchedule.setStartTime(LocalTime.of(14, 0));
        wednesdaySchedule.setEndTime(LocalTime.of(18, 0));
    }

    // ---- findByProfessional ----

    @Test
    @DisplayName("findByProfessional: returns all weekly schedules for the given professional")
    void findByProfessional_returnsAllSchedules() {
        when(weeklyScheduleRepository.findByProfessional(professional))
                .thenReturn(List.of(mondaySchedule, wednesdaySchedule));

        List<WeeklySchedule> result = weeklyScheduleService.findByProfessional(professional);

        assertThat(result).hasSize(2).containsExactlyInAnyOrder(mondaySchedule, wednesdaySchedule);
        verify(weeklyScheduleRepository).findByProfessional(professional);
    }

    @Test
    @DisplayName("findByProfessional: returns empty list when professional has no weekly schedule configured")
    void findByProfessional_noSchedules_returnsEmpty() {
        when(weeklyScheduleRepository.findByProfessional(professional)).thenReturn(List.of());

        List<WeeklySchedule> result = weeklyScheduleService.findByProfessional(professional);

        assertThat(result).isEmpty();
        verify(weeklyScheduleRepository).findByProfessional(professional);
    }

    @Test
    @DisplayName("findByProfessional: delegates exactly once to the repository with the given professional")
    void findByProfessional_callsRepositoryOnce() {
        when(weeklyScheduleRepository.findByProfessional(professional)).thenReturn(List.of(mondaySchedule));

        weeklyScheduleService.findByProfessional(professional);

        verify(weeklyScheduleRepository, times(1)).findByProfessional(professional);
    }
}
