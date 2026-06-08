package com.project.kore.scheduler;

import com.project.kore.enums.Role;
import com.project.kore.facade.ProfessionalFacade;
import com.project.kore.model.User;
import com.project.kore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SlotGenerationScheduler unit tests")
class SlotGenerationSchedulerTest {

    @Mock private ProfessionalFacade professionalFacade;
    @Mock private UserService userService;

    @InjectMocks
    private SlotGenerationScheduler scheduler;

    private User pt1;
    private User nutri1;

    @BeforeEach
    void setUp() {
        pt1 = new User();
        pt1.setId(1L);
        pt1.setFirstName("Marco");
        pt1.setLastName("PT");
        pt1.setRole(Role.PERSONAL_TRAINER);

        nutri1 = new User();
        nutri1.setId(2L);
        nutri1.setFirstName("Sara");
        nutri1.setLastName("Nutri");
        nutri1.setRole(Role.NUTRITIONIST);
    }

    @Test
    @DisplayName("generateWeeklySlotsForAllProfessionals: no professionals → nothing happens")
    void generateWeeklySlots_noProfessionals_nothingHappens() {
        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of());
        when(userService.findByRole(Role.NUTRITIONIST)).thenReturn(List.of());

        scheduler.generateWeeklySlotsForAllProfessionals();

        verifyNoInteractions(professionalFacade);
    }

    @Test
    @DisplayName("generateWeeklySlotsForAllProfessionals: two professionals → generateSlotsFromSchedule called for each")
    void generateWeeklySlots_twoProfessionals_callsForEach() {
        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of(pt1));
        when(userService.findByRole(Role.NUTRITIONIST)).thenReturn(List.of(nutri1));

        scheduler.generateWeeklySlotsForAllProfessionals();

        verify(professionalFacade).generateSlotsFromSchedule(eq(pt1), any(LocalDate.class), any(LocalDate.class));
        verify(professionalFacade).generateSlotsFromSchedule(eq(nutri1), any(LocalDate.class), any(LocalDate.class));
        verifyNoMoreInteractions(professionalFacade);
    }

    @Test
    @DisplayName("generateWeeklySlotsForAllProfessionals: start = today+7, end = start+6")
    void generateWeeklySlots_correctDateRange() {
        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of(pt1));
        when(userService.findByRole(Role.NUTRITIONIST)).thenReturn(List.of());

        LocalDate before = LocalDate.now();
        scheduler.generateWeeklySlotsForAllProfessionals();
        LocalDate after = LocalDate.now();

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(professionalFacade).generateSlotsFromSchedule(eq(pt1), startCaptor.capture(), endCaptor.capture());

        LocalDate capturedStart = startCaptor.getValue();
        LocalDate capturedEnd = endCaptor.getValue();

        assertThat(capturedStart).isBetween(before.plusDays(7), after.plusDays(7));
        assertThat(capturedEnd).isEqualTo(capturedStart.plusDays(6));
    }

    @Test
    @DisplayName("generateWeeklySlotsForAllProfessionals: exception for one professional → other still processed")
    void generateWeeklySlots_exceptionForOne_continuesWithOther() {
        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of(pt1));
        when(userService.findByRole(Role.NUTRITIONIST)).thenReturn(List.of(nutri1));

        doThrow(new RuntimeException("schedule not found"))
                .when(professionalFacade).generateSlotsFromSchedule(eq(pt1), any(LocalDate.class), any(LocalDate.class));

        scheduler.generateWeeklySlotsForAllProfessionals();

        // nutri1 should still be processed despite pt1 throwing
        verify(professionalFacade).generateSlotsFromSchedule(eq(nutri1), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("generateWeeklySlotsForAllProfessionals: only PT professionals → only PT processed")
    void generateWeeklySlots_onlyPTs_onlyPTsProcessed() {
        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of(pt1));
        when(userService.findByRole(Role.NUTRITIONIST)).thenReturn(List.of());

        scheduler.generateWeeklySlotsForAllProfessionals();

        verify(professionalFacade, times(1)).generateSlotsFromSchedule(any(), any(), any());
        verify(professionalFacade).generateSlotsFromSchedule(eq(pt1), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("generateWeeklySlotsForAllProfessionals: only NUTRITIONIST professionals → only nutritionists processed")
    void generateWeeklySlots_onlyNutri_onlyNutriProcessed() {
        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of());
        when(userService.findByRole(Role.NUTRITIONIST)).thenReturn(List.of(nutri1));

        scheduler.generateWeeklySlotsForAllProfessionals();

        verify(professionalFacade, times(1)).generateSlotsFromSchedule(any(), any(), any());
        verify(professionalFacade).generateSlotsFromSchedule(eq(nutri1), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("generateWeeklySlotsForAllProfessionals: both PT and NUTRITIONIST exceptions → no crash")
    void generateWeeklySlots_allThrow_doesNotCrash() {
        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of(pt1));
        when(userService.findByRole(Role.NUTRITIONIST)).thenReturn(List.of(nutri1));

        doThrow(new RuntimeException("error"))
                .when(professionalFacade).generateSlotsFromSchedule(any(), any(), any());

        // Should not throw
        scheduler.generateWeeklySlotsForAllProfessionals();

        verify(professionalFacade, times(2)).generateSlotsFromSchedule(any(), any(), any());
    }
}
