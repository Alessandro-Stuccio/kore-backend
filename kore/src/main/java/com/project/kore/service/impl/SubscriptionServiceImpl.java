package com.project.kore.service.impl;

import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.repository.SubscriptionRepository;
import com.project.kore.service.SubscriptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Gestione degli abbonamenti. findActiveByUserWithLock prende un pessimistic write lock
 * sulla riga: serve quando si scalano i crediti durante prenotazioni simultanee, per evitare
 * che due booking concorrenti scendano sotto zero.
 */
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
public Subscription getSubscriptionStatus(User user) {
        return subscriptionRepository.findByUserAndActiveTrue(user)
                .orElseThrow(() -> new CustomResourceNotFoundException("Nessun abbonamento attivo trovato."));
    }

    @Override
    public Subscription save(Subscription sub) {
        return subscriptionRepository.save(sub);
    }

    @Override
    public Optional<Subscription> findActiveByUser(User user) {
        return subscriptionRepository.findByUserAndActiveTrue(user);
    }

    @Override
    public Optional<Subscription> findActiveByUserWithLock(User user) {
        return subscriptionRepository.findByUserAndActiveTrueWithLock(user);
    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    public Subscription updateSubscriptionCredits(Long subscriptionId, int creditsPT, int creditsNutri) {
        Subscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new CustomResourceNotFoundException("Abbonamento", subscriptionId));

        sub.setCurrentCreditsPT(creditsPT);
        sub.setCurrentCreditsNutri(creditsNutri);
        return subscriptionRepository.save(sub);
    }

    @Override
    public boolean hasSubscribersByPlan(Long planId) {
        return subscriptionRepository.existsByPlanId(planId);
    }
}
