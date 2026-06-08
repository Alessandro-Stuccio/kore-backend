package com.project.kore.facade.impl;

import com.project.kore.dto.response.ActivityFeedItemResponse;
import com.project.kore.enums.Role;
import com.project.kore.mapper.ActivityFeedMapper;
import com.project.kore.model.Document;
import com.project.kore.model.Slot;
import com.project.kore.model.User;
import com.project.kore.service.DocumentService;
import com.project.kore.service.SlotService;
import com.project.kore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityFeedFacadeImpl unit tests")
class ActivityFeedFacadeImplTest {

    @Mock private UserService userService;
    @Mock private SlotService slotService;
    @Mock private DocumentService documentService;
    @Mock private ActivityFeedMapper activityFeedMapper;

    @InjectMocks
    private ActivityFeedFacadeImpl facade;

    private User clientUser;
    private User ptUser;
    private User nutriUser;
    private ActivityFeedItemResponse feedItem;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setRole(Role.CLIENT);

        ptUser = new User();
        ptUser.setId(2L);
        ptUser.setRole(Role.PERSONAL_TRAINER);

        nutriUser = new User();
        nutriUser.setId(3L);
        nutriUser.setRole(Role.NUTRITIONIST);

        feedItem = ActivityFeedItemResponse.builder().build();
    }

    // ─── CLIENT path ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getActivityFeed CLIENT: calls findRecentByUser and findRecentByOwner")
    void getActivityFeed_client_callsUserBasedServices() {
        List<Slot> slots = List.of(new Slot());
        List<Document> docs = List.of(new Document());
        List<ActivityFeedItemResponse> expected = List.of(feedItem);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(slotService.findRecentByUser(eq(clientUser), any(LocalDateTime.class))).thenReturn(slots);
        when(documentService.findRecentByOwner(eq(clientUser), any(LocalDateTime.class))).thenReturn(docs);
        when(activityFeedMapper.toActivityFeedItemResponse(slots, docs, clientUser)).thenReturn(expected);

        List<ActivityFeedItemResponse> result = facade.getActivityFeed(1L, 7, 10);

        assertThat(result).isEqualTo(expected);
        verify(slotService).findRecentByUser(eq(clientUser), any(LocalDateTime.class));
        verify(documentService).findRecentByOwner(eq(clientUser), any(LocalDateTime.class));
        verify(slotService, never()).findRecentByProfessional(any(), any());
        verify(documentService, never()).findRecentByProfessional(any(), any());
    }

    @Test
    @DisplayName("getActivityFeed CLIENT: since date is within the requested days window")
    void getActivityFeed_client_sinceIsApproximatelyNowMinusDays() {
        LocalDateTime beforeCall = LocalDateTime.now().minusDays(7).minusSeconds(2);
        LocalDateTime afterCall  = LocalDateTime.now().minusDays(7).plusSeconds(2);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(slotService.findRecentByUser(eq(clientUser), any(LocalDateTime.class))).thenReturn(List.of());
        when(documentService.findRecentByOwner(eq(clientUser), any(LocalDateTime.class))).thenReturn(List.of());
        when(activityFeedMapper.toActivityFeedItemResponse(any(), any(), any())).thenReturn(List.of());

        facade.getActivityFeed(1L, 7, 10);

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(slotService).findRecentByUser(eq(clientUser), captor.capture());
        LocalDateTime since = captor.getValue();
        assertThat(since).isAfter(beforeCall).isBefore(afterCall);
    }

    // ─── PERSONAL_TRAINER path ───────────────────────────────────────────────────

    @Test
    @DisplayName("getActivityFeed PERSONAL_TRAINER: calls findRecentByProfessional for slots and docs")
    void getActivityFeed_personalTrainer_callsProfessionalBasedServices() {
        List<Slot> slots = List.of(new Slot());
        List<Document> docs = List.of(new Document());
        List<ActivityFeedItemResponse> expected = List.of(feedItem);

        when(userService.getUserById(2L)).thenReturn(ptUser);
        when(slotService.findRecentByProfessional(eq(ptUser), any(LocalDateTime.class))).thenReturn(slots);
        when(documentService.findRecentByProfessional(eq(ptUser), any(LocalDateTime.class))).thenReturn(docs);
        when(activityFeedMapper.toActivityFeedItemResponse(slots, docs, ptUser)).thenReturn(expected);

        List<ActivityFeedItemResponse> result = facade.getActivityFeed(2L, 30, 20);

        assertThat(result).isEqualTo(expected);
        verify(slotService).findRecentByProfessional(eq(ptUser), any(LocalDateTime.class));
        verify(documentService).findRecentByProfessional(eq(ptUser), any(LocalDateTime.class));
        verify(slotService, never()).findRecentByUser(any(), any());
    }

    // ─── NUTRITIONIST path ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getActivityFeed NUTRITIONIST: calls findRecentByProfessional for slots and docs")
    void getActivityFeed_nutritionist_callsProfessionalBasedServices() {
        when(userService.getUserById(3L)).thenReturn(nutriUser);
        when(slotService.findRecentByProfessional(eq(nutriUser), any(LocalDateTime.class))).thenReturn(List.of());
        when(documentService.findRecentByProfessional(eq(nutriUser), any(LocalDateTime.class))).thenReturn(List.of());
        when(activityFeedMapper.toActivityFeedItemResponse(any(), any(), eq(nutriUser))).thenReturn(List.of());

        facade.getActivityFeed(3L, 14, 5);

        verify(slotService).findRecentByProfessional(eq(nutriUser), any(LocalDateTime.class));
        verify(documentService).findRecentByProfessional(eq(nutriUser), any(LocalDateTime.class));
    }

    // ─── Other roles (no slots/docs fetched) ─────────────────────────────────────

    @Test
    @DisplayName("getActivityFeed MODERATOR: neither slot nor document services are called")
    void getActivityFeed_moderator_noServiceCalls() {
        User modUser = new User();
        modUser.setId(4L);
        modUser.setRole(Role.MODERATOR);

        when(userService.getUserById(4L)).thenReturn(modUser);
        when(activityFeedMapper.toActivityFeedItemResponse(any(), any(), eq(modUser))).thenReturn(List.of());

        List<ActivityFeedItemResponse> result = facade.getActivityFeed(4L, 7, 10);

        assertThat(result).isEmpty();
        verify(slotService, never()).findRecentByUser(any(), any());
        verify(slotService, never()).findRecentByProfessional(any(), any());
        verify(documentService, never()).findRecentByOwner(any(), any());
        verify(documentService, never()).findRecentByProfessional(any(), any());
    }

    @Test
    @DisplayName("getActivityFeed: mapper result is returned as-is")
    void getActivityFeed_returnsMapperResult() {
        ActivityFeedItemResponse item1 = ActivityFeedItemResponse.builder().build();
        ActivityFeedItemResponse item2 = ActivityFeedItemResponse.builder().build();
        List<ActivityFeedItemResponse> mapperOutput = List.of(item1, item2);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(slotService.findRecentByUser(any(), any())).thenReturn(List.of());
        when(documentService.findRecentByOwner(any(), any())).thenReturn(List.of());
        when(activityFeedMapper.toActivityFeedItemResponse(any(), any(), any())).thenReturn(mapperOutput);

        List<ActivityFeedItemResponse> result = facade.getActivityFeed(1L, 7, 10);

        assertThat(result).hasSize(2).containsExactly(item1, item2);
    }
}
