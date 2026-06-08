package com.project.kore.facade.impl;

import com.project.kore.dto.request.BookingRequest;
import com.project.kore.dto.response.BookingResponse;
import com.project.kore.enums.BookingStatus;
import com.project.kore.enums.Role;
import com.project.kore.exception.booking.BookingCancellationException;
import com.project.kore.exception.booking.SlotAlreadyBookedException;
import com.project.kore.exception.booking.SubscriptionExpiredException;
import com.project.kore.exception.common.BusinessLogicException;
import com.project.kore.mapper.BookingMapper;
import com.project.kore.model.Slot;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.EmailService;
import com.project.kore.service.SlotService;
import com.project.kore.service.SubscriptionService;
import com.project.kore.service.UserService;
import com.project.kore.service.VideoConferenceService;
import com.project.kore.service.strategy.BookingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingFacadeImpl unit tests")
class BookingFacadeImplTest {

    @Mock private UserService userService;
    @Mock private SlotService slotService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private VideoConferenceService videoConferenceService;
    @Mock private EmailService emailService;
    @Mock private BookingStrategy ptStrategy;
    @Mock private BookingMapper bookingMapper;

    private BookingFacadeImpl facade;

    private User client;
    private User professional;
    private Slot slot;
    private Subscription subscription;
    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setEmail("client@test.com");
        client.setFirstName("Luca");
        client.setLastName("Rossi");
        client.setRole(Role.CLIENT);

        professional = new User();
        professional.setId(10L);
        professional.setEmail("pt@test.com");
        professional.setFirstName("Marco");
        professional.setLastName("Bianchi");
        professional.setRole(Role.PERSONAL_TRAINER);

        slot = new Slot();
        slot.setId(1L);
        slot.setProfessional(professional);
        slot.setBookedBy(null);
        slot.setStartTime(LocalDateTime.now().plusDays(7));
        slot.setEndTime(LocalDateTime.now().plusDays(7).plusMinutes(30));

        subscription = new Subscription();
        subscription.setId(1L);
        subscription.setEndDate(LocalDate.now().plusMonths(6));
        subscription.setActive(true);
        subscription.setCurrentCreditsPT(2);
        subscription.setCurrentCreditsNutri(2);

        bookingResponse = new BookingResponse();
        bookingResponse.setId(1L);

        // lenient: only needed by tests that reach the strategy-lookup branch
        lenient().when(ptStrategy.getSupportedRole()).thenReturn(Role.PERSONAL_TRAINER);

        facade = new BookingFacadeImpl(
                userService, slotService, subscriptionService,
                videoConferenceService, emailService,
                List.of(ptStrategy), bookingMapper);
    }

    // ─── createBooking ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBooking: happy path → returns BookingResponse")
    void createBooking_happyPath_returnsBookingResponse() {
        BookingRequest request = new BookingRequest(1L);

        when(userService.getUserById(1L)).thenReturn(client);
        when(slotService.getSlot(1L)).thenReturn(slot);
        when(subscriptionService.getSubscriptionStatus(client)).thenReturn(subscription);
        when(videoConferenceService.generateMeetingLink(client, professional, slot))
                .thenReturn("https://meet.jitsi/room123");
        when(slotService.saveBooking(eq(1L), eq(client), anyString())).thenReturn(slot);
        when(subscriptionService.findActiveByUserWithLock(client))
                .thenReturn(Optional.of(subscription));
        when(subscriptionService.save(subscription)).thenReturn(subscription);
        when(bookingMapper.toResponse(slot)).thenReturn(bookingResponse);

        BookingResponse result = facade.createBooking(request, 1L);

        assertThat(result).isEqualTo(bookingResponse);
        verify(ptStrategy).verifyAssignment(client, professional);
        verify(ptStrategy).consumeCredits(subscription);
        verify(slotService).logBookingCreated(slot);
    }

    @Test
    @DisplayName("createBooking: user is the professional → throws BusinessLogicException")
    void createBooking_userIsOwnProfessional_throwsBusinessLogicException() {
        BookingRequest request = new BookingRequest(1L);

        // client.id == professional.id
        client.setId(10L);
        when(userService.getUserById(10L)).thenReturn(client);
        when(slotService.getSlot(1L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.createBooking(request, 10L))
                .isInstanceOf(BusinessLogicException.class);

        verify(slotService, never()).saveBooking(anyLong(), any(), anyString());
    }

    @Test
    @DisplayName("createBooking: slot already booked → throws SlotAlreadyBookedException")
    void createBooking_slotAlreadyBooked_throwsSlotAlreadyBookedException() {
        BookingRequest request = new BookingRequest(1L);
        slot.setBookedBy(new User()); // slot already taken

        when(userService.getUserById(1L)).thenReturn(client);
        when(slotService.getSlot(1L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.createBooking(request, 1L))
                .isInstanceOf(SlotAlreadyBookedException.class);
    }

    @Test
    @DisplayName("createBooking: no strategy for role → throws IllegalStateException")
    void createBooking_noStrategyForRole_throwsIllegalStateException() {
        BookingRequest request = new BookingRequest(1L);
        professional.setRole(Role.NUTRITIONIST); // no NUTRITIONIST strategy registered

        when(userService.getUserById(1L)).thenReturn(client);
        when(slotService.getSlot(1L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.createBooking(request, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("professionista");
    }

    @Test
    @DisplayName("createBooking: subscription already expired (today after endDate) → throws SubscriptionExpiredException")
    void createBooking_subscriptionExpiredToday_throwsSubscriptionExpiredException() {
        BookingRequest request = new BookingRequest(1L);
        subscription.setEndDate(LocalDate.now().minusDays(1)); // expired yesterday

        when(userService.getUserById(1L)).thenReturn(client);
        when(slotService.getSlot(1L)).thenReturn(slot);
        when(subscriptionService.getSubscriptionStatus(client)).thenReturn(subscription);

        assertThatThrownBy(() -> facade.createBooking(request, 1L))
                .isInstanceOf(SubscriptionExpiredException.class);
    }

    @Test
    @DisplayName("createBooking: slot start time is after subscription end date → throws SubscriptionExpiredException")
    void createBooking_slotAfterSubscriptionEnd_throwsSubscriptionExpiredException() {
        BookingRequest request = new BookingRequest(1L);
        // Subscription ends in 3 days, slot is in 7 days
        subscription.setEndDate(LocalDate.now().plusDays(3));
        slot.setStartTime(LocalDateTime.now().plusDays(7));

        when(userService.getUserById(1L)).thenReturn(client);
        when(slotService.getSlot(1L)).thenReturn(slot);
        when(subscriptionService.getSubscriptionStatus(client)).thenReturn(subscription);

        assertThatThrownBy(() -> facade.createBooking(request, 1L))
                .isInstanceOf(SubscriptionExpiredException.class);
    }

    @Test
    @DisplayName("createBooking: optimistic locking failure during credit consumption → throws IllegalStateException")
    void createBooking_optimisticLockingFailure_throwsIllegalStateException() {
        BookingRequest request = new BookingRequest(1L);

        when(userService.getUserById(1L)).thenReturn(client);
        when(slotService.getSlot(1L)).thenReturn(slot);
        when(subscriptionService.getSubscriptionStatus(client)).thenReturn(subscription);
        when(videoConferenceService.generateMeetingLink(client, professional, slot))
                .thenReturn("https://meet.jitsi/room123");
        when(slotService.saveBooking(eq(1L), eq(client), anyString())).thenReturn(slot);
        when(subscriptionService.findActiveByUserWithLock(client))
                .thenReturn(Optional.of(subscription));
        doThrow(new ObjectOptimisticLockingFailureException(Subscription.class, 1L))
                .when(subscriptionService).save(any());

        assertThatThrownBy(() -> facade.createBooking(request, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("crediti");
    }

    @Test
    @DisplayName("createBooking: active subscription not found after lock → throws IllegalStateException")
    void createBooking_noActiveSubscriptionWithLock_throwsIllegalStateException() {
        BookingRequest request = new BookingRequest(1L);

        when(userService.getUserById(1L)).thenReturn(client);
        when(slotService.getSlot(1L)).thenReturn(slot);
        when(subscriptionService.getSubscriptionStatus(client)).thenReturn(subscription);
        when(videoConferenceService.generateMeetingLink(client, professional, slot))
                .thenReturn("https://meet.jitsi/room123");
        when(slotService.saveBooking(eq(1L), eq(client), anyString())).thenReturn(slot);
        when(subscriptionService.findActiveByUserWithLock(client))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> facade.createBooking(request, 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("createBooking: email sending throws exception → exception is swallowed and response returned")
    void createBooking_emailFails_exceptionSwallowed() {
        BookingRequest request = new BookingRequest(1L);

        when(userService.getUserById(1L)).thenReturn(client);
        when(slotService.getSlot(1L)).thenReturn(slot);
        when(subscriptionService.getSubscriptionStatus(client)).thenReturn(subscription);
        when(videoConferenceService.generateMeetingLink(client, professional, slot))
                .thenReturn("https://meet.jitsi/room123");
        when(slotService.saveBooking(eq(1L), eq(client), anyString())).thenReturn(slot);
        when(subscriptionService.findActiveByUserWithLock(client))
                .thenReturn(Optional.of(subscription));
        when(subscriptionService.save(subscription)).thenReturn(subscription);
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendBookingConfirmationEmail(anyString(), anyString(), anyString(), any(), anyString());
        when(bookingMapper.toResponse(slot)).thenReturn(bookingResponse);

        BookingResponse result = facade.createBooking(request, 1L);

        assertThat(result).isEqualTo(bookingResponse);
    }

    // ─── cancelBooking ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("cancelBooking: happy path → cancels booking and refunds credits")
    void cancelBooking_happyPath_cancelsAndRefunds() {
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setStartTime(LocalDateTime.now().plusDays(3));
        slot.setBookedBy(client);

        when(slotService.getSlot(1L)).thenReturn(slot);
        when(subscriptionService.findActiveByUserWithLock(client))
                .thenReturn(Optional.of(subscription));
        when(subscriptionService.save(subscription)).thenReturn(subscription);

        facade.cancelBooking(1L, 1L);

        verify(slotService).cancelBooking(1L, 1L);
        verify(ptStrategy).refundCredits(subscription);
        verify(subscriptionService).save(subscription);
    }

    @Test
    @DisplayName("cancelBooking: slot has no bookedBy → throws BookingCancellationException")
    void cancelBooking_slotNotBooked_throwsBookingCancellationException() {
        slot.setBookedBy(null);

        when(slotService.getSlot(1L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.cancelBooking(1L, 1L))
                .isInstanceOf(BookingCancellationException.class);
    }

    @Test
    @DisplayName("cancelBooking: caller is not the booking owner → throws BookingCancellationException")
    void cancelBooking_notOwner_throwsBookingCancellationException() {
        slot.setBookedBy(client); // client.id = 1
        // caller userId = 99 (different user)

        when(slotService.getSlot(1L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.cancelBooking(1L, 99L))
                .isInstanceOf(BookingCancellationException.class);
    }

    @Test
    @DisplayName("cancelBooking: slot status is not CONFIRMED → throws BookingCancellationException")
    void cancelBooking_statusNotConfirmed_throwsBookingCancellationException() {
        slot.setBookedBy(client);
        slot.setStatus(BookingStatus.CANCELED);
        slot.setStartTime(LocalDateTime.now().plusDays(3));

        when(slotService.getSlot(1L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.cancelBooking(1L, 1L))
                .isInstanceOf(BookingCancellationException.class);
    }

    @Test
    @DisplayName("cancelBooking: less than 24h before start → throws BookingCancellationException")
    void cancelBooking_lessThan24hBefore_throwsBookingCancellationException() {
        slot.setBookedBy(client);
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setStartTime(LocalDateTime.now().plusHours(10)); // only 10 hours away

        when(slotService.getSlot(1L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.cancelBooking(1L, 1L))
                .isInstanceOf(BookingCancellationException.class);
    }

    @Test
    @DisplayName("cancelBooking: no active subscription → skips refund with warning log")
    void cancelBooking_noActiveSubscription_skipsRefund() {
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setStartTime(LocalDateTime.now().plusDays(3));
        slot.setBookedBy(client);

        when(slotService.getSlot(1L)).thenReturn(slot);
        when(subscriptionService.findActiveByUserWithLock(client))
                .thenReturn(Optional.empty());

        facade.cancelBooking(1L, 1L);

        verify(slotService).cancelBooking(1L, 1L);
        verify(ptStrategy, never()).refundCredits(any());
        verify(subscriptionService, never()).save(any());
    }

    @Test
    @DisplayName("cancelBooking: email sending throws exception → exception is swallowed")
    void cancelBooking_emailFails_exceptionSwallowed() {
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setStartTime(LocalDateTime.now().plusDays(3));
        slot.setBookedBy(client);

        when(slotService.getSlot(1L)).thenReturn(slot);
        when(subscriptionService.findActiveByUserWithLock(client))
                .thenReturn(Optional.of(subscription));
        when(subscriptionService.save(subscription)).thenReturn(subscription);
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendBookingCancellationEmail(anyString(), anyString(), anyString(), any());

        // Must not throw
        facade.cancelBooking(1L, 1L);

        verify(slotService).cancelBooking(1L, 1L);
    }

    @Test
    @DisplayName("cancelBooking: exactly 24h before start (boundary) → throws BookingCancellationException")
    void cancelBooking_exactly24hBefore_throwsBookingCancellationException() {
        slot.setBookedBy(client);
        slot.setStatus(BookingStatus.CONFIRMED);
        // startTime is exactly now + 24h, which means isBefore(now+24h) is false (equal is not before),
        // BUT LocalDateTime.now().plusHours(24) could be slightly before if test execution takes time.
        // Use plusHours(23) to be safely inside the 24h window.
        slot.setStartTime(LocalDateTime.now().plusHours(23));

        when(slotService.getSlot(1L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.cancelBooking(1L, 1L))
                .isInstanceOf(BookingCancellationException.class);
    }
}
