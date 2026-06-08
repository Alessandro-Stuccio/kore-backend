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

class NutritionistBookingStrategyTest {

    private NutritionistBookingStrategy strategy;

    private User client;
    private User professional;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        strategy = new NutritionistBookingStrategy();

        professional = new User();
        professional.setId(1L);

        client = new User();
        client.setId(2L);
        client.setAssignedNutritionist(professional);

        subscription = new Subscription();
        subscription.setCurrentCreditsNutri(2);
    }

    @Test
    @DisplayName("getSupportedRole — restituisce NUTRITIONIST")
    void getSupportedRole_returnsNutritionist() {
        assertThat(strategy.getSupportedRole()).isEqualTo(Role.NUTRITIONIST);
    }

    @Test
    @DisplayName("verifyAssignment — nutrizionista assegnato coincide: nessuna eccezione")
    void verifyAssignment_nutriAssigned_doesNotThrow() {
        assertThatNoException().isThrownBy(() -> strategy.verifyAssignment(client, professional));
    }

    @Test
    @DisplayName("verifyAssignment — nutrizionista null: lancia ProfessionalNotAssignedException")
    void verifyAssignment_nutriNull_throwsProfessionalNotAssignedException() {
        client.setAssignedNutritionist(null);
        assertThatThrownBy(() -> strategy.verifyAssignment(client, professional))
                .isInstanceOf(ProfessionalNotAssignedException.class);
    }

    @Test
    @DisplayName("verifyAssignment — nutrizionista diverso da quello richiesto: lancia ProfessionalNotAssignedException")
    void verifyAssignment_differentNutri_throwsProfessionalNotAssignedException() {
        User otherNutri = new User();
        otherNutri.setId(99L);
        client.setAssignedNutritionist(otherNutri);

        assertThatThrownBy(() -> strategy.verifyAssignment(client, professional))
                .isInstanceOf(ProfessionalNotAssignedException.class);
    }

    @Test
    @DisplayName("consumeCredits — crediti sufficienti: decrementa di 1")
    void consumeCredits_sufficientCredits_decrementsCredits() {
        strategy.consumeCredits(subscription);
        assertThat(subscription.getCurrentCreditsNutri()).isEqualTo(1);
    }

    @Test
    @DisplayName("consumeCredits — crediti a 0: lancia InsufficientCreditsException")
    void consumeCredits_zeroCredits_throwsInsufficientCreditsException() {
        subscription.setCurrentCreditsNutri(0);
        assertThatThrownBy(() -> strategy.consumeCredits(subscription))
                .isInstanceOf(InsufficientCreditsException.class);
    }

    @Test
    @DisplayName("refundCredits — incrementa currentCreditsNutri di 1")
    void refundCredits_incrementsCurrentCreditsNutri() {
        subscription.setCurrentCreditsNutri(1);
        strategy.refundCredits(subscription);
        assertThat(subscription.getCurrentCreditsNutri()).isEqualTo(2);
    }
}
