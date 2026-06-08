package com.project.kore.service.impl;

import com.project.kore.model.User;
import com.project.kore.model.WeeklySchedule;
import com.project.kore.repository.WeeklyScheduleRepository;
import com.project.kore.service.WeeklyScheduleService;
import org.springframework.stereotype.Service;

import java.util.List;

/** Recupero dei calendari settimanali dei professionisti. */
@Service
public class WeeklyScheduleServiceImpl implements WeeklyScheduleService {

    private final WeeklyScheduleRepository weeklyScheduleRepository;

    public WeeklyScheduleServiceImpl(WeeklyScheduleRepository weeklyScheduleRepository) {
        this.weeklyScheduleRepository = weeklyScheduleRepository;
    }

    @Override
    public List<WeeklySchedule> findByProfessional(User professional) {
        return weeklyScheduleRepository.findByProfessional(professional);
    }

}
