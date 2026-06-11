package com.project.kore.facade.impl;

import com.project.kore.dto.request.PlanCreateRequest;
import com.project.kore.dto.response.PlanResponse;
import com.project.kore.dto.response.stats.AdminStatsResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.facade.AdminFacade;
import com.project.kore.mapper.PlanMapper;
import com.project.kore.model.Plan;
import com.project.kore.model.Slot;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.PlanService;
import com.project.kore.service.SlotService;
import com.project.kore.service.SubscriptionService;
import com.project.kore.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Operazioni amministrative: gestione dei piani di abbonamento e statistiche globali della piattaforma.
 */
@Component
public class AdminFacadeImpl implements AdminFacade {

    private final PlanService planService;
    private final SlotService slotService;
    private final PlanMapper planMapper;
    private final SubscriptionService subscriptionService;
    private final UserService userService;

    public AdminFacadeImpl(UserService userService,
                           PlanService planService,
                           SlotService slotService,
                           PlanMapper planMapper,
                           SubscriptionService subscriptionService) {
        this.planService = planService;
        this.slotService = slotService;
        this.planMapper = planMapper;
        this.subscriptionService = subscriptionService;
        this.userService=userService;
    }

    /**
     * Crea un piano dopo aver controllato che i campi obbligatori ci siano e che il nome sia univoco.
     */
    @Override
    @Transactional
    public PlanResponse createPlan(PlanCreateRequest request) {
        String name = request.name();
        String durationRaw = request.duration();
        Double fullPrice = request.fullPrice();
        Double monthlyInstallmentPrice = request.monthlyInstallmentPrice();

        if (name == null || durationRaw == null || fullPrice == null || monthlyInstallmentPrice == null) {
            throw new IllegalArgumentException(
                    "Campi obbligatori mancanti (name, duration, fullPrice, monthlyInstallmentPrice).");
        }

        if (planService.existsByName(name)) {
            throw new ResourceAlreadyExistsException("Piano", "name", name);
        }

        Plan plan;
        try {
            plan = planMapper.toPlan(request);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Durata non valida: " + durationRaw);
        }

        return planMapper.toResponse(planService.createPlan(plan));
    }

    /**
     * Aggiorna un piano; se cambia il nome ne verifica prima l'unicità.
     */
    @Override
    @Transactional
    public PlanResponse updatePlan(Long id, PlanCreateRequest request) {
        Plan plan = planService.getPlanById(id);

        if (subscriptionService.hasActiveSubscribersByPlan(id)) {
            throw new IllegalStateException("Impossibile modificare il piano: esistono abbonamenti attivi collegati.");
        }

        if (request.name() != null && !request.name().isBlank() && !request.name().equals(plan.getName())) {
            if (planService.existsByName(request.name())) {
                throw new ResourceAlreadyExistsException("Piano", "name", request.name());
            }
        }

        try {
            planMapper.updatePlanFromRequest(request, plan);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Durata non valida: " + request.duration());
        }

        return planMapper.toResponse(planService.createPlan(plan));
    }

    // Tutti i piani, compresi i disabilitati, per la vista amministrativa.
    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlansForAdmin() {
        return planMapper.toResponseList(planService.getAllPlans());
    }

    /**
     * Abilita o disabilita un piano (soft-disable: il record resta in DB). Si può disabilitare
     * solo se non ha abbonamenti collegati, altrimenti viene rifiutato.
     */
    @Override
    @Transactional
    public PlanResponse setPlanStatus(Long id, boolean active) {
        if (!active && subscriptionService.hasSubscribersByPlan(id)) {
            throw new IllegalStateException("Impossibile disabilitare il piano: esistono abbonamenti collegati.");
        }
        return planMapper.toResponse(planService.setActive(id, active));
    }

    /**
     * Mette insieme le statistiche globali della piattaforma: utenti per ruolo, iscrizioni degli
     * ultimi sei mesi, popolarità dei piani, utilizzo dei crediti, revenue stimata, prenotazioni
     * e carico di lavoro dei professionisti.
     */
    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStats() {
        List<User> allUsers = userService.findAll();
        List<Subscription> allSubs = subscriptionService.getAllSubscriptions();
        List<Subscription> activeSubs = allSubs.stream().filter(Subscription::isActive).toList();
        List<Plan> allPlans = planService.getAllPlans();
        List<Slot> allBooked = slotService.getAllBookedSlots();

        Map<String, Long> usersByRole = allUsers.stream()
                .collect(Collectors.groupingBy(u -> u.getRole().name(), Collectors.counting()));

        List<AdminStatsResponse.MonthlyUserCount> usersPerMonth = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            long count = allUsers.stream()
                    .filter(u -> u.getCreatedAt() != null)
                    .filter(u -> {
                        LocalDate created = u.getCreatedAt().toLocalDate();
                        return !created.isBefore(start) && !created.isAfter(end);
                    })
                    .count();
            usersPerMonth.add(AdminStatsResponse.MonthlyUserCount.builder()
                    .month(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ITALIAN))
                    .year(ym.getYear())
                    .count(count)
                    .build());
        }

        Map<String, Long> subsByPlan = activeSubs.stream()
                .collect(Collectors.groupingBy(s -> s.getPlan().getName(), Collectors.counting()));
        long totalActiveSubs = activeSubs.size();
        List<AdminStatsResponse.PlanPopularityItem> planPopularity = allPlans.stream()
                .map(p -> {
                    long cnt = subsByPlan.getOrDefault(p.getName(), 0L);
                    return AdminStatsResponse.PlanPopularityItem.builder()
                            .name(p.getName())
                            .activeCount(cnt)
                            .percentage(totalActiveSubs > 0 ? Math.round((cnt * 100.0) / totalActiveSubs) : 0)
                            .monthlyPrice(p.getMonthlyInstallmentPrice())
                            .fullPrice(p.getFullPrice())
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getActiveCount(), a.getActiveCount()))
                .collect(Collectors.toList());

        int ptAvail = 0, nutriAvail = 0, ptMax = 0, nutriMax = 0;
        for (Subscription s : activeSubs) {
            ptAvail    += s.getCurrentCreditsPT();
            nutriAvail += s.getCurrentCreditsNutri();
            ptMax      += s.getPlan().getMonthlyCreditsPT();
            nutriMax   += s.getPlan().getMonthlyCreditsNutri();
        }
        AdminStatsResponse.CreditsStats credits = AdminStatsResponse.CreditsStats.builder()
                .ptAvailable(ptAvail)
                .ptTotal(ptMax)
                .ptConsumed(ptMax - ptAvail)
                .ptPercentUsed(ptMax > 0 ? Math.round(((ptMax - ptAvail) * 100.0) / ptMax) : 0)
                .nutriAvailable(nutriAvail)
                .nutriTotal(nutriMax)
                .nutriConsumed(nutriMax - nutriAvail)
                .nutriPercentUsed(nutriMax > 0 ? Math.round(((nutriMax - nutriAvail) * 100.0) / nutriMax) : 0)
                .build();

        double monthlyRevenue = activeSubs.stream()
                .mapToDouble(s -> s.getPlan().getMonthlyInstallmentPrice()).sum();
        double monthlyRev = Math.round(monthlyRevenue * 100.0) / 100.0;
        double yearlyRev  = Math.round(monthlyRevenue * 12 * 100.0) / 100.0;

        YearMonth thisMonth = YearMonth.now();
        long bookingsThisMonth = allBooked.stream()
                .filter(s -> s.getBookedAt() != null)
                .filter(s -> YearMonth.from(s.getBookedAt()).equals(thisMonth))
                .count();

        List<AdminStatsResponse.ProfessionalWorkloadItem> proWorkload = allUsers.stream()
                .filter(u -> u.getRole() == Role.PERSONAL_TRAINER || u.getRole() == Role.NUTRITIONIST)
                .map(pro -> {
                    long clientCount = pro.getRole() == Role.PERSONAL_TRAINER
                            ? allUsers.stream().filter(u -> u.getAssignedPT() != null && u.getAssignedPT().getId().equals(pro.getId())).count()
                            : allUsers.stream().filter(u -> u.getAssignedNutritionist() != null && u.getAssignedNutritionist().getId().equals(pro.getId())).count();
                    return AdminStatsResponse.ProfessionalWorkloadItem.builder()
                            .name(pro.getFullName())
                            .role(pro.getRole().name())
                            .clientCount(clientCount)
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getClientCount(), a.getClientCount()))
                .collect(Collectors.toList());

        return AdminStatsResponse.builder()
                .usersByRole(usersByRole)
                .totalUsers(allUsers.size())
                .usersPerMonth(usersPerMonth)
                .planPopularity(planPopularity)
                .totalActiveSubscriptions(totalActiveSubs)
                .totalSubscriptions(allSubs.size())
                .credits(credits)
                .monthlyRevenue(monthlyRev)
                .yearlyRevenue(yearlyRev)
                .bookingsThisMonth(bookingsThisMonth)
                .bookingsTotal(allBooked.size())
                .professionalWorkload(proWorkload)
                .build();
    }
}
