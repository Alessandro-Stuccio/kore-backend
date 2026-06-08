package com.project.kore.service.impl;

import com.project.kore.enums.BookingStatus;
import com.project.kore.exception.booking.SlotAlreadyBookedException;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.Slot;
import com.project.kore.model.User;
import com.project.kore.repository.SlotRepository;
import com.project.kore.service.SlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ciclo di vita degli slot e delle prenotazioni. La prenotazione prende un lock
 * pessimistico sulla riga (findByIdWithLock con PESSIMISTIC_WRITE) per evitare il
 * double-booking quando due clienti tentano lo stesso slot in contemporanea.
 */
@Service
public class SlotServiceImpl implements SlotService {

    private static final Logger log = LoggerFactory.getLogger(SlotServiceImpl.class);
    private final SlotRepository slotRepository;

    public SlotServiceImpl(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public List<Slot> createSlots(List<Slot> slots) {
        return slotRepository.saveAll(slots);
    }

    @Override
    public List<Slot> getAvailableSlots(User professional) {
        return slotRepository.findByProfessionalAndBookedByIsNull(professional);
    }

    @Override
    public Slot getSlot(Long slotId) {
        return slotRepository.findById(slotId)
                .orElseThrow(() -> new CustomResourceNotFoundException("Slot", slotId));
    }

    /**
     * Prende il lock pessimistico sulla riga, ricontrolla che lo slot sia ancora libero
     * e lo segna come prenotato (CONFIRMED) con link e timestamp. Se nel frattempo qualcuno
     * lo ha occupato lancia SlotAlreadyBookedException; un eventuale conflitto di versione
     * ottimistica viene tradotto altrove dal GlobalExceptionHandler.
     */
    @Override
    public Slot saveBooking(Long slotId, User user, String meetingLink) {
        Slot slot = slotRepository.findByIdWithLock(slotId)
                .orElseThrow(() -> new CustomResourceNotFoundException("Slot", slotId));

        if (slot.getBookedBy() != null) {
            throw new SlotAlreadyBookedException("Slot non più disponibile");
        }

        slot.setBookedBy(user);
        slot.setStatus(BookingStatus.CONFIRMED);
        slot.setMeetingLink(meetingLink);
        slot.setBookedAt(LocalDateTime.now());
        return slotRepository.save(slot);
    }

    @Override
    public void deleteSlot(Long slotId) {
        if (!slotRepository.existsById(slotId)) {
            throw new CustomResourceNotFoundException("Slot", slotId);
        }
        slotRepository.deleteById(slotId);
    }

    /**
     * Libera lo slot azzerando i dati della prenotazione e portandolo a CANCELED.
     * Il rimborso dei crediti avviene a monte, nella facade, prima di chiamare qui.
     */
    @Override
    public void cancelBooking(Long slotId, Long userId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new CustomResourceNotFoundException("Prenotazione", slotId));
        slot.setBookedBy(null);
        slot.setStatus(BookingStatus.CANCELED);
        slot.setMeetingLink(null);
        slot.setBookedAt(null);
        slotRepository.save(slot);
    }

    @Override
    public List<Slot> findRecentByUser(User user, LocalDateTime since) {
        return slotRepository.findRecentByBookedBy(user, since);
    }

    @Override
    public List<Slot> findRecentByProfessional(User professional, LocalDateTime since) {
        return slotRepository.findRecentByProfessional(professional, since);
    }

    @Override
    public List<Slot> findBookingsByProfessional(User professional) {
        return slotRepository.findByProfessional(professional).stream()
                .filter(s -> s.getBookedBy() != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Slot> findFutureByUser(User user, LocalDateTime from) {
        return slotRepository.findFutureByBookedBy(user, from);
    }

    @Override
    public boolean slotExists(User professional, LocalDateTime startTime) {
        return slotRepository.existsByProfessionalAndStartTime(professional, startTime);
    }

    @Override
    public List<Slot> getAllBookedSlots() {
        return slotRepository.findAllBooked();
    }

    /**
     * Registra la prenotazione sull'activity feed. Se manca bookedAt lo valorizza ora e
     * salva lo slot, così il timestamp resta coerente anche se la prenotazione arriva da
     * un percorso che non l'aveva impostato.
     */
    @Override
    public void logBookingCreated(Slot slot) {
        if (slot.getBookedAt() == null) {
            slot.setBookedAt(LocalDateTime.now());
            slotRepository.save(slot);
            log.info("ActivityFeed [Observer]: timestamp bookedAt registrato per slot ID={}", slot.getId());
        } else {
            log.info("ActivityFeed [Observer]: slot ID={} già registrato (bookedAt={}).", slot.getId(), slot.getBookedAt());
        }
    }

    @Override
    public List<Slot> findTodayByProfessional(User professional, LocalDateTime dayStart, LocalDateTime dayEnd) {
        return slotRepository.findTodayByProfessional(professional, dayStart, dayEnd);
    }

    @Override
    public boolean hasBookingBetween(Long clientId, Long professionalId) {
        return slotRepository.existsByBookedByIdAndProfessionalId(clientId, professionalId);
    }
}
