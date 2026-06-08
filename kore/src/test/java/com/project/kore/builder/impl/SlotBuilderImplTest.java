package com.project.kore.builder.impl;

import com.project.kore.model.Slot;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("SlotBuilderImpl unit tests")
class SlotBuilderImplTest {

    private User professional;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        professional = new User();
        professional.setId(10L);
        professional.setFirstName("Marco");
        professional.setLastName("Bianchi");

        startTime = LocalDateTime.of(2026, 6, 1, 9, 0);
        endTime = LocalDateTime.of(2026, 6, 1, 9, 30);
    }

    // ─── happy path ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("build: all required fields set → returns a valid Slot")
    void build_allRequiredFields_returnsSlot() {
        Slot slot = new SlotBuilderImpl()
                .professional(professional)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        assertThat(slot).isNotNull();
        assertThat(slot.getProfessional()).isEqualTo(professional);
        assertThat(slot.getStartTime()).isEqualTo(startTime);
        assertThat(slot.getEndTime()).isEqualTo(endTime);
    }

    @Test
    @DisplayName("build: id setter is reflected in built Slot")
    void build_withId_setsIdOnSlot() {
        Slot slot = new SlotBuilderImpl()
                .id(42L)
                .professional(professional)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        assertThat(slot.getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("build: bookedBy setter is reflected in built Slot")
    void build_withBookedBy_setsBookedByOnSlot() {
        User client = new User();
        client.setId(1L);

        Slot slot = new SlotBuilderImpl()
                .professional(professional)
                .startTime(startTime)
                .endTime(endTime)
                .bookedBy(client)
                .build();

        assertThat(slot.getBookedBy()).isEqualTo(client);
    }

    @Test
    @DisplayName("build: version setter is reflected in built Slot")
    void build_withVersion_setsVersionOnSlot() {
        Slot slot = new SlotBuilderImpl()
                .professional(professional)
                .startTime(startTime)
                .endTime(endTime)
                .version(3)
                .build();

        assertThat(slot.getVersion()).isEqualTo(3);
    }

    @Test
    @DisplayName("build: bookedAt setter is reflected in built Slot")
    void build_withBookedAt_setsBookedAtOnSlot() {
        LocalDateTime bookedAt = LocalDateTime.of(2026, 5, 28, 10, 0);

        Slot slot = new SlotBuilderImpl()
                .professional(professional)
                .startTime(startTime)
                .endTime(endTime)
                .bookedAt(bookedAt)
                .build();

        assertThat(slot.getBookedAt()).isEqualTo(bookedAt);
    }

    @Test
    @DisplayName("build: bookedAt not set → bookedAt is null on built Slot")
    void build_withoutBookedAt_bookedAtIsNull() {
        Slot slot = new SlotBuilderImpl()
                .professional(professional)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        assertThat(slot.getBookedAt()).isNull();
    }

    // ─── validation: null fields ──────────────────────────────────────────────────

    @Test
    @DisplayName("build: null professional → throws NullPointerException")
    void build_nullProfessional_throwsNullPointerException() {
        assertThatThrownBy(() -> new SlotBuilderImpl()
                .professional(null)
                .startTime(startTime)
                .endTime(endTime)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("professional");
    }

    @Test
    @DisplayName("build: professional not set → throws NullPointerException")
    void build_professionalNotSet_throwsNullPointerException() {
        assertThatThrownBy(() -> new SlotBuilderImpl()
                .startTime(startTime)
                .endTime(endTime)
                .build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("build: null startTime → throws NullPointerException")
    void build_nullStartTime_throwsNullPointerException() {
        assertThatThrownBy(() -> new SlotBuilderImpl()
                .professional(professional)
                .startTime(null)
                .endTime(endTime)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("startTime");
    }

    @Test
    @DisplayName("build: null endTime → throws NullPointerException")
    void build_nullEndTime_throwsNullPointerException() {
        assertThatThrownBy(() -> new SlotBuilderImpl()
                .professional(professional)
                .startTime(startTime)
                .endTime(null)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("endTime");
    }

    // ─── validation: temporal ordering ───────────────────────────────────────────

    @Test
    @DisplayName("build: startTime equal to endTime → throws IllegalArgumentException")
    void build_startTimeEqualsEndTime_throwsIllegalArgumentException() {
        LocalDateTime sameTime = LocalDateTime.of(2026, 6, 1, 9, 0);

        assertThatThrownBy(() -> new SlotBuilderImpl()
                .professional(professional)
                .startTime(sameTime)
                .endTime(sameTime)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startTime");
    }

    @Test
    @DisplayName("build: startTime after endTime → throws IllegalArgumentException")
    void build_startTimeAfterEndTime_throwsIllegalArgumentException() {
        LocalDateTime laterStart = LocalDateTime.of(2026, 6, 1, 10, 0);
        LocalDateTime earlierEnd = LocalDateTime.of(2026, 6, 1, 9, 0);

        assertThatThrownBy(() -> new SlotBuilderImpl()
                .professional(professional)
                .startTime(laterStart)
                .endTime(earlierEnd)
                .build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
