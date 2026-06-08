package com.project.kore.repository;

import com.project.kore.model.WeeklySchedule;
import com.project.kore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Regole orarie ricorrenti dei professionisti, da cui lo scheduler genera gli slot della settimana.
 */
public interface WeeklyScheduleRepository extends JpaRepository<WeeklySchedule, Long> {

    // Le fasce settimanali di un professionista, es. MONDAY 09:00-13:00, WEDNESDAY 14:00-18:00.
    List<WeeklySchedule> findByProfessional(User professional);

}