package com.project.kore.facade.impl;

import com.project.kore.dto.request.LoginRequest;
import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.response.AuthResult;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.PaymentFrequency;
import com.project.kore.enums.Role;
import com.project.kore.exception.booking.ProfessionalSoldOutException;
import com.project.kore.exception.common.ResourceAlreadyExistsException;
import com.project.kore.facade.SubscriptionFacade;
import com.project.kore.mapper.UserMapper;
import com.project.kore.model.Plan;
import com.project.kore.model.User;
import com.project.kore.security.JwtUtil;
import com.project.kore.service.EmailService;
import com.project.kore.service.PlanService;
import com.project.kore.service.UserService;
import com.project.kore.util.BusinessConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFacadeImpl unit tests")
class AuthFacadeImplTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private UserService userService;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;
    @Mock private PlanService planService;
    @Mock private SubscriptionFacade subscriptionFacade;

    @InjectMocks
    private AuthFacadeImpl authFacade;

    private User client;
    private User ptUser;
    private User nutriUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setEmail("mario@test.com");
        client.setFirstName("Mario");
        client.setLastName("Rossi");
        client.setRole(Role.CLIENT);
        client.setPassword("encoded_pass");

        ptUser = new User();
        ptUser.setId(10L);
        ptUser.setFirstName("PT");
        ptUser.setLastName("Uno");
        ptUser.setRole(Role.PERSONAL_TRAINER);

        nutriUser = new User();
        nutriUser.setId(20L);
        nutriUser.setFirstName("Nutri");
        nutriUser.setLastName("Uno");
        nutriUser.setRole(Role.NUTRITIONIST);

        userResponse = UserResponse.builder()
                .id(1L)
                .firstName("Mario")
                .lastName("Rossi")
                .email("mario@test.com")
                .role(Role.CLIENT)
                .build();
    }

    // ─── registerUser ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerUser: email already exists → throws ResourceAlreadyExistsException")
    void registerUser_emailAlreadyExists_throwsResourceAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                null, null, null, null, null);

        when(userService.existsByEmail("mario@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authFacade.registerUser(request))
                .isInstanceOf(ResourceAlreadyExistsException.class);

        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: happy path without PT, nutritionist, or plan → saves user and sends welcome email")
    void registerUser_noProfessionalNoPlan_savesUserAndSendsEmail() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                null, null, null, null, null);

        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(client);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.save(client)).thenReturn(client);
        when(userMapper.toUserResponse(client)).thenReturn(userResponse);

        UserResponse result = authFacade.registerUser(request);

        assertThat(result).isEqualTo(userResponse);
        verify(userService).save(client);
        verify(emailService).sendWelcomeEmail(eq("mario@test.com"), eq("Mario"));
        verify(subscriptionFacade, never()).activateSubscription(any(), any(), any());
    }

    @Test
    @DisplayName("registerUser: with PT assigned → sets assignedPT on user")
    void registerUser_withPT_setsAssignedPT() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                10L, null, null, null, null);

        User newUser = new User();
        newUser.setEmail("mario@test.com");
        newUser.setRole(Role.CLIENT);

        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(newUser);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.getUserById(10L)).thenReturn(ptUser);
        when(userService.countByAssignedPT(ptUser)).thenReturn(0L);
        when(userService.save(newUser)).thenReturn(newUser);
        when(userMapper.toUserResponse(newUser)).thenReturn(userResponse);

        authFacade.registerUser(request);

        assertThat(newUser.getAssignedPT()).isEqualTo(ptUser);
    }

    @Test
    @DisplayName("registerUser: with nutritionist assigned → sets assignedNutritionist on user")
    void registerUser_withNutri_setsAssignedNutritionist() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                null, 20L, null, null, null);

        User newUser = new User();
        newUser.setEmail("mario@test.com");
        newUser.setRole(Role.CLIENT);

        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(newUser);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.getUserById(20L)).thenReturn(nutriUser);
        when(userService.countByAssignedNutritionist(nutriUser)).thenReturn(5L);
        when(userService.save(newUser)).thenReturn(newUser);
        when(userMapper.toUserResponse(newUser)).thenReturn(userResponse);

        authFacade.registerUser(request);

        assertThat(newUser.getAssignedNutritionist()).isEqualTo(nutriUser);
    }

    @Test
    @DisplayName("registerUser: PT at max capacity → throws ProfessionalSoldOutException")
    void registerUser_ptSoldOut_throwsProfessionalSoldOutException() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                10L, null, null, null, null);

        User newUser = new User();
        newUser.setEmail("mario@test.com");
        newUser.setRole(Role.CLIENT);

        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(newUser);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.getUserById(10L)).thenReturn(ptUser);
        when(userService.countByAssignedPT(ptUser))
                .thenReturn((long) BusinessConstants.MAX_CLIENTS_PER_PROFESSIONAL);

        assertThatThrownBy(() -> authFacade.registerUser(request))
                .isInstanceOf(ProfessionalSoldOutException.class);

        verify(userService, never()).save(any());
    }

    @Test
    @DisplayName("registerUser: nutritionist at max capacity → throws ProfessionalSoldOutException")
    void registerUser_nutriSoldOut_throwsProfessionalSoldOutException() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                null, 20L, null, null, null);

        User newUser = new User();
        newUser.setEmail("mario@test.com");
        newUser.setRole(Role.CLIENT);

        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(newUser);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.getUserById(20L)).thenReturn(nutriUser);
        when(userService.countByAssignedNutritionist(nutriUser))
                .thenReturn((long) BusinessConstants.MAX_CLIENTS_PER_PROFESSIONAL);

        assertThatThrownBy(() -> authFacade.registerUser(request))
                .isInstanceOf(ProfessionalSoldOutException.class);
    }

    @Test
    @DisplayName("registerUser: selected PT ID points to wrong role → throws IllegalArgumentException")
    void registerUser_ptIdWrongRole_throwsIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                20L, null, null, null, null);

        User newUser = new User();
        newUser.setEmail("mario@test.com");
        newUser.setRole(Role.CLIENT);

        // nutriUser has role NUTRITIONIST, not PERSONAL_TRAINER
        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(newUser);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.getUserById(20L)).thenReturn(nutriUser);

        assertThatThrownBy(() -> authFacade.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("registerUser: plan and payment frequency provided → activates subscription")
    void registerUser_withPlanAndFrequency_activatesSubscription() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                null, null, null, 5L, PaymentFrequency.RATE_MENSILI);

        Plan plan = new Plan();
        plan.setId(5L);

        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(client);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.save(client)).thenReturn(client);
        when(planService.getPlanById(5L)).thenReturn(plan);
        when(userMapper.toUserResponse(client)).thenReturn(userResponse);

        authFacade.registerUser(request);

        verify(subscriptionFacade).activateSubscription(client, plan, PaymentFrequency.RATE_MENSILI);
    }

    @Test
    @DisplayName("registerUser: only plan id without frequency → does NOT activate subscription")
    void registerUser_planWithoutFrequency_doesNotActivateSubscription() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                null, null, null, 5L, null);

        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(client);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.save(client)).thenReturn(client);
        when(userMapper.toUserResponse(client)).thenReturn(userResponse);

        authFacade.registerUser(request);

        verify(subscriptionFacade, never()).activateSubscription(any(), any(), any());
    }

    @Test
    @DisplayName("registerUser: only frequency without plan id → does NOT activate subscription")
    void registerUser_frequencyWithoutPlan_doesNotActivateSubscription() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                null, null, null, null, PaymentFrequency.RATE_MENSILI);

        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(client);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.save(client)).thenReturn(client);
        when(userMapper.toUserResponse(client)).thenReturn(userResponse);

        authFacade.registerUser(request);

        verify(subscriptionFacade, never()).activateSubscription(any(), any(), any());
    }

    @Test
    @DisplayName("registerUser: welcome email failure is swallowed → user is still returned")
    void registerUser_emailFailure_doesNotPropagateException() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "pass123",
                null, null, null, null, null);

        when(userService.existsByEmail("mario@test.com")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(client);
        when(userService.encodePassword("pass123")).thenReturn("encoded_pass");
        when(userService.save(client)).thenReturn(client);
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendWelcomeEmail(anyString(), anyString());
        when(userMapper.toUserResponse(client)).thenReturn(userResponse);

        UserResponse result = authFacade.registerUser(request);

        assertThat(result).isEqualTo(userResponse);
    }

    // ─── login ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: valid credentials → returns AuthResult with token and user")
    void login_validCredentials_returnsAuthResult() {
        LoginRequest request = new LoginRequest("mario@test.com", "pass123");

        when(userService.getUserByEmail("mario@test.com")).thenReturn(client);
        when(passwordEncoder.matches("pass123", "encoded_pass")).thenReturn(true);
        when(jwtUtil.generateToken(client)).thenReturn("jwt-token");

        AuthResult result = authFacade.login(request);

        assertThat(result.getToken()).isEqualTo("jwt-token");
        assertThat(result.getUser()).isEqualTo(client);
    }

    @Test
    @DisplayName("login: wrong password → throws BadCredentialsException")
    void login_wrongPassword_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("mario@test.com", "wrong");

        when(userService.getUserByEmail("mario@test.com")).thenReturn(client);
        when(passwordEncoder.matches("wrong", "encoded_pass")).thenReturn(false);

        assertThatThrownBy(() -> authFacade.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtUtil, never()).generateToken(any());
    }

    // ─── forgotPassword ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("forgotPassword: existing user → generates reset token and sends email")
    void forgotPassword_existingUser_sendsResetEmail() {
        when(userService.getUserByEmail("mario@test.com")).thenReturn(client);
        when(jwtUtil.generatePasswordResetToken("mario@test.com")).thenReturn("reset-token");

        authFacade.forgotPassword("mario@test.com");

        verify(jwtUtil).generatePasswordResetToken("mario@test.com");
        verify(emailService).sendPasswordResetEmail(
                eq("mario@test.com"), eq("Mario"), eq("reset-token"));
    }

    @Test
    @DisplayName("forgotPassword: email send failure is swallowed → no exception propagated")
    void forgotPassword_emailFailure_doesNotPropagateException() {
        when(userService.getUserByEmail("mario@test.com")).thenReturn(client);
        when(jwtUtil.generatePasswordResetToken("mario@test.com")).thenReturn("reset-token");
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        // Must not throw
        authFacade.forgotPassword("mario@test.com");

        verify(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    // ─── resetPassword ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("resetPassword: valid token → updates password and sends confirmation email")
    void resetPassword_validToken_updatesPasswordAndSendsConfirmation() {
        when(jwtUtil.validatePasswordResetToken("reset-token")).thenReturn("mario@test.com");
        when(userService.getUserByEmail("mario@test.com")).thenReturn(client);
        when(passwordEncoder.encode("newPass1")).thenReturn("new_encoded");
        when(userService.save(client)).thenReturn(client);

        authFacade.resetPassword("reset-token", "newPass1");

        assertThat(client.getPassword()).isEqualTo("new_encoded");
        verify(userService).save(client);
        verify(emailService).sendPasswordChangeEmail(eq("mario@test.com"), eq("Mario"));
    }

    @Test
    @DisplayName("resetPassword: confirmation email failure is swallowed → no exception propagated")
    void resetPassword_emailFailure_doesNotPropagateException() {
        when(jwtUtil.validatePasswordResetToken("reset-token")).thenReturn("mario@test.com");
        when(userService.getUserByEmail("mario@test.com")).thenReturn(client);
        when(passwordEncoder.encode("newPass1")).thenReturn("new_encoded");
        when(userService.save(client)).thenReturn(client);
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendPasswordChangeEmail(anyString(), anyString());

        // Must not throw
        authFacade.resetPassword("reset-token", "newPass1");

        verify(userService).save(client);
    }
}
