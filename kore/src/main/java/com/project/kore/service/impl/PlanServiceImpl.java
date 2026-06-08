package com.project.kore.service.impl;

import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Plan;
import com.project.kore.repository.PlanRepository;
import com.project.kore.service.PlanService;
import org.springframework.stereotype.Service;

import java.util.List;

/** CRUD sui piani di abbonamento. */
@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    public PlanServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Override
    public List<Plan> getActivePlans() {
        return planRepository.findByActiveTrue();
    }

    @Override
    public Plan getPlanById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Piano", id));
    }

    @Override
    public Plan createPlan(Plan plan) {
        return planRepository.save(plan);
    }

    // Soft-disable: cambia solo il flag, il piano resta in DB.
    @Override
    public Plan setActive(Long id, boolean active) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> new CustomResourceNotFoundException("Piano", id));
        plan.setActive(active);
        return planRepository.save(plan);
    }

    @Override
    public boolean existsByName(String name) {
        return planRepository.findByName(name).isPresent();
    }
}
