package com.project.kore.mapper;

import com.project.kore.dto.response.SlotDTO;
import com.project.kore.enums.Role;
import com.project.kore.model.Slot;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlotMapperTest {

    private SlotMapper slotMapper;

    @BeforeEach
    void setUp() {
        slotMapper = new SlotMapper();
    }

    // ---- helpers ----

    private User buildProfessional(Long id) {
        User u = new User();
        u.setId(id);
        u.setFirstName("Marco");
        u.setLastName("Rossi");
        u.setRole(Role.PERSONAL_TRAINER);
        return u;
    }

    private User buildClient(Long id) {
        User u = new User();
        u.setId(id);
        u.setFirstName("Luca");
        u.setLastName("Bianchi");
        u.setRole(Role.CLIENT);
        return u;
    }

    private Slot buildSlot(Long id, User professional, User bookedBy, LocalDateTime start) {
        Slot slot = new Slot();
        slot.setId(id);
        slot.setProfessional(professional);
        slot.setBookedBy(bookedBy);
        slot.setStartTime(start);
        slot.setEndTime(start.plusMinutes(30));
        return slot;
    }

    private SlotDTO buildSlotDTO(Long id, LocalDateTime start, Long professionalId) {
        return SlotDTO.builder()
                .id(id)
                .startTime(start)
                .endTime(start.plusMinutes(30))
                .isAvailable(true)
                .professionalId(professionalId)
                .build();
    }

    // ---- toDto: null guard ----

    @Test
    @DisplayName("toDto: returns null for null slot")
    void toDto_nullSlot_returnsNull() {
        assertThat(slotMapper.toDto(null)).isNull();
    }

    // ---- toDto: field mapping ----

    @Test
    @DisplayName("toDto: maps id, startTime, endTime, and professionalId")
    void toDto_mapsBaseFields() {
        User professional = buildProfessional(10L);
        LocalDateTime start = LocalDateTime.of(2025, 6, 1, 9, 0);
        Slot slot = buildSlot(50L, professional, null, start);

        SlotDTO dto = slotMapper.toDto(slot);

        assertThat(dto.getId()).isEqualTo(50L);
        assertThat(dto.getStartTime()).isEqualTo(start);
        assertThat(dto.getEndTime()).isEqualTo(start.plusMinutes(30));
        assertThat(dto.getProfessionalId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("toDto: isAvailable=true when bookedBy is null")
    void toDto_noBookedBy_isAvailableTrue() {
        User professional = buildProfessional(10L);
        Slot slot = buildSlot(51L, professional, null, LocalDateTime.now());

        SlotDTO dto = slotMapper.toDto(slot);

        assertThat(dto.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("toDto: isAvailable=false when slot is booked")
    void toDto_withBookedBy_isAvailableFalse() {
        User professional = buildProfessional(10L);
        User client = buildClient(1L);
        Slot slot = buildSlot(52L, professional, client, LocalDateTime.now());

        SlotDTO dto = slotMapper.toDto(slot);

        assertThat(dto.isAvailable()).isFalse();
    }

    // ---- toDtoList ----

    @Test
    @DisplayName("toDtoList: maps all slots in list")
    void toDtoList_mapsAllSlots() {
        User professional = buildProfessional(10L);
        LocalDateTime base = LocalDateTime.of(2025, 6, 1, 9, 0);
        Slot slot1 = buildSlot(1L, professional, null, base);
        Slot slot2 = buildSlot(2L, professional, null, base.plusHours(1));

        List<SlotDTO> result = slotMapper.toDtoList(List.of(slot1, slot2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toDtoList: returns empty list for empty input")
    void toDtoList_emptyInput_returnsEmptyList() {
        assertThat(slotMapper.toDtoList(List.of())).isEmpty();
    }

    // ---- toEntity ----

    @Test
    @DisplayName("toEntity: creates Slot with professional and times from DTO")
    void toEntity_mapsFields() {
        User professional = buildProfessional(10L);
        LocalDateTime start = LocalDateTime.of(2025, 6, 1, 10, 0);
        SlotDTO dto = buildSlotDTO(null, start, 10L);

        Slot slot = slotMapper.toEntity(dto, professional);

        assertThat(slot.getProfessional()).isSameAs(professional);
        assertThat(slot.getStartTime()).isEqualTo(start);
        assertThat(slot.getEndTime()).isEqualTo(start.plusMinutes(30));
    }

    @Test
    @DisplayName("toEntity: bookedBy is not set (slot is free)")
    void toEntity_bookedByIsNull() {
        User professional = buildProfessional(10L);
        LocalDateTime start = LocalDateTime.of(2025, 6, 1, 10, 0);
        SlotDTO dto = buildSlotDTO(null, start, 10L);

        Slot slot = slotMapper.toEntity(dto, professional);

        assertThat(slot.getBookedBy()).isNull();
    }

    // ---- toEntityList ----

    @Test
    @DisplayName("toEntityList: maps all DTOs assigning same professional to each")
    void toEntityList_mapsAllWithSameProfessional() {
        User professional = buildProfessional(10L);
        LocalDateTime base = LocalDateTime.of(2025, 6, 1, 9, 0);
        SlotDTO dto1 = buildSlotDTO(null, base, 10L);
        SlotDTO dto2 = buildSlotDTO(null, base.plusHours(1), 10L);

        List<Slot> slots = slotMapper.toEntityList(List.of(dto1, dto2), professional);

        assertThat(slots).hasSize(2);
        assertThat(slots.get(0).getProfessional()).isSameAs(professional);
        assertThat(slots.get(1).getProfessional()).isSameAs(professional);
        assertThat(slots.get(0).getStartTime()).isEqualTo(base);
        assertThat(slots.get(1).getStartTime()).isEqualTo(base.plusHours(1));
    }

    @Test
    @DisplayName("toEntityList: returns empty list for empty input")
    void toEntityList_emptyInput_returnsEmptyList() {
        User professional = buildProfessional(10L);
        assertThat(slotMapper.toEntityList(List.of(), professional)).isEmpty();
    }
}
