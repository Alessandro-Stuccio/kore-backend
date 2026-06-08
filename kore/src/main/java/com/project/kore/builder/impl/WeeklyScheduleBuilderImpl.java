package com.project.kore.builder.impl;

import com.project.kore.builder.WeeklyScheduleBuilder;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import com.project.kore.model.*;


/**
 * Builder concreto per WeeklySchedule. In build pretende i campi obbligatori e che startTime preceda endTime.
 */
public class WeeklyScheduleBuilderImpl implements WeeklyScheduleBuilder {
    private Long id;
    private User professional;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    @Override
    public WeeklyScheduleBuilder id(Long id) {
        this.id = id;
        return this;
    }
    @Override
    public WeeklyScheduleBuilder professional(User professional) {
        this.professional = professional;
        return this;
    }
    @Override
    public WeeklyScheduleBuilder dayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        return this;
    }
    @Override
    public WeeklyScheduleBuilder startTime(LocalTime startTime) {
        this.startTime = startTime;
        return this;
    }
    @Override
    public WeeklyScheduleBuilder endTime(LocalTime endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public WeeklySchedule build() {
        Objects.requireNonNull(this.professional, "professional è obbligatorio");
        Objects.requireNonNull(this.dayOfWeek, "dayOfWeek è obbligatorio");
        Objects.requireNonNull(this.startTime, "startTime è obbligatorio");
        Objects.requireNonNull(this.endTime, "endTime è obbligatorio");

        if (!this.startTime.isBefore(this.endTime))
            throw new IllegalArgumentException("startTime deve essere precedente a endTime");

        WeeklySchedule obj = new WeeklySchedule();
        obj.setId(this.id);
        obj.setProfessional(this.professional);
        obj.setDayOfWeek(this.dayOfWeek);
        obj.setStartTime(this.startTime);
        obj.setEndTime(this.endTime);
        return obj;
    }
}
