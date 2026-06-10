package com.project.kore.facade.impl;

import com.project.kore.dto.request.PlanRequest;
import com.project.kore.dto.request.ProfileUpdateRequest;
import com.project.kore.dto.response.*;
import com.project.kore.dto.response.ProfessionalSummaryResponse;
import com.project.kore.enums.PaymentFrequency;
import com.project.kore.enums.Role;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.facade.SubscriptionFacade;
import com.project.kore.mapper.BookingMapper;
import com.project.kore.mapper.SubscriptionMapper;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.Plan;
import com.project.kore.model.Slot;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFacadeImpl unit tests")
class UserFacadeImplTest {

    @Mock private UserService userService;
    @Mock private PlanService planService;
    @Mock private SlotService slotService;
    @Mock private ReviewService reviewService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private DocumentService documentService;
    @Mock private UserMapper userMapper;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private BookingMapper bookingMapper;
    @Mock private EmailService emailService;
    @Mock private SubscriptionFacade subscriptionFacade;

    @InjectMocks
    private UserFacadeImpl userFacade;

    private User clientUser;
    private User ptUser;
    private User nutriUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setFirstName("Luca");
        clientUser.setLastName("Bianchi");
        clientUser.setEmail("luca@test.com");
        clientUser.setRole(Role.CLIENT);

        ptUser = new User();
        ptUser.setId(2L);
        ptUser.setFirstName("Marco");
        ptUser.setLastName("PT");
        ptUser.setEmail("pt@test.com");
        ptUser.setRole(Role.PERSONAL_TRAINER);

        nutriUser = new User();
        nutriUser.setId(3L);
        nutriUser.setFirstName("Sara");
        nutriUser.setLastName("Nutri");
        nutriUser.setEmail("nutri@test.com");
        nutriUser.setRole(Role.NUTRITIONIST);

        adminUser = new User();
        adminUser.setId(10L);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.ADMIN);
    }

    // ─── updateProfile ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile: all fields non-null → sets all fields and saves")
    void updateProfile_allFields_setsAndSaves() {
        ProfileUpdateRequest request = new ProfileUpdateRequest("Mario", "Rossi", "newpass", "pic.jpg");
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.encodePassword("newpass")).thenReturn("encoded");

        userFacade.updateProfile(1L, request);

        assertThat(clientUser.getFirstName()).isEqualTo("Mario");
        assertThat(clientUser.getLastName()).isEqualTo("Rossi");
        assertThat(clientUser.getProfilePicture()).isEqualTo("pic.jpg");
        assertThat(clientUser.getPassword()).isEqualTo("encoded");
        verify(userService).save(clientUser);
    }

    @Test
    @DisplayName("updateProfile: null firstName → firstName not changed")
    void updateProfile_nullFirstName_notChanged() {
        clientUser.setFirstName("Original");
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, null);
        when(userService.getUserById(1L)).thenReturn(clientUser);

        userFacade.updateProfile(1L, request);

        assertThat(clientUser.getFirstName()).isEqualTo("Original");
        verify(userService).save(clientUser);
    }

    @Test
    @DisplayName("updateProfile: blank firstName → firstName not changed")
    void updateProfile_blankFirstName_notChanged() {
        clientUser.setFirstName("Original");
        ProfileUpdateRequest request = new ProfileUpdateRequest("   ", null, null, null);
        when(userService.getUserById(1L)).thenReturn(clientUser);

        userFacade.updateProfile(1L, request);

        assertThat(clientUser.getFirstName()).isEqualTo("Original");
        verify(userService).save(clientUser);
    }

    @Test
    @DisplayName("updateProfile: null lastName → lastName not changed")
    void updateProfile_nullLastName_notChanged() {
        clientUser.setLastName("OriginalLast");
        ProfileUpdateRequest request = new ProfileUpdateRequest("Mario", null, null, null);
        when(userService.getUserById(1L)).thenReturn(clientUser);

        userFacade.updateProfile(1L, request);

        assertThat(clientUser.getLastName()).isEqualTo("OriginalLast");
        verify(userService).save(clientUser);
    }

    @Test
    @DisplayName("updateProfile: blank profilePicture → picture not changed")
    void updateProfile_blankProfilePicture_notChanged() {
        clientUser.setProfilePicture("original.jpg");
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, null, "  ");
        when(userService.getUserById(1L)).thenReturn(clientUser);

        userFacade.updateProfile(1L, request);

        assertThat(clientUser.getProfilePicture()).isEqualTo("original.jpg");
        verify(userService).save(clientUser);
    }

    @Test
    @DisplayName("updateProfile: password provided → encodes and sends email")
    void updateProfile_withPassword_encodesAndSendsEmail() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, "secret123", null);
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.encodePassword("secret123")).thenReturn("bcrypt_hash");

        userFacade.updateProfile(1L, request);

        assertThat(clientUser.getPassword()).isEqualTo("bcrypt_hash");
        verify(emailService).sendPasswordChangeEmail(clientUser.getEmail(), clientUser.getFirstName());
        verify(userService).save(clientUser);
    }

    @Test
    @DisplayName("updateProfile: password provided but SMTP throws → exception swallowed, user still saved")
    void updateProfile_smtpException_swallowedAndSaves() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest(null, null, "secret123", null);
        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.encodePassword("secret123")).thenReturn("bcrypt_hash");
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendPasswordChangeEmail(anyString(), anyString());

        userFacade.updateProfile(1L, request);

        verify(userService).save(clientUser);
    }

    @Test
    @DisplayName("updateProfile: null password → no encoding, no email sent")
    void updateProfile_nullPassword_noEmailSent() throws Exception {
        ProfileUpdateRequest request = new ProfileUpdateRequest("Mario", null, null, null);
        when(userService.getUserById(1L)).thenReturn(clientUser);

        userFacade.updateProfile(1L, request);

        verify(emailService, never()).sendPasswordChangeEmail(anyString(), anyString());
        verify(userService, never()).encodePassword(anyString());
        verify(userService).save(clientUser);
    }

    // ─── getClientDashboard ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getClientDashboard: non-CLIENT role → throws AccessDeniedException")
    void getClientDashboard_nonClient_throwsUnauthorized() {
        ptUser.setRole(Role.PERSONAL_TRAINER);
        when(userService.getUserById(2L)).thenReturn(ptUser);

        assertThatThrownBy(() -> userFacade.getClientDashboard(2L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("getClientDashboard: CLIENT with both PT and nutritionist assigned → both in followingProfessionals")
    void getClientDashboard_clientWithBothProfessionals_buildsDashboard() {
        clientUser.setAssignedPT(ptUser);
        clientUser.setAssignedNutritionist(nutriUser);

        UserResponse userResponse = UserResponse.builder().id(1L).build();
        ProfessionalSummaryResponse ptSummary = ProfessionalSummaryResponse.builder().id(2L).build();
        ProfessionalSummaryResponse nutriSummary = ProfessionalSummaryResponse.builder().id(3L).build();
        SubscriptionResponse subResponse = SubscriptionResponse.builder().id(99L).active(true).build();
        Subscription subscription = new Subscription();

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userMapper.toUserResponse(clientUser)).thenReturn(userResponse);
        when(userMapper.toProfessionalSummary(ptUser)).thenReturn(ptSummary);
        when(userMapper.toProfessionalSummary(nutriUser)).thenReturn(nutriSummary);
        when(subscriptionService.getSubscriptionStatus(clientUser)).thenReturn(subscription);
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subResponse);
        when(slotService.findFutureByUser(eq(clientUser), any(LocalDateTime.class))).thenReturn(List.of());

        ClientDashboardResponse result = userFacade.getClientDashboard(1L);

        assertThat(result.getProfile()).isEqualTo(userResponse);
        assertThat(result.getFollowingProfessionals()).containsExactlyInAnyOrder(ptSummary, nutriSummary);
        assertThat(result.getSubscription()).isEqualTo(subResponse);
        assertThat(result.getUpcomingBookings()).isEmpty();
    }

    @Test
    @DisplayName("getClientDashboard: CLIENT with no professionals assigned → empty followingProfessionals")
    void getClientDashboard_noProfessionalsAssigned_emptyList() {
        clientUser.setAssignedPT(null);
        clientUser.setAssignedNutritionist(null);

        UserResponse userResponse = UserResponse.builder().id(1L).build();
        Subscription subscription = new Subscription();
        SubscriptionResponse subResponse = SubscriptionResponse.builder().id(99L).build();

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userMapper.toUserResponse(clientUser)).thenReturn(userResponse);
        when(subscriptionService.getSubscriptionStatus(clientUser)).thenReturn(subscription);
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subResponse);
        when(slotService.findFutureByUser(eq(clientUser), any(LocalDateTime.class))).thenReturn(List.of());

        ClientDashboardResponse result = userFacade.getClientDashboard(1L);

        assertThat(result.getFollowingProfessionals()).isEmpty();
    }

    @Test
    @DisplayName("getClientDashboard: subscriptionService throws → subscription is null (exception ignored)")
    void getClientDashboard_subscriptionThrows_subResponseNull() {
        clientUser.setAssignedPT(null);
        clientUser.setAssignedNutritionist(null);

        UserResponse userResponse = UserResponse.builder().id(1L).build();

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userMapper.toUserResponse(clientUser)).thenReturn(userResponse);
        when(subscriptionService.getSubscriptionStatus(clientUser)).thenThrow(new RuntimeException("no sub"));
        when(slotService.findFutureByUser(eq(clientUser), any(LocalDateTime.class))).thenReturn(List.of());

        ClientDashboardResponse result = userFacade.getClientDashboard(1L);

        assertThat(result.getSubscription()).isNull();
    }

    @Test
    @DisplayName("getClientDashboard: future bookings mapped to BookingResponse list")
    void getClientDashboard_futureBookings_mappedCorrectly() {
        clientUser.setAssignedPT(null);
        clientUser.setAssignedNutritionist(null);

        Slot slot = new Slot();
        slot.setId(5L);

        UserResponse userResponse = UserResponse.builder().id(1L).build();
        Subscription subscription = new Subscription();
        SubscriptionResponse subResponse = SubscriptionResponse.builder().id(99L).build();

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userMapper.toUserResponse(clientUser)).thenReturn(userResponse);
        when(subscriptionService.getSubscriptionStatus(clientUser)).thenReturn(subscription);
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subResponse);
        when(slotService.findFutureByUser(eq(clientUser), any(LocalDateTime.class))).thenReturn(List.of(slot));
        when(bookingMapper.toResponse(slot)).thenReturn(mock(com.project.kore.dto.response.BookingResponse.class));

        ClientDashboardResponse result = userFacade.getClientDashboard(1L);

        assertThat(result.getUpcomingBookings()).hasSize(1);
    }

    // ─── findAvailableProfessionals ───────────────────────────────────────────────

    @Test
    @DisplayName("findAvailableProfessionals: returns professionals sorted by avgRating desc")
    void findAvailableProfessionals_sortedByRatingDesc() {
        User pt1 = new User();
        pt1.setId(1L);
        pt1.setFirstName("A");
        pt1.setLastName("B");
        pt1.setRole(Role.PERSONAL_TRAINER);

        User pt2 = new User();
        pt2.setId(2L);
        pt2.setFirstName("C");
        pt2.setLastName("D");
        pt2.setRole(Role.PERSONAL_TRAINER);

        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of(pt1, pt2));
        when(reviewService.getAverageRating(1L)).thenReturn(3.0);
        when(reviewService.getAverageRating(2L)).thenReturn(4.5);
        when(userService.countByAssignedPT(pt1)).thenReturn(5L);
        when(userService.countByAssignedPT(pt2)).thenReturn(10L);

        List<ProfessionalSummaryResponse> result = userFacade.findAvailableProfessionals(Role.PERSONAL_TRAINER);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAverageRating()).isEqualTo(4.5);
        assertThat(result.get(1).getAverageRating()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("findAvailableProfessionals: professional at max clients → isSoldOut=true")
    void findAvailableProfessionals_maxClients_isSoldOut() {
        User pt1 = new User();
        pt1.setId(1L);
        pt1.setFirstName("A");
        pt1.setLastName("B");
        pt1.setRole(Role.PERSONAL_TRAINER);

        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of(pt1));
        when(reviewService.getAverageRating(1L)).thenReturn(4.0);
        when(userService.countByAssignedPT(pt1)).thenReturn(50L); // MAX_CLIENTS_PER_PROFESSIONAL

        List<ProfessionalSummaryResponse> result = userFacade.findAvailableProfessionals(Role.PERSONAL_TRAINER);

        assertThat(result.get(0).isSoldOut()).isTrue();
    }

    @Test
    @DisplayName("findAvailableProfessionals: NUTRITIONIST role → uses countByAssignedNutritionist")
    void findAvailableProfessionals_nutritionist_usesNutriCount() {
        User nutri = new User();
        nutri.setId(3L);
        nutri.setFirstName("S");
        nutri.setLastName("N");
        nutri.setRole(Role.NUTRITIONIST);

        when(userService.findByRole(Role.NUTRITIONIST)).thenReturn(List.of(nutri));
        when(reviewService.getAverageRating(3L)).thenReturn(4.0);
        when(userService.countByAssignedNutritionist(nutri)).thenReturn(20L);

        List<ProfessionalSummaryResponse> result = userFacade.findAvailableProfessionals(Role.NUTRITIONIST);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCurrentActiveClients()).isEqualTo(20);
        verify(userService, never()).countByAssignedPT(any());
    }

    @Test
    @DisplayName("findAvailableProfessionals: empty list → returns empty")
    void findAvailableProfessionals_emptyList_returnsEmpty() {
        when(userService.findByRole(Role.PERSONAL_TRAINER)).thenReturn(List.of());

        List<ProfessionalSummaryResponse> result = userFacade.findAvailableProfessionals(Role.PERSONAL_TRAINER);

        assertThat(result).isEmpty();
    }

    // ─── getClientsForProfessional ────────────────────────────────────────────────

    @Test
    @DisplayName("getClientsForProfessional: PT role → returns clients via findByAssignedPT")
    void getClientsForProfessional_pt_returnsPTClients() {
        User client1 = new User();
        client1.setId(5L);
        client1.setRole(Role.CLIENT);

        ClientBasicInfoResponse resp = ClientBasicInfoResponse.builder().id(5L).build();

        when(userService.getUserById(2L)).thenReturn(ptUser);
        when(userService.findByAssignedPT(ptUser)).thenReturn(List.of(client1));
        when(userMapper.toBasicInfoResponse(client1)).thenReturn(resp);

        List<ClientBasicInfoResponse> result = userFacade.getClientsForProfessional(2L);

        assertThat(result).containsExactly(resp);
        verify(userService, never()).findByAssignedNutritionist(any());
    }

    @Test
    @DisplayName("getClientsForProfessional: NUTRITIONIST role → returns clients via findByAssignedNutritionist")
    void getClientsForProfessional_nutritionist_returnsNutriClients() {
        User client1 = new User();
        client1.setId(6L);
        client1.setRole(Role.CLIENT);

        ClientBasicInfoResponse resp = ClientBasicInfoResponse.builder().id(6L).build();

        when(userService.getUserById(3L)).thenReturn(nutriUser);
        when(userService.findByAssignedNutritionist(nutriUser)).thenReturn(List.of(client1));
        when(userMapper.toBasicInfoResponse(client1)).thenReturn(resp);

        List<ClientBasicInfoResponse> result = userFacade.getClientsForProfessional(3L);

        assertThat(result).containsExactly(resp);
        verify(userService, never()).findByAssignedPT(any());
    }

    @Test
    @DisplayName("getClientsForProfessional: non-professional role → throws IllegalArgumentException")
    void getClientsForProfessional_nonProfessional_throwsIllegalArgument() {
        when(userService.getUserById(1L)).thenReturn(clientUser); // CLIENT role

        assertThatThrownBy(() -> userFacade.getClientsForProfessional(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── getAdmin ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAdmin: admin found → returns mapped response")
    void getAdmin_found_returnsMappedResponse() {
        ClientBasicInfoResponse adminResp = ClientBasicInfoResponse.builder().id(10L).email("admin@test.com").build();

        when(userService.findByRole(Role.ADMIN)).thenReturn(List.of(adminUser));
        when(userMapper.toBasicInfoResponse(adminUser)).thenReturn(adminResp);

        ClientBasicInfoResponse result = userFacade.getAdmin();

        assertThat(result).isEqualTo(adminResp);
    }

    @Test
    @DisplayName("getAdmin: no admin found → throws ResourceNotFoundException")
    void getAdmin_notFound_throwsResourceNotFoundException() {
        when(userService.findByRole(Role.ADMIN)).thenReturn(List.of());

        assertThatThrownBy(() -> userFacade.getAdmin())
                .isInstanceOf(CustomResourceNotFoundException.class);
    }

    // ─── activateSubscription ────────────────────────────────────────────────────

    @Test
    @DisplayName("activateSubscription: non-CLIENT role → throws AccessDeniedException")
    void activateSubscription_nonClient_throwsUnauthorized() {
        PlanRequest request = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
        when(userService.getUserById(2L)).thenReturn(ptUser);

        assertThatThrownBy(() -> userFacade.activateSubscription(request, 2L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("activateSubscription: CLIENT with existing active subscription → throws ResourceAlreadyExistsException")
    void activateSubscription_alreadyActive_throwsResourceAlreadyExists() {
        PlanRequest request = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
        Subscription existingSub = new Subscription();

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(subscriptionService.findActiveByUser(clientUser)).thenReturn(Optional.of(existingSub));

        assertThatThrownBy(() -> userFacade.activateSubscription(request, 1L))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    @DisplayName("activateSubscription: valid CLIENT with no active sub → delegates and returns response")
    void activateSubscription_validClient_activatesAndReturns() {
        PlanRequest request = new PlanRequest(1L, PaymentFrequency.UNICA_SOLUZIONE);
        Plan plan = new Plan();
        plan.setId(1L);
        Subscription newSub = new Subscription();
        SubscriptionResponse subResponse = SubscriptionResponse.builder().id(50L).active(true).build();

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(subscriptionService.findActiveByUser(clientUser)).thenReturn(Optional.empty());
        when(planService.getPlanById(1L)).thenReturn(plan);
        when(subscriptionFacade.activateSubscription(clientUser, plan, PaymentFrequency.UNICA_SOLUZIONE))
                .thenReturn(newSub);
        when(subscriptionMapper.toResponse(newSub)).thenReturn(subResponse);

        SubscriptionResponse result = userFacade.activateSubscription(request, 1L);

        assertThat(result).isEqualTo(subResponse);
        verify(subscriptionFacade).activateSubscription(clientUser, plan, PaymentFrequency.UNICA_SOLUZIONE);
    }

    @Test
    @DisplayName("activateSubscription: RATE_MENSILI payment frequency → forwarded to facade")
    void activateSubscription_rateMensili_forwardedToFacade() {
        PlanRequest request = new PlanRequest(2L, PaymentFrequency.RATE_MENSILI);
        Plan plan = new Plan();
        plan.setId(2L);
        Subscription newSub = new Subscription();
        SubscriptionResponse subResponse = SubscriptionResponse.builder().id(51L).active(true).build();

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(subscriptionService.findActiveByUser(clientUser)).thenReturn(Optional.empty());
        when(planService.getPlanById(2L)).thenReturn(plan);
        when(subscriptionFacade.activateSubscription(clientUser, plan, PaymentFrequency.RATE_MENSILI))
                .thenReturn(newSub);
        when(subscriptionMapper.toResponse(newSub)).thenReturn(subResponse);

        SubscriptionResponse result = userFacade.activateSubscription(request, 1L);

        assertThat(result).isEqualTo(subResponse);
        verify(subscriptionFacade).activateSubscription(clientUser, plan, PaymentFrequency.RATE_MENSILI);
    }

    // ─── getSubscriptionStatus ────────────────────────────────────────────────────

    @Test
    @DisplayName("getSubscriptionStatus: returns mapped subscription status")
    void getSubscriptionStatus_returnsResponse() {
        Subscription sub = new Subscription();
        SubscriptionResponse subResponse = SubscriptionResponse.builder().id(77L).active(true).build();

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(subscriptionService.getSubscriptionStatus(clientUser)).thenReturn(sub);
        when(subscriptionMapper.toResponse(sub)).thenReturn(subResponse);

        SubscriptionResponse result = userFacade.getSubscriptionStatus(1L);

        assertThat(result).isEqualTo(subResponse);
    }
}
