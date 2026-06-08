package com.project.kore.builder.impl;

import com.project.kore.model.User;
import com.project.kore.model.WeeklySchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyScheduleBuilderImplTest {

    private WeeklyScheduleBuilderImpl builder;
    private User professional;

    @BeforeEach
    void setUp() {
        builder = new WeeklyScheduleBuilderImpl();

        professional = new User();
        professional.setId(1L);
        professional.setEmail("pt@test.com");
    }

    @Test
    @DisplayName("build — tutti i campi impostati: restituisce WeeklySchedule corretto")
    void build_allFieldsSet_returnsWeeklySchedule() {
        WeeklySchedule schedule = builder
                .id(10L)
                .professional(professional)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        assertThat(schedule).isNotNull();
        assertThat(schedule.getId()).isEqualTo(10L);
        assertThat(schedule.getProfessional()).isEqualTo(professional);
        assertThat(schedule.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(schedule.getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(schedule.getEndTime()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("build — senza professional: lancia NullPointerException")
    void build_missingProfessional_throwsNullPointerException() {
        assertThatThrownBy(() -> builder
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("professional");
    }

    @Test
    @DisplayName("build — senza dayOfWeek: lancia NullPointerException")
    void build_missingDayOfWeek_throwsNullPointerException() {
        assertThatThrownBy(() -> builder
                .professional(professional)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("dayOfWeek");
    }

    @Test
    @DisplayName("build — senza startTime: lancia NullPointerException")
    void build_missingStartTime_throwsNullPointerException() {
        assertThatThrownBy(() -> builder
                .professional(professional)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .endTime(LocalTime.of(10, 0))
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("startTime");
    }

    @Test
    @DisplayName("build — senza endTime: lancia NullPointerException")
    void build_missingEndTime_throwsNullPointerException() {
        assertThatThrownBy(() -> builder
                .professional(professional)
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("endTime");
    }

    @Test
    @DisplayName("build — startTime uguale a endTime: lancia IllegalArgumentException")
    void build_startTimeEqualsEndTime_throwsIllegalArgumentException() {
        LocalTime same = LocalTime.of(10, 0);
        assertThatThrownBy(() -> builder
                .professional(professional)
                .dayOfWeek(DayOfWeek.FRIDAY)
                .startTime(same)
                .endTime(same)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startTime");
    }

    @Test
    @DisplayName("build — startTime dopo endTime: lancia IllegalArgumentException")
    void build_startTimeAfterEndTime_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> builder
                .professional(professional)
                .dayOfWeek(DayOfWeek.THURSDAY)
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(10, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("build — senza id opzionale: id è null")
    void build_withoutId_idIsNull() {
        WeeklySchedule schedule = builder
                .professional(professional)
                .dayOfWeek(DayOfWeek.SATURDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 30))
                .build();

        assertThat(schedule.getId()).isNull();
    }

    @Test
    @DisplayName("fluent API — ogni setter restituisce lo stesso builder")
    void fluentApi_settersReturnSameBuilder() {
        WeeklyScheduleBuilderImpl b = new WeeklyScheduleBuilderImpl();
        assertThat(b.id(1L)).isSameAs(b);
        assertThat(b.professional(professional)).isSameAs(b);
        assertThat(b.dayOfWeek(DayOfWeek.MONDAY)).isSameAs(b);
        assertThat(b.startTime(LocalTime.of(9, 0))).isSameAs(b);
        assertThat(b.endTime(LocalTime.of(10, 0))).isSameAs(b);
    }
}
