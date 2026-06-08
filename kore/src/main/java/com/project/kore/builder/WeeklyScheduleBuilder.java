package com.project.kore.builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import com.project.kore.model.*;


/**
 * Costruisce un WeeklySchedule, la fascia oraria settimanale di un professionista, con interfaccia fluente.
 */
public interface WeeklyScheduleBuilder {

    /**
     * Imposta l'id della fascia oraria.
     *
     * @param id identificativo della fascia
     * @return questo builder, per concatenare le chiamate
     */
    WeeklyScheduleBuilder id(Long id);

    /**
     * Imposta il professionista titolare della fascia.
     *
     * @param professional il professionista
     * @return questo builder, per concatenare le chiamate
     */
    WeeklyScheduleBuilder professional(User professional);

    /**
     * Imposta il giorno della settimana.
     *
     * @param dayOfWeek giorno della settimana coperto dalla fascia
     * @return questo builder, per concatenare le chiamate
     */
    WeeklyScheduleBuilder dayOfWeek(DayOfWeek dayOfWeek);

    /**
     * Imposta l'orario di inizio della fascia.
     *
     * @param startTime ora di inizio
     * @return questo builder, per concatenare le chiamate
     */
    WeeklyScheduleBuilder startTime(LocalTime startTime);

    /**
     * Imposta l'orario di fine della fascia.
     *
     * @param endTime ora di fine
     * @return questo builder, per concatenare le chiamate
     */
    WeeklyScheduleBuilder endTime(LocalTime endTime);

    /**
     * Costruisce il WeeklySchedule con i valori impostati.
     *
     * @return la fascia oraria costruita
     */
    WeeklySchedule build();
}
