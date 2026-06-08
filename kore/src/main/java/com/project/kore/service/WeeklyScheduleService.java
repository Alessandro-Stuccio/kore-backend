package com.project.kore.service;

import com.project.kore.model.User;
import com.project.kore.model.WeeklySchedule;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** Gestione dei calendari settimanali dei professionisti. */
@Validated
public interface WeeklyScheduleService {

    /**
     * Calendari settimanali del professionista.
     *
     * @param professional il professionista
     * @return i suoi calendari settimanali
     */
    List<WeeklySchedule> findByProfessional(@NotNull User professional);
}
