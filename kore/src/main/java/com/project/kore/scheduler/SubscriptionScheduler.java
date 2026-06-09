package com.project.kore.scheduler;

import com.project.kore.enums.PaymentFrequency;
import com.project.kore.model.Subscription;
import com.project.kore.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Cron job che gira in background ogni notte a mezzanotte.
 * Controlla individualmente ogni abbonamento attivo e resetta i crediti mensili
 * di PT e Nutrizionista in base alla data di anniversario del singolo cliente.
 */
@Component
public class SubscriptionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionScheduler.class);
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionScheduler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Scheduled(cron = "${schedule.time.subscription}")
    @Transactional
    public void renewCredits() {
        List<Subscription> activeSubs = subscriptionRepository.findByActiveTrue();
        LocalDate today = LocalDate.now();

        for (Subscription sub : activeSubs) {
            try {
                boolean deveRinnovare = false;

                if (sub.getPaymentFrequency() == PaymentFrequency.RATE_MENSILI) {
                    // Per i pagamenti rateali, il rinnovo è guidato dalla scadenza della prossima rata
                    if (sub.getNextPaymentDate() != null
                            && !today.isBefore(sub.getNextPaymentDate())) {

                        if (sub.getInstallmentsPaid() < sub.getTotalInstallments()) {
                            sub.setInstallmentsPaid(sub.getInstallmentsPaid() + 1);
                            sub.setNextPaymentDate(sub.getNextPaymentDate().plusMonths(1));
                            deveRinnovare = true;
                        } else {
                            log.warn("Pagamento rateale non dovuto per l'abbonamento ID {}: tutte le rate sono saldate", sub.getId());
                            continue;
                        }
                    }
                } else {
                    // Per abbonamenti in soluzione unica (es. annuali/trimestrali) che hanno comunque crediti mensili,
                    // verifichiamo se è passato esattamente un mese dall'ultimo rinnovo (o dalla data di inizio)
                    LocalDate dataRiferimento = sub.getLastRenewalDate() != null ? sub.getLastRenewalDate() : sub.getStartDate();
                    if (dataRiferimento != null && !today.isBefore(dataRiferimento.plusMonths(1))) {
                        deveRinnovare = true;
                    }
                }

                // Esegui il reset dei crediti solo se la condizione temporale specifica è soddisfatta
                if (deveRinnovare) {
                    sub.setCurrentCreditsPT(sub.getPlan().getMonthlyCreditsPT());
                    sub.setCurrentCreditsNutri(sub.getPlan().getMonthlyCreditsNutri());
                    sub.setLastRenewalDate(today);

                    subscriptionRepository.save(sub);
                    log.info("Rinnovo crediti eseguito con successo per l'abbonamento ID {}", sub.getId());
                }

            } catch (Exception e) {
                log.error("Errore nel rinnovo crediti per subscription ID {}: {}", sub.getId(), e.getMessage());
            }
        }
    }
}