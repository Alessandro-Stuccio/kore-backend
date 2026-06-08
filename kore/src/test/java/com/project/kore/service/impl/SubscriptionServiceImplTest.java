package com.project.kore.service.impl;

import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private User user;
    private Subscription activeSubscription;
    private Subscription inactiveSubscription;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("client@test.com");

        activeSubscription = new Subscription();
        activeSubscription.setId(100L);
        activeSubscription.setUser(user);
        activeSubscription.setActive(true);
        activeSubscription.setCurrentCreditsPT(2);
        activeSubscription.setCurrentCreditsNutri(2);

        inactiveSubscription = new Subscription();
        inactiveSubscription.setId(200L);
        inactiveSubscription.setUser(user);
        inactiveSubscription.setActive(false);
        inactiveSubscription.setCurrentCreditsPT(0);
        inactiveSubscription.setCurrentCreditsNutri(0);
    }

    // ---- getSubscriptionStatus ----

    @Test
    @DisplayName("getSubscriptionStatus: returns active subscription for user")
    void getSubscriptionStatus_activeSubscriptionExists_returnsIt() {
        when(subscriptionRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(activeSubscription));

        Subscription result = subscriptionService.getSubscriptionStatus(user);

        assertThat(result).isSameAs(activeSubscription);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    @DisplayName("getSubscriptionStatus: throws CustomResourceNotFoundException when user has no active subscription")
    void getSubscriptionStatus_noActiveSubscription_throwsCustomResourceNotFoundException() {
        when(subscriptionRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscriptionStatus(user))
                .isInstanceOf(CustomResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getSubscriptionStatus: uses findByUserAndActiveTrue (not findById) to query the subscription")
    void getSubscriptionStatus_usesCorrectRepositoryQuery() {
        when(subscriptionRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(activeSubscription));

        subscriptionService.getSubscriptionStatus(user);

        verify(subscriptionRepository).findByUserAndActiveTrue(user);
        verify(subscriptionRepository, never()).findById(any());
    }

    // ---- save ----

    @Test
    @DisplayName("save: persists subscription and returns the saved entity")
    void save_persistsAndReturnsSavedSubscription() {
        when(subscriptionRepository.save(activeSubscription)).thenReturn(activeSubscription);

        Subscription result = subscriptionService.save(activeSubscription);

        assertThat(result).isSameAs(activeSubscription);
        verify(subscriptionRepository).save(activeSubscription);
    }

    // ---- findActiveByUser ----

    @Test
    @DisplayName("findActiveByUser: returns Optional with subscription when active subscription exists")
    void findActiveByUser_exists_returnsOptionalWithSubscription() {
        when(subscriptionRepository.findByUserAndActiveTrue(user))
                .thenReturn(Optional.of(activeSubscription));

        Optional<Subscription> result = subscriptionService.findActiveByUser(user);

        assertThat(result).isPresent().contains(activeSubscription);
    }

    @Test
    @DisplayName("findActiveByUser: returns empty Optional when user has no active subscription")
    void findActiveByUser_noActiveSubscription_returnsEmptyOptional() {
        when(subscriptionRepository.findByUserAndActiveTrue(user)).thenReturn(Optional.empty());

        Optional<Subscription> result = subscriptionService.findActiveByUser(user);

        assertThat(result).isEmpty();
    }

    // ---- findActiveByUserWithLock ----

    @Test
    @DisplayName("findActiveByUserWithLock: returns Optional with locked active subscription")
    void findActiveByUserWithLock_exists_returnsOptionalWithSubscription() {
        when(subscriptionRepository.findByUserAndActiveTrueWithLock(user))
                .thenReturn(Optional.of(activeSubscription));

        Optional<Subscription> result = subscriptionService.findActiveByUserWithLock(user);

        assertThat(result).isPresent().contains(activeSubscription);
        verify(subscriptionRepository).findByUserAndActiveTrueWithLock(user);
    }

    @Test
    @DisplayName("findActiveByUserWithLock: returns empty Optional when no active subscription is found under lock")
    void findActiveByUserWithLock_notFound_returnsEmptyOptional() {
        when(subscriptionRepository.findByUserAndActiveTrueWithLock(user))
                .thenReturn(Optional.empty());

        Optional<Subscription> result = subscriptionService.findActiveByUserWithLock(user);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findActiveByUserWithLock: uses the WITH_LOCK variant, not the standard findByUserAndActiveTrue")
    void findActiveByUserWithLock_doesNotDelegateToStandardQuery() {
        when(subscriptionRepository.findByUserAndActiveTrueWithLock(user))
                .thenReturn(Optional.of(activeSubscription));

        subscriptionService.findActiveByUserWithLock(user);

        verify(subscriptionRepository).findByUserAndActiveTrueWithLock(user);
        verify(subscriptionRepository, never()).findByUserAndActiveTrue(any());
    }

    // ---- getAllSubscriptions ----

    @Test
    @DisplayName("getAllSubscriptions: returns all subscriptions from repository")
    void getAllSubscriptions_returnsAll() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(activeSubscription, inactiveSubscription));

        List<Subscription> result = subscriptionService.getAllSubscriptions();

        assertThat(result).hasSize(2).containsExactlyInAnyOrder(activeSubscription, inactiveSubscription);
        verify(subscriptionRepository).findAll();
    }

    @Test
    @DisplayName("getAllSubscriptions: returns empty list when no subscriptions exist")
    void getAllSubscriptions_empty_returnsEmptyList() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        assertThat(subscriptionService.getAllSubscriptions()).isEmpty();
    }

    // ---- updateSubscriptionCredits ----

    @Test
    @DisplayName("updateSubscriptionCredits: loads subscription, updates PT and Nutri credits, then saves")
    void updateSubscriptionCredits_validId_updatesAndSaves() {
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(activeSubscription)).thenReturn(activeSubscription);

        Subscription result = subscriptionService.updateSubscriptionCredits(100L, 3, 1);

        assertThat(result.getCurrentCreditsPT()).isEqualTo(3);
        assertThat(result.getCurrentCreditsNutri()).isEqualTo(1);
        verify(subscriptionRepository).save(activeSubscription);
    }

    @Test
    @DisplayName("updateSubscriptionCredits: sets both credits to zero correctly")
    void updateSubscriptionCredits_setToZero_persistsZeroValues() {
        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(activeSubscription)).thenReturn(activeSubscription);

        subscriptionService.updateSubscriptionCredits(100L, 0, 0);

        assertThat(activeSubscription.getCurrentCreditsPT()).isZero();
        assertThat(activeSubscription.getCurrentCreditsNutri()).isZero();
    }

    @Test
    @DisplayName("updateSubscriptionCredits: throws ResourceNotFoundException when subscription id does not exist")
    void updateSubscriptionCredits_notFound_throwsResourceNotFoundException() {
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.updateSubscriptionCredits(999L, 2, 2))
                .isInstanceOf(CustomResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateSubscriptionCredits: returns the updated subscription returned by save")
    void updateSubscriptionCredits_returnsEntityFromSave() {
        Subscription updatedSub = new Subscription();
        updatedSub.setId(100L);
        updatedSub.setCurrentCreditsPT(5);
        updatedSub.setCurrentCreditsNutri(5);

        when(subscriptionRepository.findById(100L)).thenReturn(Optional.of(activeSubscription));
        when(subscriptionRepository.save(activeSubscription)).thenReturn(updatedSub);

        Subscription result = subscriptionService.updateSubscriptionCredits(100L, 5, 5);

        assertThat(result).isSameAs(updatedSub);
    }

    // ---- hasSubscribersByPlan ----

    @Test
    @DisplayName("hasSubscribersByPlan: returns true when at least one subscription references the plan")
    void hasSubscribersByPlan_hasSubs_returnsTrue() {
        when(subscriptionRepository.existsByPlanId(42L)).thenReturn(true);

        assertThat(subscriptionService.hasSubscribersByPlan(42L)).isTrue();
        verify(subscriptionRepository).existsByPlanId(42L);
    }

    @Test
    @DisplayName("hasSubscribersByPlan: returns false when no subscription references the plan")
    void hasSubscribersByPlan_noSubs_returnsFalse() {
        when(subscriptionRepository.existsByPlanId(99L)).thenReturn(false);

        assertThat(subscriptionService.hasSubscribersByPlan(99L)).isFalse();
    }
}
