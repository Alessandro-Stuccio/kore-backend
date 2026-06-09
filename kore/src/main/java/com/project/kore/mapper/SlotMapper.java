package com.project.kore.mapper;

import com.project.kore.dto.response.SlotDTO;
import com.project.kore.model.Slot;
import com.project.kore.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte gli slot tra entità e DTO.
 */
@Component
public class SlotMapper {

    /**
     * Converte uno slot nel suo DTO; {@code isAvailable} è true finché nessuno ha prenotato.
     *
     * @param slot lo slot da convertire
     * @return il DTO dello slot, oppure {@code null} se l'input è {@code null}
     */
    public SlotDTO toDto(Slot slot) {
        if (slot == null) return null;
        return SlotDTO.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.getBookedBy() == null)
                .professionalId(slot.getProfessional().getId())
                .build();
    }

    /**
     * Converte una lista di slot nei rispettivi DTO.
     *
     * @param slots gli slot da convertire
     * @return i DTO degli slot
     */
    public List<SlotDTO> toDtoList(List<Slot> slots) {
        return slots.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Crea uno slot libero per il professionista: i campi di prenotazione restano vuoti.
     *
     * @param dto          dati dello slot (orari)
     * @param professional il professionista titolare
     * @return il nuovo slot libero
     */
    public Slot toEntity(SlotDTO dto, User professional) {
        Slot slot = new Slot();
        slot.setProfessional(professional);
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        return slot;
    }

    /**
     * Converte una lista di DTO in slot, tutti assegnati allo stesso professionista.
     *
     * @param dtos         gli slot da creare
     * @param professional il professionista titolare
     * @return i nuovi slot
     */
    public List<Slot> toEntityList(List<SlotDTO> dtos, User professional) {
        return dtos.stream().map(dto -> toEntity(dto, professional)).collect(Collectors.toList());
    }
}
