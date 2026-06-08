package com.project.kore.service.impl;

import com.project.kore.enums.BookingStatus;
import com.project.kore.exception.booking.SlotAlreadyBookedException;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Slot;
import com.project.kore.model.User;
import com.project.kore.repository.SlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @Mock
    private SlotRepository slotRepository;

    @InjectMocks
    private SlotServiceImpl slotService;

    private User professional;
    private User client;
    private Slot availableSlot;
    private Slot bookedSlot;

    @BeforeEach
    void setUp() {
        professional = new User();
        professional.setId(1L);
        professional.setEmail("pt@test.com");

        client = new User();
        client.setId(2L);
        client.setEmail("client@test.com");

        availableSlot = new Slot();
        availableSlot.setId(10L);
        availableSlot.setProfessional(professional);
        availableSlot.setStartTime(LocalDateTime.now().plusDays(1));
        availableSlot.setEndTime(LocalDateTime.now().plusDays(1).plusMinutes(30));

        bookedSlot = new Slot();
        bookedSlot.setId(20L);
        bookedSlot.setProfessional(professional);
        bookedSlot.setBookedBy(client);
        bookedSlot.setStatus(BookingStatus.CONFIRMED);
        bookedSlot.setMeetingLink("https://meet.jit.si/room-abc");
        bookedSlot.setStartTime(LocalDateTime.now().plusDays(2));
        bookedSlot.setEndTime(LocalDateTime.now().plusDays(2).plusMinutes(30));
        bookedSlot.setBookedAt(LocalDateTime.now().minusHours(1));
    }

    // ---- createSlots ----

    @Test
    @DisplayName("createSlots: saves and returns all provided slots")
    void createSlots_savesAndReturnsAll() {
        List<Slot> input = List.of(availableSlot, bookedSlot);
        when(slotRepository.saveAll(input)).thenReturn(input);

        List<Slot> result = slotService.createSlots(input);

        assertThat(result).hasSize(2).containsExactlyElementsOf(input);
        verify(slotRepository).saveAll(input);
    }

    @Test
    @DisplayName("createSlots: returns empty list when given empty input")
    void createSlots_emptyList_returnsEmpty() {
        when(slotRepository.saveAll(List.of())).thenReturn(List.of());

        List<Slot> result = slotService.createSlots(List.of());

        assertThat(result).isEmpty();
    }

    // ---- getAvailableSlots ----

    @Test
    @DisplayName("getAvailableSlots: delegates to repository and returns free slots for professional")
    void getAvailableSlots_returnsFreeSlots() {
        when(slotRepository.findByProfessionalAndBookedByIsNull(professional))
                .thenReturn(List.of(availableSlot));

        List<Slot> result = slotService.getAvailableSlots(professional);

        assertThat(result).hasSize(1).containsExactly(availableSlot);
        verify(slotRepository).findByProfessionalAndBookedByIsNull(professional);
    }

    @Test
    @DisplayName("getAvailableSlots: returns empty list when professional has no free slots")
    void getAvailableSlots_noFreeSlots_returnsEmpty() {
        when(slotRepository.findByProfessionalAndBookedByIsNull(professional))
                .thenReturn(List.of());

        List<Slot> result = slotService.getAvailableSlots(professional);

        assertThat(result).isEmpty();
    }

    // ---- getSlot ----

    @Test
    @DisplayName("getSlot: returns slot when found by id")
    void getSlot_found_returnsSlot() {
        when(slotRepository.findById(10L)).thenReturn(Optional.of(availableSlot));

        Slot result = slotService.getSlot(10L);

        assertThat(result).isSameAs(availableSlot);
    }

    @Test
    @DisplayName("getSlot: throws ResourceNotFoundException when slot id does not exist")
    void getSlot_notFound_throwsResourceNotFoundException() {
        when(slotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.getSlot(99L))
                .isInstanceOf(CustomResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- saveBooking ----

    @Test
    @DisplayName("saveBooking: confirms booking, sets status CONFIRMED, meetingLink, bookedAt and saves")
    void saveBooking_availableSlot_setsAllFieldsAndSaves() {
        when(slotRepository.findByIdWithLock(10L)).thenReturn(Optional.of(availableSlot));
        when(slotRepository.save(availableSlot)).thenReturn(availableSlot);

        LocalDateTime before = LocalDateTime.now();
        Slot result = slotService.saveBooking(10L, client, "https://meet.jit.si/room-xyz");
        LocalDateTime after = LocalDateTime.now();

        assertThat(result.getBookedBy()).isSameAs(client);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.getMeetingLink()).isEqualTo("https://meet.jit.si/room-xyz");
        assertThat(result.getBookedAt()).isBetween(before, after);
        verify(slotRepository).save(availableSlot);
    }

    @Test
    @DisplayName("saveBooking: throws SlotAlreadyBookedException when slot is already taken")
    void saveBooking_alreadyBooked_throwsSlotAlreadyBookedException() {
        when(slotRepository.findByIdWithLock(20L)).thenReturn(Optional.of(bookedSlot));

        assertThatThrownBy(() -> slotService.saveBooking(20L, client, "https://meet.jit.si/room-new"))
                .isInstanceOf(SlotAlreadyBookedException.class);

        verify(slotRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveBooking: throws ResourceNotFoundException when slot id is not found (with lock)")
    void saveBooking_slotNotFound_throwsResourceNotFoundException() {
        when(slotRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.saveBooking(999L, client, "https://meet.jit.si/room"))
                .isInstanceOf(CustomResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ---- deleteSlot ----

    @Test
    @DisplayName("deleteSlot: deletes slot when it exists")
    void deleteSlot_exists_callsDeleteById() {
        when(slotRepository.existsById(10L)).thenReturn(true);

        slotService.deleteSlot(10L);

        verify(slotRepository).deleteById(10L);
    }

    @Test
    @DisplayName("deleteSlot: throws ResourceNotFoundException when slot does not exist")
    void deleteSlot_notFound_throwsResourceNotFoundException() {
        when(slotRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> slotService.deleteSlot(99L))
                .isInstanceOf(CustomResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(slotRepository, never()).deleteById(any());
    }

    // ---- cancelBooking ----

    @Test
    @DisplayName("cancelBooking: clears bookedBy, meetingLink, bookedAt and sets status CANCELED")
    void cancelBooking_bookedSlot_clearsAllBookingFields() {
        when(slotRepository.findById(20L)).thenReturn(Optional.of(bookedSlot));
        when(slotRepository.save(bookedSlot)).thenReturn(bookedSlot);

        slotService.cancelBooking(20L, client.getId());

        assertThat(bookedSlot.getBookedBy()).isNull();
        assertThat(bookedSlot.getStatus()).isEqualTo(BookingStatus.CANCELED);
        assertThat(bookedSlot.getMeetingLink()).isNull();
        assertThat(bookedSlot.getBookedAt()).isNull();
        verify(slotRepository).save(bookedSlot);
    }

    @Test
    @DisplayName("cancelBooking: throws ResourceNotFoundException when slot id does not exist")
    void cancelBooking_slotNotFound_throwsResourceNotFoundException() {
        when(slotRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.cancelBooking(999L, 1L))
                .isInstanceOf(CustomResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(slotRepository, never()).save(any());
    }

    // ---- findRecentByUser ----

    @Test
    @DisplayName("findRecentByUser: delegates to repository with user and since parameter")
    void findRecentByUser_delegatesToRepository() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        when(slotRepository.findRecentByBookedBy(client, since)).thenReturn(List.of(bookedSlot));

        List<Slot> result = slotService.findRecentByUser(client, since);

        assertThat(result).containsExactly(bookedSlot);
        verify(slotRepository).findRecentByBookedBy(client, since);
    }

    // ---- findRecentByProfessional ----

    @Test
    @DisplayName("findRecentByProfessional: delegates to repository with professional and since parameter")
    void findRecentByProfessional_delegatesToRepository() {
        LocalDateTime since = LocalDateTime.now().minusDays(3);
        when(slotRepository.findRecentByProfessional(professional, since)).thenReturn(List.of(bookedSlot));

        List<Slot> result = slotService.findRecentByProfessional(professional, since);

        assertThat(result).containsExactly(bookedSlot);
        verify(slotRepository).findRecentByProfessional(professional, since);
    }

    // ---- findBookingsByProfessional ----

    @Test
    @DisplayName("findBookingsByProfessional: returns only slots where bookedBy is not null")
    void findBookingsByProfessional_filtersUnbookedSlots() {
        when(slotRepository.findByProfessional(professional))
                .thenReturn(List.of(availableSlot, bookedSlot));

        List<Slot> result = slotService.findBookingsByProfessional(professional);

        assertThat(result).hasSize(1).containsExactly(bookedSlot);
    }

    @Test
    @DisplayName("findBookingsByProfessional: returns empty list when all slots are unbooked")
    void findBookingsByProfessional_allUnbooked_returnsEmpty() {
        Slot anotherFree = new Slot();
        anotherFree.setId(30L);
        when(slotRepository.findByProfessional(professional))
                .thenReturn(List.of(availableSlot, anotherFree));

        List<Slot> result = slotService.findBookingsByProfessional(professional);

        assertThat(result).isEmpty();
    }

    // ---- findFutureByUser ----

    @Test
    @DisplayName("findFutureByUser: delegates to repository with user and from timestamp")
    void findFutureByUser_delegatesToRepository() {
        LocalDateTime from = LocalDateTime.now();
        when(slotRepository.findFutureByBookedBy(client, from)).thenReturn(List.of(bookedSlot));

        List<Slot> result = slotService.findFutureByUser(client, from);

        assertThat(result).containsExactly(bookedSlot);
        verify(slotRepository).findFutureByBookedBy(client, from);
    }

    // ---- slotExists ----

    @Test
    @DisplayName("slotExists: returns true when repository confirms existence")
    void slotExists_exists_returnsTrue() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        when(slotRepository.existsByProfessionalAndStartTime(professional, startTime)).thenReturn(true);

        assertThat(slotService.slotExists(professional, startTime)).isTrue();
    }

    @Test
    @DisplayName("slotExists: returns false when slot does not exist for that professional and time")
    void slotExists_notExists_returnsFalse() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(5);
        when(slotRepository.existsByProfessionalAndStartTime(professional, startTime)).thenReturn(false);

        assertThat(slotService.slotExists(professional, startTime)).isFalse();
    }

    // ---- getAllBookedSlots ----

    @Test
    @DisplayName("getAllBookedSlots: returns all booked slots from repository")
    void getAllBookedSlots_returnsAllBooked() {
        when(slotRepository.findAllBooked()).thenReturn(List.of(bookedSlot));

        List<Slot> result = slotService.getAllBookedSlots();

        assertThat(result).containsExactly(bookedSlot);
        verify(slotRepository).findAllBooked();
    }

    @Test
    @DisplayName("getAllBookedSlots: returns empty list when no slots have been booked yet")
    void getAllBookedSlots_noneBooked_returnsEmpty() {
        when(slotRepository.findAllBooked()).thenReturn(List.of());

        assertThat(slotService.getAllBookedSlots()).isEmpty();
    }

    // ---- logBookingCreated ----

    @Test
    @DisplayName("logBookingCreated: sets bookedAt and saves when bookedAt is null")
    void logBookingCreated_bookedAtNull_setsTimestampAndSaves() {
        availableSlot.setBookedAt(null);

        LocalDateTime before = LocalDateTime.now();
        slotService.logBookingCreated(availableSlot);
        LocalDateTime after = LocalDateTime.now();

        assertThat(availableSlot.getBookedAt()).isBetween(before, after);
        verify(slotRepository).save(availableSlot);
    }

    @Test
    @DisplayName("logBookingCreated: does NOT save when bookedAt is already set")
    void logBookingCreated_bookedAtAlreadySet_doesNotSave() {
        LocalDateTime existing = LocalDateTime.now().minusMinutes(5);
        bookedSlot.setBookedAt(existing);

        slotService.logBookingCreated(bookedSlot);

        verify(slotRepository, never()).save(any());
        assertThat(bookedSlot.getBookedAt()).isEqualTo(existing);
    }

    // ---- findTodayByProfessional ----

    @Test
    @DisplayName("findTodayByProfessional: delegates to repository with professional and day range")
    void findTodayByProfessional_delegatesToRepository() {
        LocalDateTime dayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        when(slotRepository.findTodayByProfessional(professional, dayStart, dayEnd))
                .thenReturn(List.of(bookedSlot));

        List<Slot> result = slotService.findTodayByProfessional(professional, dayStart, dayEnd);

        assertThat(result).containsExactly(bookedSlot);
        verify(slotRepository).findTodayByProfessional(professional, dayStart, dayEnd);
    }

    // ---- hasBookingBetween ----

    @Test
    @DisplayName("hasBookingBetween: returns true when client has booked with professional")
    void hasBookingBetween_existingBooking_returnsTrue() {
        when(slotRepository.existsByBookedByIdAndProfessionalId(2L, 1L)).thenReturn(true);

        assertThat(slotService.hasBookingBetween(2L, 1L)).isTrue();
    }

    @Test
    @DisplayName("hasBookingBetween: returns false when no booking exists between client and professional")
    void hasBookingBetween_noBooking_returnsFalse() {
        when(slotRepository.existsByBookedByIdAndProfessionalId(2L, 1L)).thenReturn(false);

        assertThat(slotService.hasBookingBetween(2L, 1L)).isFalse();
    }
}
