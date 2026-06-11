package com.project.kore.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RandomGenerationServiceImpl unit tests")
class RandomGenerationServiceImplTest {

    @Test
    @DisplayName("getTokenKey: genera una stringa della lunghezza configurata")
    void getTokenKey_returnsStringOfConfiguredLength() {
        RandomGenerationServiceImpl service = new RandomGenerationServiceImpl();
        ReflectionTestUtils.setField(service, "length", 32);

        String token = service.getTokenKey();

        assertThat(token).isNotNull().hasSize(32);
    }

    @Test
    @DisplayName("getTokenKey: contiene minuscole, maiuscole e cifre e produce valori diversi")
    void getTokenKey_containsRequiredCharacterClasses() {
        RandomGenerationServiceImpl service = new RandomGenerationServiceImpl();
        ReflectionTestUtils.setField(service, "length", 24);

        String token = service.getTokenKey();
        String other = service.getTokenKey();

        assertThat(token).hasSize(24)
                .matches(".*[a-z].*")
                .matches(".*[A-Z].*")
                .matches(".*[0-9].*");
        // Con 24 caratteri casuali la probabilità di collisione è trascurabile.
        assertThat(token).isNotEqualTo(other);
    }
}
