package com.project.kore.mapper;

import com.project.kore.dto.response.BookingResponse;
import com.project.kore.enums.BookingStatus;
import com.project.kore.enums.Role;
import com.project.kore.model.Slot;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    private BookingMapper bookingMapper;

    @BeforeEach
    void setUp() {
        bookingMapper = new BookingMapper();
    }

    // ---- helpers ----

    private User buildProfessional() {
        User pt = new User();
        pt.setId(10L);
        pt.setFirstName("Marco");
        pt.setLastName("Rossi");
        pt.setRole(Role.PERSONAL_TRAINER);
        return pt;
    }

    private User buildClient() {
        User client = new User();
        client.setId(1L);
        client.setFirstName("Luca");
        client.setLastName("Bianchi");
        client.setRole(Role.CLIENT);
        return client;
    }

    private Slot buildSlot(LocalDateTime start, User professional, User bookedBy) {
        Slot slot = new Slot();
        slot.setId(100L);
        slot.setStartTime(start);
        slot.setEndTime(start.plusMinutes(30));
        slot.setProfessional(professional);
        slot.setBookedBy(bookedBy);
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setMeetingLink("https://meet.jit.si/room");
        return slot;
    }

    // ---- toResponse: null guard ----

    @Test
    @DisplayName("toResponse: returns null for null slot")
    void toResponse_nullSlot_returnsNull() {
        assertThat(bookingMapper.toResponse(null)).isNull();
    }

    // ---- toResponse: field mapping ----

    @Test
    @DisplayName("toResponse: maps id, date, startTime, endTime correctly")
    void toResponse_mapsDateAndTimeFields() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 15, 10, 0);
        Slot slot = buildSlot(start, buildProfessional(), buildClient());

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getDate()).isEqualTo("2025-06-15");
        assertThat(response.getStartTime()).isEqualTo("10:00");
        assertThat(response.getEndTime()).isEqualTo("10:30");
    }

    @Test
    @DisplayName("toResponse: maps professionalName from slot professional")
    void toResponse_mapsProfessionalName() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 15, 10, 0);
        Slot slot = buildSlot(start, buildProfessional(), null);

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.getProfessionalName()).isEqualTo("Marco Rossi");
        assertThat(response.getProfessionalRole()).isEqualTo(Role.PERSONAL_TRAINER);
    }

    @Test
    @DisplayName("toResponse: maps clientName from bookedBy when present")
    void toResponse_bookedByPresent_mapsClientName() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 15, 10, 0);
        Slot slot = buildSlot(start, buildProfessional(), buildClient());

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.getClientName()).isEqualTo("Luca Bianchi");
    }

    @Test
    @DisplayName("toResponse: clientName is empty string when bookedBy is null")
    void toResponse_bookedByNull_clientNameIsEmpty() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 15, 10, 0);
        Slot slot = buildSlot(start, buildProfessional(), null);

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.getClientName()).isEmpty();
    }

    @Test
    @DisplayName("toResponse: maps meetingLink and status from slot")
    void toResponse_mapsMeetingLinkAndStatus() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 15, 10, 0);
        Slot slot = buildSlot(start, buildProfessional(), buildClient());

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.getMeetingLink()).isEqualTo("https://meet.jit.si/room");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    // ---- canJoin logic ----

    @Test
    @DisplayName("canJoin: true when slot starts in 5 minutes (within 10-minute early window)")
    void toResponse_slotStartsIn5Minutes_canJoinTrue() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        Slot slot = buildSlot(start, buildProfessional(), buildClient());

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.isCanJoin()).isTrue();
    }

    @Test
    @DisplayName("canJoin: false when slot starts in 15 minutes (outside 10-minute early window)")
    void toResponse_slotStartsIn15Minutes_canJoinFalse() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(15);
        Slot slot = buildSlot(start, buildProfessional(), buildClient());

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.isCanJoin()).isFalse();
    }

    @Test
    @DisplayName("canJoin: true when slot started 20 minutes ago (within 30-minute post-start window)")
    void toResponse_slotStarted20MinutesAgo_canJoinTrue() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(20);
        Slot slot = buildSlot(start, buildProfessional(), buildClient());

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.isCanJoin()).isTrue();
    }

    @Test
    @DisplayName("canJoin: false when slot started 35 minutes ago (past 30-minute post-start window)")
    void toResponse_slotStarted35MinutesAgo_canJoinFalse() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(35);
        Slot slot = buildSlot(start, buildProfessional(), buildClient());

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.isCanJoin()).isFalse();
    }

    @Test
    @DisplayName("canJoin: true exactly at start time (boundary)")
    void toResponse_exactlyAtStartTime_canJoinTrue() {
        // Slight margin to avoid clock drift in the test runner
        LocalDateTime start = LocalDateTime.now().minusSeconds(1);
        Slot slot = buildSlot(start, buildProfessional(), buildClient());

        BookingResponse response = bookingMapper.toResponse(slot);

        assertThat(response.isCanJoin()).isTrue();
    }

    @Test
    @DisplayName("isMeetingJoinable: null startTime restituisce false")
    void isMeetingJoinable_nullStartTime_returnsFalse() throws Exception {
        java.lang.reflect.Method method = BookingMapper.class.getDeclaredMethod("isMeetingJoinable", LocalDateTime.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(bookingMapper, (LocalDateTime) null);

        assertThat(result).isFalse();
    }
}
