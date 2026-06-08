package com.project.kore.facade.impl;

import com.project.kore.dto.response.BookingResponse;
import com.project.kore.dto.response.SlotDTO;
import com.project.kore.dto.response.stats.ProfessionalStatsResponse;
import com.project.kore.enums.BookingStatus;
import com.project.kore.enums.DocumentType;
import com.project.kore.enums.Role;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.mapper.BookingMapper;
import com.project.kore.mapper.SlotMapper;
import com.project.kore.model.Document;
import com.project.kore.model.Slot;
import com.project.kore.model.User;
import com.project.kore.model.WeeklySchedule;
import com.project.kore.service.DocumentService;
import com.project.kore.service.SlotService;
import com.project.kore.service.UserService;
import com.project.kore.service.WeeklyScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfessionalFacadeImpl unit tests")
class ProfessionalFacadeImplTest {

    @Mock private UserService userService;
    @Mock private SlotService slotService;
    @Mock private WeeklyScheduleService weeklyScheduleService;
    @Mock private DocumentService documentService;
    @Mock private SlotMapper slotMapper;
    @Mock private BookingMapper bookingMapper;

    @InjectMocks
    private ProfessionalFacadeImpl facade;

    private User ptUser;
    private User nutriUser;
    private User clientUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        ptUser = new User();
        ptUser.setId(1L);
        ptUser.setFirstName("Alice");
        ptUser.setLastName("Smith");
        ptUser.setRole(Role.PERSONAL_TRAINER);

        nutriUser = new User();
        nutriUser.setId(2L);
        nutriUser.setFirstName("Bob");
        nutriUser.setLastName("Jones");
        nutriUser.setRole(Role.NUTRITIONIST);

        clientUser = new User();
        clientUser.setId(3L);
        clientUser.setFirstName("Carlo");
        clientUser.setLastName("Rossi");
        clientUser.setRole(Role.CLIENT);

        adminUser = new User();
        adminUser.setId(4L);
        adminUser.setRole(Role.ADMIN);
    }

    // ─── getAvailableSlots ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getAvailableSlots: delegates to slotService and maps result")
    void getAvailableSlots_delegatesAndMaps() {
        List<Slot> slots = List.of(new Slot());
        List<SlotDTO> dtos = List.of(new SlotDTO());

        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotService.getAvailableSlots(ptUser)).thenReturn(slots);
        when(slotMapper.toDtoList(slots)).thenReturn(dtos);

        List<SlotDTO> result = facade.getAvailableSlots(1L);

        assertThat(result).isEqualTo(dtos);
    }

    @Test
    @DisplayName("getAvailableSlots: returns empty list when no slots available")
    void getAvailableSlots_empty_returnsEmpty() {
        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotService.getAvailableSlots(ptUser)).thenReturn(List.of());
        when(slotMapper.toDtoList(List.of())).thenReturn(List.of());

        List<SlotDTO> result = facade.getAvailableSlots(1L);

        assertThat(result).isEmpty();
    }

    // ─── createSlots ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createSlots PT: maps to entities, creates via service, and returns DTOs")
    void createSlots_personalTrainer_success() {
        List<SlotDTO> inputDtos = List.of(new SlotDTO());
        List<Slot> entities = List.of(new Slot());
        List<Slot> savedEntities = List.of(new Slot());
        List<SlotDTO> expectedDtos = List.of(new SlotDTO());

        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotMapper.toEntityList(inputDtos, ptUser)).thenReturn(entities);
        when(slotService.createSlots(entities)).thenReturn(savedEntities);
        when(slotMapper.toDtoList(savedEntities)).thenReturn(expectedDtos);

        List<SlotDTO> result = facade.createSlots(1L, inputDtos);

        assertThat(result).isEqualTo(expectedDtos);
    }

    @Test
    @DisplayName("createSlots NUTRITIONIST: succeeds")
    void createSlots_nutritionist_success() {
        List<SlotDTO> inputDtos = List.of(new SlotDTO());
        List<Slot> entities = List.of(new Slot());
        List<Slot> savedEntities = List.of(new Slot());
        List<SlotDTO> expectedDtos = List.of(new SlotDTO());

        when(userService.getUserById(2L)).thenReturn(nutriUser);
        when(slotMapper.toEntityList(inputDtos, nutriUser)).thenReturn(entities);
        when(slotService.createSlots(entities)).thenReturn(savedEntities);
        when(slotMapper.toDtoList(savedEntities)).thenReturn(expectedDtos);

        List<SlotDTO> result = facade.createSlots(2L, inputDtos);

        assertThat(result).isEqualTo(expectedDtos);
    }

    @Test
    @DisplayName("createSlots: throws AccessDeniedException when user is not a professional")
    void createSlots_notProfessional_throwsUnauthorized() {
        when(userService.getUserById(3L)).thenReturn(clientUser);

        assertThatThrownBy(() -> facade.createSlots(3L, List.of()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("professionisti");
    }

    // ─── deleteSlot ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteSlot: deletes slot when requester owns it and slot is not booked")
    void deleteSlot_ownerAndNotBooked_success() {
        Slot slot = new Slot();
        slot.setId(10L);
        slot.setProfessional(ptUser);
        slot.setStatus(null);
        slot.setBookedBy(null);

        when(slotService.getSlot(10L)).thenReturn(slot);

        facade.deleteSlot(10L, 1L);

        verify(slotService).deleteSlot(10L);
    }

    @Test
    @DisplayName("deleteSlot: throws AccessDeniedException when requester does not own the slot")
    void deleteSlot_notOwner_throwsUnauthorized() {
        Slot slot = new Slot();
        slot.setId(10L);
        slot.setProfessional(ptUser);  // owned by ptUser (id=1)

        when(slotService.getSlot(10L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.deleteSlot(10L, 99L))  // requester id=99 != owner
                .isInstanceOf(AccessDeniedException.class);

        verify(slotService, never()).deleteSlot(anyLong());
    }

    @Test
    @DisplayName("deleteSlot: throws IllegalStateException when slot is already booked (bookedBy not null)")
    void deleteSlot_alreadyBooked_throwsIllegalState() {
        Slot slot = new Slot();
        slot.setId(10L);
        slot.setProfessional(ptUser);
        slot.setBookedBy(clientUser);
        slot.setStatus(null);

        when(slotService.getSlot(10L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.deleteSlot(10L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prenotato");
    }

    @Test
    @DisplayName("deleteSlot: throws IllegalStateException when slot status is CONFIRMED even with null bookedBy")
    void deleteSlot_confirmedStatus_throwsIllegalState() {
        Slot slot = new Slot();
        slot.setId(10L);
        slot.setProfessional(ptUser);
        slot.setBookedBy(null);
        slot.setStatus(BookingStatus.CONFIRMED);

        when(slotService.getSlot(10L)).thenReturn(slot);

        assertThatThrownBy(() -> facade.deleteSlot(10L, 1L))
                .isInstanceOf(IllegalStateException.class);
    }

    // ─── generateSlotsFromSchedule ───────────────────────────────────────────────

    @Test
    @DisplayName("generateSlotsFromSchedule: throws AccessDeniedException for non-professional user")
    void generateSlotsFromSchedule_notProfessional_throwsUnauthorized() {
        assertThatThrownBy(() -> facade.generateSlotsFromSchedule(
                clientUser, LocalDate.now(), LocalDate.now().plusDays(1)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("generateSlotsFromSchedule: generates slots when schedule covers a matching day")
    void generateSlotsFromSchedule_withMatchingSchedule_createsSlots() {
        // Monday range
        LocalDate monday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        LocalDate tuesday = monday.plusDays(1);

        WeeklySchedule schedule = new WeeklySchedule();
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(10, 0));  // 2 slots: 09:00-09:30 and 09:30-10:00

        when(weeklyScheduleService.findByProfessional(ptUser)).thenReturn(List.of(schedule));
        when(slotService.slotExists(eq(ptUser), any(LocalDateTime.class))).thenReturn(false);
        when(slotService.createSlots(any())).thenReturn(List.of());

        facade.generateSlotsFromSchedule(ptUser, monday, tuesday);

        ArgumentCaptor<List<Slot>> captor = ArgumentCaptor.forClass(List.class);
        verify(slotService).createSlots(captor.capture());

        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("generateSlotsFromSchedule: skips slots that already exist")
    void generateSlotsFromSchedule_existingSlots_skipped() {
        LocalDate monday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        WeeklySchedule schedule = new WeeklySchedule();
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(10, 0));

        when(weeklyScheduleService.findByProfessional(ptUser)).thenReturn(List.of(schedule));
        // All slots already exist
        when(slotService.slotExists(eq(ptUser), any(LocalDateTime.class))).thenReturn(true);

        facade.generateSlotsFromSchedule(ptUser, monday, monday);

        verify(slotService, never()).createSlots(any());
    }

    @Test
    @DisplayName("generateSlotsFromSchedule: does not call createSlots when no schedule matches range")
    void generateSlotsFromSchedule_noMatchingSchedule_noSlotsCreated() {
        LocalDate monday = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        WeeklySchedule schedule = new WeeklySchedule();
        schedule.setDayOfWeek(DayOfWeek.SUNDAY);  // no sunday in the monday-only range
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(10, 0));

        when(weeklyScheduleService.findByProfessional(ptUser)).thenReturn(List.of(schedule));

        facade.generateSlotsFromSchedule(ptUser, monday, monday);

        verify(slotService, never()).createSlots(any());
    }

    // ─── getUpcomingBookings ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getUpcomingBookings PT: returns only future bookings, sorted by startTime")
    void getUpcomingBookings_pt_returnsFutureBookingsSorted() {
        Slot futureSlot1 = new Slot();
        futureSlot1.setStartTime(LocalDateTime.now().plusDays(2));

        Slot futureSlot2 = new Slot();
        futureSlot2.setStartTime(LocalDateTime.now().plusDays(1));

        Slot pastSlot = new Slot();
        pastSlot.setStartTime(LocalDateTime.now().minusDays(1));

        BookingResponse resp1 = new BookingResponse();
        BookingResponse resp2 = new BookingResponse();

        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotService.findBookingsByProfessional(ptUser))
                .thenReturn(List.of(futureSlot1, futureSlot2, pastSlot));
        when(bookingMapper.toResponse(futureSlot2)).thenReturn(resp2);
        when(bookingMapper.toResponse(futureSlot1)).thenReturn(resp1);

        List<BookingResponse> result = facade.getUpcomingBookings(1L);

        assertThat(result).hasSize(2);
        // sorted by startTime ascending: futureSlot2 (D+1) before futureSlot1 (D+2)
        assertThat(result.get(0)).isEqualTo(resp2);
        assertThat(result.get(1)).isEqualTo(resp1);
    }

    @Test
    @DisplayName("getUpcomingBookings: throws AccessDeniedException when user is not a professional")
    void getUpcomingBookings_notProfessional_throwsUnauthorized() {
        when(userService.getUserById(3L)).thenReturn(clientUser);

        assertThatThrownBy(() -> facade.getUpcomingBookings(3L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("professionisti");
    }

    @Test
    @DisplayName("getUpcomingBookings: slot with null startTime is excluded")
    void getUpcomingBookings_nullStartTime_excluded() {
        Slot nullStartSlot = new Slot();
        nullStartSlot.setStartTime(null);

        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotService.findBookingsByProfessional(ptUser)).thenReturn(List.of(nullStartSlot));

        List<BookingResponse> result = facade.getUpcomingBookings(1L);

        assertThat(result).isEmpty();
        verify(bookingMapper, never()).toResponse(any());
    }

    // ─── getProfessionalStats ────────────────────────────────────────────────────

    @Test
    @DisplayName("getProfessionalStats: throws AccessDeniedException when user is not a professional")
    void getProfessionalStats_notProfessional_throwsUnauthorized() {
        when(userService.getUserById(3L)).thenReturn(clientUser);

        assertThatThrownBy(() -> facade.getProfessionalStats(3L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getProfessionalStats PT: returns stats with todayBookings count")
    void getProfessionalStats_pt_todayBookingsCount() {
        Slot todaySlot = buildBookedSlot(LocalDateTime.now().withHour(10).withMinute(0),
                LocalDateTime.now().withHour(10).withMinute(30), clientUser);

        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotService.findTodayByProfessional(eq(ptUser), any(), any())).thenReturn(List.of(todaySlot));
        when(userService.findByAssignedPT(ptUser)).thenReturn(List.of());
        when(documentService.countUploadedSince(eq(ptUser), any())).thenReturn(3);

        ProfessionalStatsResponse result = facade.getProfessionalStats(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTodayBookingsCount()).isEqualTo(1);
        assertThat(result.getDocsUploadedThisWeek()).isEqualTo(3);
    }

    @Test
    @DisplayName("getProfessionalStats PT: client needing attention has no recent WORKOUT_PLAN doc")
    void getProfessionalStats_pt_clientNeedsAttention_noRecentDoc() {
        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotService.findTodayByProfessional(eq(ptUser), any(), any())).thenReturn(List.of());
        when(userService.findByAssignedPT(ptUser)).thenReturn(List.of(clientUser));
        // Latest document is older than 7 days
        Document oldDoc = new Document();
        oldDoc.setUploadDate(LocalDateTime.now().minusDays(10));
        when(documentService.findLatestByOwnerAndType(clientUser, DocumentType.WORKOUT_PLAN)).thenReturn(oldDoc);
        when(documentService.countUploadedSince(eq(ptUser), any())).thenReturn(0);

        ProfessionalStatsResponse result = facade.getProfessionalStats(1L);

        assertThat(result.getClientsNeedingAttentionCount()).isEqualTo(1);
        assertThat(result.getClientsNeedingAttention().get(0).getFirstName()).isEqualTo("Carlo");
    }

    @Test
    @DisplayName("getProfessionalStats PT: client NOT needing attention has a recent WORKOUT_PLAN doc")
    void getProfessionalStats_pt_clientDoesNotNeedAttention_recentDoc() {
        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotService.findTodayByProfessional(eq(ptUser), any(), any())).thenReturn(List.of());
        when(userService.findByAssignedPT(ptUser)).thenReturn(List.of(clientUser));
        Document recentDoc = new Document();
        recentDoc.setUploadDate(LocalDateTime.now().minusDays(2));
        when(documentService.findLatestByOwnerAndType(clientUser, DocumentType.WORKOUT_PLAN)).thenReturn(recentDoc);
        when(documentService.countUploadedSince(eq(ptUser), any())).thenReturn(0);

        ProfessionalStatsResponse result = facade.getProfessionalStats(1L);

        assertThat(result.getClientsNeedingAttentionCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("getProfessionalStats NUTRITIONIST: uses DIET_PLAN document type for attention check")
    void getProfessionalStats_nutritionist_usesDietPlanDocType() {
        when(userService.getUserById(2L)).thenReturn(nutriUser);
        when(slotService.findTodayByProfessional(eq(nutriUser), any(), any())).thenReturn(List.of());
        when(userService.findByAssignedNutritionist(nutriUser)).thenReturn(List.of(clientUser));
        when(documentService.findLatestByOwnerAndType(clientUser, DocumentType.DIET_PLAN)).thenReturn(null);
        when(documentService.countUploadedSince(eq(nutriUser), any())).thenReturn(0);

        ProfessionalStatsResponse result = facade.getProfessionalStats(2L);

        verify(documentService).findLatestByOwnerAndType(clientUser, DocumentType.DIET_PLAN);
        assertThat(result.getClientsNeedingAttentionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getProfessionalStats: totalClients reflects assigned client count")
    void getProfessionalStats_totalClientsCount() {
        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotService.findTodayByProfessional(eq(ptUser), any(), any())).thenReturn(List.of());
        when(userService.findByAssignedPT(ptUser)).thenReturn(List.of(clientUser, new User()));
        when(documentService.findLatestByOwnerAndType(any(), eq(DocumentType.WORKOUT_PLAN))).thenReturn(null);
        when(documentService.countUploadedSince(eq(ptUser), any())).thenReturn(0);

        ProfessionalStatsResponse result = facade.getProfessionalStats(1L);

        assertThat(result.getTotalClients()).isEqualTo(2);
    }

    @Test
    @DisplayName("getProfessionalStats: today booking con bookedBy null e status null")
    void getProfessionalStats_pt_todayBookingNullBookedByAndStatus() {
        Slot unbookedSlot = new Slot();
        unbookedSlot.setId(11L);
        unbookedSlot.setStartTime(LocalDateTime.now().plusHours(1));
        unbookedSlot.setEndTime(LocalDateTime.now().plusHours(1).plusMinutes(30));
        unbookedSlot.setBookedBy(null);
        unbookedSlot.setStatus(null);

        when(userService.getUserById(1L)).thenReturn(ptUser);
        when(slotService.findTodayByProfessional(eq(ptUser), any(), any())).thenReturn(List.of(unbookedSlot));
        when(userService.findByAssignedPT(ptUser)).thenReturn(List.of());
        when(documentService.countUploadedSince(eq(ptUser), any())).thenReturn(0);

        ProfessionalStatsResponse result = facade.getProfessionalStats(1L);

        assertThat(result.getTodayBookingsCount()).isEqualTo(1);
        assertThat(result.getTodayBookings().get(0).getClientName()).isEqualTo("");
        assertThat(result.getTodayBookings().get(0).getClientId()).isNull();
        assertThat(result.getTodayBookings().get(0).getStatus()).isEqualTo("");
    }

    // ─── helpers ─────────────────────────────────────────────────────────────────

    private Slot buildBookedSlot(LocalDateTime start, LocalDateTime end, User bookedBy) {
        Slot slot = new Slot();
        slot.setId(10L);
        slot.setStartTime(start);
        slot.setEndTime(end);
        slot.setBookedBy(bookedBy);
        slot.setStatus(BookingStatus.CONFIRMED);
        return slot;
    }
}
