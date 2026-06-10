package com.project.kore.facade.impl;

import com.project.kore.dto.request.ModeratorUserUpdateRequest;
import com.project.kore.dto.request.UserCreateRequest;
import com.project.kore.dto.response.SubscriptionResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.Role;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import org.springframework.security.access.AccessDeniedException;
import com.project.kore.facade.SubscriptionFacade;
import com.project.kore.mapper.SubscriptionMapper;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.Subscription;
import com.project.kore.model.User;
import com.project.kore.service.ChatService;
import com.project.kore.service.PlanService;
import com.project.kore.service.SubscriptionService;
import com.project.kore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ModeratorFacadeImpl unit tests")
class ModeratorFacadeImplTest {

    @Mock private ChatService chatService;
    @Mock private UserService userService;
    @Mock private SubscriptionService subscriptionService;
    @Mock private UserMapper userMapper;
    @Mock private SubscriptionMapper subscriptionMapper;
    @Mock private PlanService planService;
    @Mock private SubscriptionFacade subscriptionFacade;

    @InjectMocks
    private ModeratorFacadeImpl facade;

    private User moderator;
    private User adminUser;
    private User clientUser;
    private User ptUser;
    private UserResponse userResponse;
    private SubscriptionResponse subscriptionResponse;

    @BeforeEach
    void setUp() {
        moderator = new User();
        moderator.setId(10L);
        moderator.setRole(Role.MODERATOR);

        adminUser = new User();
        adminUser.setId(11L);
        adminUser.setRole(Role.ADMIN);

        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setEmail("client@test.com");
        clientUser.setFirstName("Mario");
        clientUser.setLastName("Rossi");
        clientUser.setRole(Role.CLIENT);

        ptUser = new User();
        ptUser.setId(2L);
        ptUser.setRole(Role.PERSONAL_TRAINER);

        userResponse = UserResponse.builder().build();
        subscriptionResponse = new SubscriptionResponse();
    }

    // ─── getManageableUsers ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getManageableUsers ADMIN: returns all users via toAdminResponse(list)")
    void getManageableUsers_admin_returnsAllUsers() {
        List<User> allUsers = List.of(clientUser, ptUser);
        List<UserResponse> expected = List.of(userResponse);

        when(userService.findAll()).thenReturn(allUsers);
        when(userMapper.toAdminResponse(allUsers)).thenReturn(expected);

        List<UserResponse> result = facade.getManageableUsers(adminUser);

        assertThat(result).isEqualTo(expected);
        verify(userMapper).toAdminResponse(allUsers);
    }

    @Test
    @DisplayName("getManageableUsers MODERATOR: filters only manageable roles and maps individually")
    void getManageableUsers_moderator_filtersRoles() {
        User insuranceUser = new User();
        insuranceUser.setRole(Role.INSURANCE_MANAGER);

        List<User> allUsers = List.of(clientUser, ptUser, insuranceUser);

        when(userService.findAll()).thenReturn(allUsers);
        when(userMapper.toAdminResponse(any(User.class))).thenReturn(userResponse);

        List<UserResponse> result = facade.getManageableUsers(moderator);

        // MODERATOR manages CLIENT, PERSONAL_TRAINER, NUTRITIONIST — not INSURANCE_MANAGER
        assertThat(result).hasSize(2);
        verify(userMapper, times(2)).toAdminResponse(any(User.class));
    }

    // ─── getChatContacts ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getChatContacts: filters ADMIN, INSURANCE_MANAGER, MODERATOR roles only")
    void getChatContacts_filtersExpectedRoles() {
        User admin = new User(); admin.setRole(Role.ADMIN);
        User insurance = new User(); insurance.setRole(Role.INSURANCE_MANAGER);
        User mod = new User(); mod.setRole(Role.MODERATOR);
        User client = new User(); client.setRole(Role.CLIENT);

        when(userService.findAll()).thenReturn(List.of(admin, insurance, mod, client));
        when(userMapper.toAdminResponse(any(User.class))).thenReturn(userResponse);

        List<UserResponse> result = facade.getChatContacts();

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("getChatContacts: returns empty list when no qualifying users exist")
    void getChatContacts_noQualifyingUsers_returnsEmpty() {
        when(userService.findAll()).thenReturn(List.of(clientUser, ptUser));

        List<UserResponse> result = facade.getChatContacts();

        assertThat(result).isEmpty();
    }

    // ─── getAllSubscriptions ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllSubscriptions: maps all subscriptions from service")
    void getAllSubscriptions_returnsMappedList() {
        Subscription sub = new Subscription();
        when(subscriptionService.getAllSubscriptions()).thenReturn(List.of(sub));
        when(subscriptionMapper.toResponse(sub)).thenReturn(subscriptionResponse);

        List<SubscriptionResponse> result = facade.getAllSubscriptions();

        assertThat(result).hasSize(1).containsExactly(subscriptionResponse);
    }

    // ─── createUser ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createUser MODERATOR: creates CLIENT user successfully")
    void createUser_moderatorCreatesClient_success() {
        UserCreateRequest request = new UserCreateRequest(
                "new@test.com", "Luca", "Bianchi", "password123", "CLIENT",
                null, null, null, null);

        when(userService.existsByEmail("new@test.com")).thenReturn(false);
        when(userService.encodePassword("password123")).thenReturn("$2a$10$hashed.password.value.ok");
        when(userService.save(any(User.class))).thenReturn(clientUser);
        when(userMapper.toAdminResponse(clientUser)).thenReturn(userResponse);

        UserResponse result = facade.createUser(request, moderator);

        assertThat(result).isEqualTo(userResponse);
        verify(userService).save(any(User.class));
    }

    @Test
    @DisplayName("createUser MODERATOR: throws AccessDeniedException when role is ADMIN")
    void createUser_moderatorCreatesAdmin_throwsUnauthorized() {
        UserCreateRequest request = new UserCreateRequest(
                "admin2@test.com", "Admin", "New", "password123", "ADMIN",
                null, null, null, null);

        assertThatThrownBy(() -> facade.createUser(request, moderator))
                .isInstanceOf(AccessDeniedException.class);

        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("createUser: throws ResourceAlreadyExistsException when email is already taken")
    void createUser_duplicateEmail_throwsAlreadyExists() {
        UserCreateRequest request = new UserCreateRequest(
                "existing@test.com", "Mario", "Rossi", "password123", "CLIENT",
                null, null, null, null);

        when(userService.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> facade.createUser(request, moderator))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    @DisplayName("createUser: throws IllegalArgumentException when required fields are null")
    void createUser_missingRequiredFields_throwsIllegalArgument() {
        UserCreateRequest request = new UserCreateRequest(
                null, "Mario", "Rossi", "password123", "CLIENT",
                null, null, null, null);

        assertThatThrownBy(() -> facade.createUser(request, moderator))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Campi obbligatori");
    }

    @Test
    @DisplayName("createUser CLIENT with assignedPT: links PT user when role is PERSONAL_TRAINER")
    void createUser_withAssignedPT_linksPT() {
        UserCreateRequest request = new UserCreateRequest(
                "new@test.com", "Luca", "Bianchi", "password123", "CLIENT",
                2L, null, null, null);

        when(userService.existsByEmail("new@test.com")).thenReturn(false);
        when(userService.encodePassword("password123")).thenReturn("$2a$10$hashed.password.value.ok");
        when(userService.getUserById(2L)).thenReturn(ptUser);
        when(userService.save(any(User.class))).thenReturn(clientUser);
        when(userMapper.toAdminResponse(clientUser)).thenReturn(userResponse);

        facade.createUser(request, moderator);

        verify(userService).getUserById(2L);
    }

    @Test
    @DisplayName("createUser CLIENT with assignedPT: throws AccessDeniedException when assigned user is not PT")
    void createUser_withAssignedPT_notPT_throwsUnauthorized() {
        UserCreateRequest request = new UserCreateRequest(
                "new@test.com", "Luca", "Bianchi", "password123", "CLIENT",
                1L, null, null, null);

        User notPT = new User();
        notPT.setId(1L);
        notPT.setRole(Role.CLIENT);

        when(userService.existsByEmail("new@test.com")).thenReturn(false);
        when(userService.encodePassword("password123")).thenReturn("$2a$10$hashed.password.value.ok");
        when(userService.getUserById(1L)).thenReturn(notPT);

        assertThatThrownBy(() -> facade.createUser(request, moderator))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("PERSONAL_TRAINER");
    }

    // ─── updateUser ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateUser MODERATOR: updates CLIENT user email and name")
    void updateUser_moderatorUpdatesClient_success() {
        ModeratorUserUpdateRequest request = new ModeratorUserUpdateRequest(
                "updated@test.com", "Luigi", null, null);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.existsUserByEmailExcluding("updated@test.com", 1L)).thenReturn(false);
        when(userService.save(clientUser)).thenReturn(clientUser);
        when(userMapper.toAdminResponse(clientUser)).thenReturn(userResponse);

        UserResponse result = facade.updateUser(1L, request, moderator);

        assertThat(result).isEqualTo(userResponse);
        assertThat(clientUser.getEmail()).isEqualTo("updated@test.com");
        assertThat(clientUser.getFirstName()).isEqualTo("Luigi");
    }

    @Test
    @DisplayName("updateUser: throws AccessDeniedException when target role is not manageable")
    void updateUser_targetRoleNotManageable_throwsUnauthorized() {
        User insuranceTarget = new User();
        insuranceTarget.setId(5L);
        insuranceTarget.setRole(Role.INSURANCE_MANAGER);

        ModeratorUserUpdateRequest request = new ModeratorUserUpdateRequest(null, "New", null, null);

        when(userService.getUserById(5L)).thenReturn(insuranceTarget);

        assertThatThrownBy(() -> facade.updateUser(5L, request, moderator))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("updateUser: throws ResourceAlreadyExistsException when new email is already in use")
    void updateUser_emailAlreadyInUse_throwsAlreadyExists() {
        ModeratorUserUpdateRequest request = new ModeratorUserUpdateRequest(
                "taken@test.com", null, null, null);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.existsUserByEmailExcluding("taken@test.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> facade.updateUser(1L, request, moderator))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    @DisplayName("updateUser: does not update email when new email equals current email")
    void updateUser_sameEmail_doesNotCallExistsCheck() {
        ModeratorUserUpdateRequest request = new ModeratorUserUpdateRequest(
                "client@test.com", null, null, null);

        when(userService.getUserById(1L)).thenReturn(clientUser);
        when(userService.save(clientUser)).thenReturn(clientUser);
        when(userMapper.toAdminResponse(clientUser)).thenReturn(userResponse);

        facade.updateUser(1L, request, moderator);

        verify(userService, never()).existsUserByEmailExcluding(any(), anyLong());
    }

    // ─── deleteUser ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteUser MODERATOR: deletes CLIENT user successfully")
    void deleteUser_moderatorDeletesClient_success() {
        when(userService.getUserById(1L)).thenReturn(clientUser);

        facade.deleteUser(1L, moderator);

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("deleteUser MODERATOR: throws AccessDeniedException for unmanageable role")
    void deleteUser_unmanageableRole_throwsUnauthorized() {
        User adminTarget = new User();
        adminTarget.setId(99L);
        adminTarget.setRole(Role.ADMIN);

        when(userService.getUserById(99L)).thenReturn(adminTarget);

        assertThatThrownBy(() -> facade.deleteUser(99L, moderator))
                .isInstanceOf(AccessDeniedException.class);

        verify(userService, never()).deleteUser(anyLong());
    }

    // ─── updateSubscriptionCredits ───────────────────────────────────────────────

    @Test
    @DisplayName("updateSubscriptionCredits: updates credits and returns mapped response")
    void updateSubscriptionCredits_validValues_success() {
        Subscription sub = new Subscription();
        when(subscriptionService.updateSubscriptionCredits(1L, 3, 2)).thenReturn(sub);
        when(subscriptionMapper.toResponse(sub)).thenReturn(subscriptionResponse);

        SubscriptionResponse result = facade.updateSubscriptionCredits(1L, 3, 2);

        assertThat(result).isEqualTo(subscriptionResponse);
    }

    @Test
    @DisplayName("updateSubscriptionCredits: throws IllegalArgumentException when PT credits are negative")
    void updateSubscriptionCredits_negativePT_throwsIllegalArgument() {
        assertThatThrownBy(() -> facade.updateSubscriptionCredits(1L, -1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativi");
    }

    @Test
    @DisplayName("updateSubscriptionCredits: throws IllegalArgumentException when nutri credits are negative")
    void updateSubscriptionCredits_negativeNutri_throwsIllegalArgument() {
        assertThatThrownBy(() -> facade.updateSubscriptionCredits(1L, 0, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativi");
    }

    @Test
    @DisplayName("updateSubscriptionCredits: zero is a valid value for both credit types")
    void updateSubscriptionCredits_zeroCredits_doesNotThrow() {
        Subscription sub = new Subscription();
        when(subscriptionService.updateSubscriptionCredits(1L, 0, 0)).thenReturn(sub);
        when(subscriptionMapper.toResponse(sub)).thenReturn(subscriptionResponse);

        SubscriptionResponse result = facade.updateSubscriptionCredits(1L, 0, 0);

        assertThat(result).isNotNull();
    }
}
