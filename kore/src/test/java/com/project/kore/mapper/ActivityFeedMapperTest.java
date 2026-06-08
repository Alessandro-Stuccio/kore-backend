package com.project.kore.mapper;

import com.project.kore.dto.response.ActivityFeedItemResponse;
import com.project.kore.enums.DocumentType;
import com.project.kore.enums.Role;
import com.project.kore.model.Document;
import com.project.kore.model.Slot;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityFeedMapperTest {

    private ActivityFeedMapper activityFeedMapper;

    @BeforeEach
    void setUp() {
        activityFeedMapper = new ActivityFeedMapper();
    }

    // ---- helpers ----

    private User buildUser(Long id, String firstName, String lastName, Role role) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setRole(role);
        return u;
    }

    private Slot buildSlot(User professional, User bookedBy, LocalDateTime startTime) {
        Slot slot = new Slot();
        slot.setId(100L);
        slot.setProfessional(professional);
        slot.setBookedBy(bookedBy);
        slot.setStartTime(startTime);
        slot.setEndTime(startTime.plusMinutes(30));
        return slot;
    }

    private Document buildDocument(User uploadedBy, User owner, DocumentType type, LocalDateTime uploadDate) {
        Document doc = new Document();
        doc.setId(200L);
        doc.setFileName("file.pdf");
        doc.setType(type);
        doc.setUploadedBy(uploadedBy);
        doc.setOwner(owner);
        doc.setUploadDate(uploadDate);
        return doc;
    }

    // ---- CLIENT view: slot items ----

    @Test
    @DisplayName("CLIENT: slot item text includes 'PT' for PERSONAL_TRAINER")
    void clientView_ptSlot_textHasPT() {
        User pt = buildUser(10L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        User client = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        Slot slot = buildSlot(pt, client, LocalDateTime.of(2025, 6, 1, 10, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(slot), List.of(), client);

        assertThat(result).hasSize(1);
        ActivityFeedItemResponse item = result.get(0);
        assertThat(item.getType()).isEqualTo("Booking");
        assertThat(item.getText()).isEqualTo("Appuntamento prenotato con PT Marco");
        assertThat(item.getTimestamp()).isEqualTo(LocalDateTime.of(2025, 6, 1, 10, 0));
    }

    @Test
    @DisplayName("CLIENT: slot item text includes 'Nutrizionista' for NUTRITIONIST")
    void clientView_nutritionistSlot_textHasNutrizionista() {
        User nutri = buildUser(11L, "Sara", "Neri", Role.NUTRITIONIST);
        User client = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        Slot slot = buildSlot(nutri, client, LocalDateTime.of(2025, 6, 2, 11, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(slot), List.of(), client);

        assertThat(result.get(0).getText()).isEqualTo("Appuntamento prenotato con Nutrizionista Sara");
    }

    // ---- PROFESSIONAL view: slot items ----

    @Test
    @DisplayName("PERSONAL_TRAINER: slot item text includes client fullName")
    void professionalView_slotWithClient_textHasClientName() {
        User pt = buildUser(10L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        User client = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        Slot slot = buildSlot(pt, client, LocalDateTime.of(2025, 6, 1, 10, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(slot), List.of(), pt);

        assertThat(result.get(0).getText()).isEqualTo("Luca Bianchi ha prenotato un appuntamento");
    }

    @Test
    @DisplayName("PERSONAL_TRAINER: slot item with no bookedBy has empty client name")
    void professionalView_slotNoBookedBy_textHasEmptyName() {
        User pt = buildUser(10L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Slot slot = buildSlot(pt, null, LocalDateTime.of(2025, 6, 1, 10, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(slot), List.of(), pt);

        assertThat(result.get(0).getText()).isEqualTo(" ha prenotato un appuntamento");
    }

    // ---- CLIENT view: document items ----

    @Test
    @DisplayName("CLIENT: document item text includes type desc and uploader firstName")
    void clientView_document_textHasTypeAndUploader() {
        User uploader = buildUser(10L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        User client = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        Document doc = buildDocument(uploader, client, DocumentType.DIET_PLAN,
                LocalDateTime.of(2025, 5, 1, 8, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(), List.of(doc), client);

        assertThat(result).hasSize(1);
        ActivityFeedItemResponse item = result.get(0);
        assertThat(item.getType()).isEqualTo("Document");
        assertThat(item.getText()).isEqualTo("dieta caricata da Marco");
        assertThat(item.getTimestamp()).isEqualTo(LocalDateTime.of(2025, 5, 1, 8, 0));
    }

    @Test
    @DisplayName("CLIENT: document text uses 'Sistema' when uploadedBy is null")
    void clientView_documentNullUploader_usesDefaultSistema() {
        User client = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        Document doc = buildDocument(null, client, DocumentType.WORKOUT_PLAN,
                LocalDateTime.of(2025, 5, 2, 9, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(), List.of(doc), client);

        assertThat(result.get(0).getText()).isEqualTo("scheda di allenamento caricata da Sistema");
    }

    // ---- PROFESSIONAL view: document items ----

    @Test
    @DisplayName("PERSONAL_TRAINER: document item text includes type desc and owner fullName")
    void professionalView_document_textHasTypeAndOwner() {
        User pt = buildUser(10L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        User owner = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        Document doc = buildDocument(pt, owner, DocumentType.WORKOUT_PLAN,
                LocalDateTime.of(2025, 5, 3, 10, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(), List.of(doc), pt);

        assertThat(result.get(0).getText()).isEqualTo("scheda di allenamento caricata per Luca Bianchi");
    }

    @Test
    @DisplayName("PERSONAL_TRAINER: document text has empty owner when owner is null")
    void professionalView_documentNullOwner_emptyOwnerName() {
        User pt = buildUser(10L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        Document doc = buildDocument(pt, null, DocumentType.INSURANCE_POLICE,
                LocalDateTime.of(2025, 5, 4, 11, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(), List.of(doc), pt);

        assertThat(result.get(0).getText()).isEqualTo("polizza caricata per ");
    }

    // ---- sorting by timestamp ----

    @Test
    @DisplayName("result is sorted by timestamp descending (most recent first)")
    void result_sortedByTimestampDescending() {
        User pt = buildUser(10L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        User client = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);

        LocalDateTime older = LocalDateTime.of(2025, 5, 1, 8, 0);
        LocalDateTime newer = LocalDateTime.of(2025, 6, 1, 10, 0);

        Slot slot = buildSlot(pt, client, older);
        Document doc = buildDocument(pt, client, DocumentType.DIET_PLAN, newer);

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(slot), List.of(doc), client);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTimestamp()).isEqualTo(newer);
        assertThat(result.get(1).getTimestamp()).isEqualTo(older);
    }

    // ---- mixed empty inputs ----

    @Test
    @DisplayName("returns empty list when both slots and documents are empty")
    void emptyInputs_returnsEmptyList() {
        User client = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(), List.of(), client);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("aggregates both slot and document items correctly")
    void aggregates_slotsAndDocuments() {
        User pt = buildUser(10L, "Marco", "Rossi", Role.PERSONAL_TRAINER);
        User client = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);

        Slot slot = buildSlot(pt, client, LocalDateTime.of(2025, 6, 1, 10, 0));
        Document doc = buildDocument(pt, client, DocumentType.DIET_PLAN,
                LocalDateTime.of(2025, 6, 1, 12, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(slot), List.of(doc), client);

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(ActivityFeedItemResponse::getType))
                .containsExactlyInAnyOrder("Booking", "Document");
    }

    @Test
    @DisplayName("NUTRITIONIST: document item uses professional view (caricata per)")
    void nutritionistView_document_textHasCaricataPer() {
        User nutri = buildUser(11L, "Sara", "Neri", Role.NUTRITIONIST);
        User owner = buildUser(1L, "Luca", "Bianchi", Role.CLIENT);
        Document doc = buildDocument(nutri, owner, DocumentType.DIET_PLAN,
                LocalDateTime.of(2025, 5, 5, 9, 0));

        List<ActivityFeedItemResponse> result =
                activityFeedMapper.toActivityFeedItemResponse(List.of(), List.of(doc), nutri);

        assertThat(result.get(0).getText()).isEqualTo("dieta caricata per Luca Bianchi");
    }
}
