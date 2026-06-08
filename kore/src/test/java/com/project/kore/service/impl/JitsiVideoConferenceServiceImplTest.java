package com.project.kore.service.impl;

import com.project.kore.model.Slot;
import com.project.kore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JitsiVideoConferenceServiceImplTest {

    @InjectMocks
    private JitsiVideoConferenceServiceImpl jitsiService;

    private User client;
    private User professional;
    private Slot slot;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jitsiService, "jitsiBaseUrl", "https://meet.jit.si/Kore_Consulto_");

        client = new User();
        client.setId(1L);
        client.setEmail("client@test.com");

        professional = new User();
        professional.setId(2L);
        professional.setEmail("pt@test.com");

        slot = new Slot();
        slot.setId(10L);
        slot.setProfessional(professional);
        slot.setStartTime(LocalDateTime.now().plusDays(1));
        slot.setEndTime(LocalDateTime.now().plusDays(1).plusMinutes(30));
    }

    // ---- generateMeetingLink ----

    @Test
    @DisplayName("generateMeetingLink: link starts with configured jitsi base URL")
    void generateMeetingLink_startsWithBaseUrl() {
        String link = jitsiService.generateMeetingLink(client, professional, slot);

        assertThat(link).startsWith("https://meet.jit.si/Kore_Consulto_");
    }

    @Test
    @DisplayName("generateMeetingLink: link contains client id and professional id separated by underscores")
    void generateMeetingLink_containsClientAndProfessionalIds() {
        String link = jitsiService.generateMeetingLink(client, professional, slot);

        assertThat(link).contains("1_2_");
    }

    @Test
    @DisplayName("generateMeetingLink: link has exactly 8-character UUID suffix appended after ids")
    void generateMeetingLink_hasEightCharUuidSuffix() {
        String link = jitsiService.generateMeetingLink(client, professional, slot);

        // format: <baseUrl><clientId>_<professionalId>_<8chars>
        String prefix = "https://meet.jit.si/Kore_Consulto_1_2_";
        assertThat(link).startsWith(prefix);
        String suffix = link.substring(prefix.length());
        assertThat(suffix).hasSize(8);
    }

    @Test
    @DisplayName("generateMeetingLink: two consecutive calls produce different links due to UUID randomness")
    void generateMeetingLink_consecutiveCalls_produceDifferentLinks() {
        String link1 = jitsiService.generateMeetingLink(client, professional, slot);
        String link2 = jitsiService.generateMeetingLink(client, professional, slot);

        // Statistically safe: probability of collision is 1 / 36^8 ≈ 2.8e-12
        assertThat(link1).isNotEqualTo(link2);
    }

    @Test
    @DisplayName("generateMeetingLink: uses custom base URL when injected via ReflectionTestUtils")
    void generateMeetingLink_customBaseUrl_usesInjectedValue() {
        ReflectionTestUtils.setField(jitsiService, "jitsiBaseUrl", "https://custom.meet/Room_");

        String link = jitsiService.generateMeetingLink(client, professional, slot);

        assertThat(link).startsWith("https://custom.meet/Room_1_2_");
    }

    @Test
    @DisplayName("generateMeetingLink: link contains professional id when different from client id")
    void generateMeetingLink_differentIds_bothPresentInLink() {
        User anotherPro = new User();
        anotherPro.setId(99L);

        String link = jitsiService.generateMeetingLink(client, anotherPro, slot);

        assertThat(link).contains("1_99_");
    }
}
