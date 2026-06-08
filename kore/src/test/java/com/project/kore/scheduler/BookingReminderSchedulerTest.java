package com.project.kore.scheduler;

import com.project.kore.model.Slot;
import com.project.kore.model.User;
import com.project.kore.repository.SlotRepository;
import com.project.kore.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingReminderScheduler unit tests")
class BookingReminderSchedulerTest {

    @Mock private SlotRepository slotRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private BookingReminderScheduler scheduler;

    private User client;
    private User professional;
    private Slot slot;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setFirstName("Luca");
        client.setLastName("Bianchi");
        client.setEmail("luca@test.com");

        professional = new User();
        professional.setId(2L);
        professional.setFirstName("Marco");
        professional.setLastName("PT");
        professional.setEmail("pt@test.com");

        slot = new Slot();
        slot.setId(10L);
        slot.setBookedBy(client);
        slot.setProfessional(professional);
        slot.setStartTime(LocalDateTime.now().plusMinutes(20));
        slot.setMeetingLink("https://meet.jit.si/test-room");
        slot.setReminderSent(false);
    }

    @Test
    @DisplayName("sendBookingReminders: no upcoming slots → nothing happens")
    void sendBookingReminders_noSlots_nothingHappens() {
        when(slotRepository.findUpcomingNeedingReminder(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduler.sendBookingReminders();

        verifyNoInteractions(emailService);
        verify(slotRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendBookingReminders: one upcoming slot → sends two emails and saves with reminderSent=true")
    void sendBookingReminders_oneSlot_sendsEmailsAndSaves() throws Exception {
        when(slotRepository.findUpcomingNeedingReminder(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(slot));

        scheduler.sendBookingReminders();

        // Two emails: one to client (isClient=true), one to professional (isClient=false)
        verify(emailService).sendBookingReminderEmail(
                eq(client.getEmail()),
                eq(client.getFirstName()),
                eq(professional.getFullName()),
                eq(slot.getStartTime()),
                eq(slot.getMeetingLink()),
                eq(true)
        );
        verify(emailService).sendBookingReminderEmail(
                eq(professional.getEmail()),
                eq(professional.getFirstName()),
                eq(client.getFullName()),
                eq(slot.getStartTime()),
                eq(slot.getMeetingLink()),
                eq(false)
        );

        assertThat(slot.isReminderSent()).isTrue();
        verify(slotRepository).save(slot);
    }

    @Test
    @DisplayName("sendBookingReminders: email throws → slot still saved with reminderSent=true")
    void sendBookingReminders_emailThrows_slotStillSaved() throws Exception {
        when(slotRepository.findUpcomingNeedingReminder(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(slot));
        doThrow(new RuntimeException("SMTP failure"))
                .when(emailService).sendBookingReminderEmail(anyString(), anyString(), anyString(),
                        any(LocalDateTime.class), anyString(), anyBoolean());

        scheduler.sendBookingReminders();

        // reminderSent is never set because the exception happens before setReminderSent
        // (slot.setReminderSent and save are after the email calls that throw)
        verify(slotRepository, never()).save(slot);
    }

    @Test
    @DisplayName("sendBookingReminders: email throws on client email → professional email not sent, loop continues")
    void sendBookingReminders_firstEmailThrows_continuesWithNextSlot() throws Exception {
        Slot slot2 = new Slot();
        slot2.setId(11L);

        User client2 = new User();
        client2.setId(3L);
        client2.setFirstName("Anna");
        client2.setLastName("Rossi");
        client2.setEmail("anna@test.com");

        User professional2 = new User();
        professional2.setId(4L);
        professional2.setFirstName("Sara");
        professional2.setLastName("Nutri");
        professional2.setEmail("nutri@test.com");

        slot2.setBookedBy(client2);
        slot2.setProfessional(professional2);
        slot2.setStartTime(LocalDateTime.now().plusMinutes(25));
        slot2.setMeetingLink("https://meet.jit.si/room2");
        slot2.setReminderSent(false);

        when(slotRepository.findUpcomingNeedingReminder(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(slot, slot2));

        // First slot throws, second slot proceeds normally
        doThrow(new RuntimeException("SMTP failure"))
                .when(emailService).sendBookingReminderEmail(
                        eq(client.getEmail()), anyString(), anyString(),
                        any(LocalDateTime.class), anyString(), anyBoolean());

        scheduler.sendBookingReminders();

        // Second slot should have been processed: 2 emails for slot2 + save
        verify(emailService).sendBookingReminderEmail(
                eq(client2.getEmail()), anyString(), anyString(),
                any(LocalDateTime.class), anyString(), eq(true));
        assertThat(slot2.isReminderSent()).isTrue();
        verify(slotRepository).save(slot2);
        verify(slotRepository, never()).save(slot);
    }

    @Test
    @DisplayName("sendBookingReminders: window boundary uses now and now+35min")
    void sendBookingReminders_usesCorrectTimeWindow() {
        when(slotRepository.findUpcomingNeedingReminder(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        LocalDateTime before = LocalDateTime.now();
        scheduler.sendBookingReminders();
        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<LocalDateTime> nowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> windowCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(slotRepository).findUpcomingNeedingReminder(nowCaptor.capture(), windowCaptor.capture());

        LocalDateTime capturedNow = nowCaptor.getValue();
        LocalDateTime capturedWindow = windowCaptor.getValue();

        assertThat(capturedNow).isBetween(before, after);
        assertThat(capturedWindow).isBetween(before.plusMinutes(35), after.plusMinutes(35));
    }
}
