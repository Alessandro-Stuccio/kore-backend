package com.project.kore.service.strategy;

import com.project.kore.enums.Role;
import com.project.kore.exception.booking.InsufficientCreditsException;
import com.project.kore.exception.booking.ProfessionalNotAssignedException;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class PersonalTrainerBookingStrategyTest {

    private PersonalTrainerBookingStrategy strategy;

    private User client;
    private User professional;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        strategy = new PersonalTrainerBookingStrategy();

        professional = new User();
        professional.setId(1L);

        client = new User();
        client.setId(2L);
        client.setAssignedPT(professional);

        subscription = new Subscription();
        subscription.setCurrentCreditsPT(2);
    }

    @Test
    @DisplayName("getSupportedRole — restituisce PERSONAL_TRAINER")
    void getSupportedRole_returnsPersonalTrainer() {
        assertThat(strategy.getSupportedRole()).isEqualTo(Role.PERSONAL_TRAINER);
    }

    @Test
    @DisplayName("verifyAssignment — PT assegnato coincide: nessuna eccezione")
    void verifyAssignment_ptAssigned_doesNotThrow() {
        assertThatNoException().isThrownBy(() -> strategy.verifyAssignment(client, professional));
    }

    @Test
    @DisplayName("verifyAssignment — PT null: lancia ProfessionalNotAssignedException")
    void verifyAssignment_ptNull_throwsProfessionalNotAssignedException() {
        client.setAssignedPT(null);
        assertThatThrownBy(() -> strategy.verifyAssignment(client, professional))
                .isInstanceOf(ProfessionalNotAssignedException.class);
    }

    @Test
    @DisplayName("verifyAssignment — PT diverso da quello richiesto: lancia ProfessionalNotAssignedException")
    void verifyAssignment_differentPt_throwsProfessionalNotAssignedException() {
        User otherPt = new User();
        otherPt.setId(99L);
        client.setAssignedPT(otherPt);

        assertThatThrownBy(() -> strategy.verifyAssignment(client, professional))
                .isInstanceOf(ProfessionalNotAssignedException.class);
    }

    @Test
    @DisplayName("consumeCredits — crediti sufficienti: decrementa di 1")
    void consumeCredits_sufficientCredits_decrementsCredits() {
        strategy.consumeCredits(subscription);
        assertThat(subscription.getCurrentCreditsPT()).isEqualTo(1);
    }

    @Test
    @DisplayName("consumeCredits — crediti a 0: lancia InsufficientCreditsException")
    void consumeCredits_zeroCredits_throwsInsufficientCreditsException() {
        subscription.setCurrentCreditsPT(0);
        assertThatThrownBy(() -> strategy.consumeCredits(subscription))
                .isInstanceOf(InsufficientCreditsException.class);
    }

    @Test
    @DisplayName("refundCredits — incrementa currentCreditsPT di 1")
    void refundCredits_incrementsCurrentCreditsPT() {
        subscription.setCurrentCreditsPT(1);
        strategy.refundCredits(subscription);
        assertThat(subscription.getCurrentCreditsPT()).isEqualTo(2);
    }
}
