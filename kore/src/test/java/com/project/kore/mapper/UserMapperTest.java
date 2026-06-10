package com.project.kore.mapper;

import com.project.kore.dto.request.RegisterRequest;
import com.project.kore.dto.response.ClientBasicInfoResponse;
import com.project.kore.dto.response.ProfessionalSummaryResponse;
import com.project.kore.dto.response.UserResponse;
import com.project.kore.enums.Role;
import com.project.kore.model.User;
import com.project.kore.repository.ReviewRepository;
import com.project.kore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper(userRepository, reviewRepository);
    }

    // ---- helpers ----

    private User buildUser(Long id, String firstName, String lastName, String email, Role role) {
        User u = new User();
        u.setId(id);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setRole(role);
        u.setProfilePicture("http://pic.url/" + id);
        return u;
    }

    // ---- toUserResponse ----

    @Test
    @DisplayName("toUserResponse: CLIENT user has no rating or clientsCount enrichment")
    void toUserResponse_clientUser_noEnrichment() {
        User client = buildUser(1L, "Luca", "Bianchi", "luca@test.com", Role.CLIENT);

        UserResponse response = userMapper.toUserResponse(client);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("Luca");
        assertThat(response.getLastName()).isEqualTo("Bianchi");
        assertThat(response.getEmail()).isEqualTo("luca@test.com");
        assertThat(response.getRole()).isEqualTo(Role.CLIENT);
        assertThat(response.getProfilePictureUrl()).isEqualTo("http://pic.url/1");
        assertThat(response.getAverageRating()).isNull();
        assertThat(response.getActiveClientsCount()).isNull();
        verifyNoInteractions(reviewRepository, userRepository);
    }

    @Test
    @DisplayName("toUserResponse: PERSONAL_TRAINER user gets avgRating and clientsCount")
    void toUserResponse_personalTrainer_enriched() {
        User pt = buildUser(2L, "Marco", "Rossi", "pt@test.com", Role.PERSONAL_TRAINER);
        when(reviewRepository.getAverageRating(2L)).thenReturn(4.5);
        when(userRepository.countByAssignedPTAndDeletedFalse(pt)).thenReturn(10L);

        UserResponse response = userMapper.toUserResponse(pt);

        assertThat(response.getAverageRating()).isEqualTo(4.5);
        assertThat(response.getActiveClientsCount()).isEqualTo(10);
        verify(reviewRepository).getAverageRating(2L);
        verify(userRepository).countByAssignedPTAndDeletedFalse(pt);
    }

    @Test
    @DisplayName("toUserResponse: PERSONAL_TRAINER with null avgRating defaults to 0.0")
    void toUserResponse_personalTrainer_nullRatingDefaultsToZero() {
        User pt = buildUser(3L, "Anna", "Verdi", "pt2@test.com", Role.PERSONAL_TRAINER);
        when(reviewRepository.getAverageRating(3L)).thenReturn(null);
        when(userRepository.countByAssignedPTAndDeletedFalse(pt)).thenReturn(0L);

        UserResponse response = userMapper.toUserResponse(pt);

        assertThat(response.getAverageRating()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("toUserResponse: NUTRITIONIST user gets avgRating and clientsCount via nutritionist count")
    void toUserResponse_nutritionist_enrichedWithNutriCount() {
        User nutri = buildUser(4L, "Sara", "Neri", "nutri@test.com", Role.NUTRITIONIST);
        when(reviewRepository.getAverageRating(4L)).thenReturn(3.8);
        when(userRepository.countByAssignedNutritionistAndDeletedFalse(nutri)).thenReturn(7L);

        UserResponse response = userMapper.toUserResponse(nutri);

        assertThat(response.getAverageRating()).isEqualTo(3.8);
        assertThat(response.getActiveClientsCount()).isEqualTo(7);
        verify(userRepository).countByAssignedNutritionistAndDeletedFalse(nutri);
        verify(userRepository, never()).countByAssignedPTAndDeletedFalse(any());
    }

    @Test
    @DisplayName("toUserResponse: CLIENT with assignedPT populates assignedPtName")
    void toUserResponse_clientWithAssignedPT_populatesAssignedPtName() {
        User pt = buildUser(10L, "Carlo", "Ferrari", "carlo@test.com", Role.PERSONAL_TRAINER);
        User client = buildUser(1L, "Luca", "Bianchi", "luca@test.com", Role.CLIENT);
        client.setAssignedPT(pt);

        UserResponse response = userMapper.toUserResponse(client);

        assertThat(response.getAssignedPtName()).isEqualTo("Carlo Ferrari");
    }

    @Test
    @DisplayName("toUserResponse: CLIENT with assignedNutritionist populates assignedNutritionistName")
    void toUserResponse_clientWithAssignedNutri_populatesNutriName() {
        User nutri = buildUser(11L, "Giulia", "Marchi", "giulia@test.com", Role.NUTRITIONIST);
        User client = buildUser(1L, "Luca", "Bianchi", "luca@test.com", Role.CLIENT);
        client.setAssignedNutritionist(nutri);

        UserResponse response = userMapper.toUserResponse(client);

        assertThat(response.getAssignedNutritionistName()).isEqualTo("Giulia Marchi");
    }

    @Test
    @DisplayName("toUserResponse: CLIENT with no assigned professionals has null names")
    void toUserResponse_clientNoAssignedProfessionals_nullNames() {
        User client = buildUser(1L, "Luca", "Bianchi", "luca@test.com", Role.CLIENT);

        UserResponse response = userMapper.toUserResponse(client);

        assertThat(response.getAssignedPtName()).isNull();
        assertThat(response.getAssignedNutritionistName()).isNull();
    }

    // ---- toAdminResponse(User) ----

    @Test
    @DisplayName("toAdminResponse(User): maps basic fields without rating or clientsCount")
    void toAdminResponse_user_mapsBasicFields() {
        User admin = buildUser(5L, "Admin", "User", "admin@test.com", Role.ADMIN);

        UserResponse response = userMapper.toAdminResponse(admin);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getFirstName()).isEqualTo("Admin");
        assertThat(response.getLastName()).isEqualTo("User");
        assertThat(response.getEmail()).isEqualTo("admin@test.com");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        assertThat(response.getAverageRating()).isNull();
        assertThat(response.getActiveClientsCount()).isNull();
        verifyNoInteractions(reviewRepository, userRepository);
    }

    @Test
    @DisplayName("toAdminResponse(User): PT user does NOT query repositories")
    void toAdminResponse_professionalUser_doesNotQueryRepositories() {
        User pt = buildUser(6L, "Marco", "Blu", "pt@test.com", Role.PERSONAL_TRAINER);

        userMapper.toAdminResponse(pt);

        verifyNoInteractions(reviewRepository, userRepository);
    }

    // ---- toAdminResponse(List<User>) ----

    @Test
    @DisplayName("toAdminResponse(List): maps all users in the list")
    void toAdminResponseList_mapsAllUsers() {
        User u1 = buildUser(1L, "A", "B", "a@test.com", Role.CLIENT);
        User u2 = buildUser(2L, "C", "D", "c@test.com", Role.ADMIN);

        List<UserResponse> result = userMapper.toAdminResponse(List.of(u1, u2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toAdminResponse(List): returns empty list when input is null")
    void toAdminResponseList_nullInput_returnsEmptyList() {
        List<UserResponse> result = userMapper.toAdminResponse((List<User>) null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toAdminResponse(List): returns empty list when input is empty")
    void toAdminResponseList_emptyInput_returnsEmptyList() {
        List<UserResponse> result = userMapper.toAdminResponse(List.of());

        assertThat(result).isEmpty();
    }

    // ---- toUser ----

    @Test
    @DisplayName("toUser: maps RegisterRequest to User with CLIENT role")
    void toUser_validRequest_mapsToClientUser() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "password", 1L, 2L, null, null, null);

        User user = userMapper.toUser(request);

        assertThat(user).isNotNull();
        assertThat(user.getFirstName()).isEqualTo("Mario");
        assertThat(user.getLastName()).isEqualTo("Rossi");
        assertThat(user.getEmail()).isEqualTo("mario@test.com");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getRole()).isEqualTo(Role.CLIENT);
    }

    @Test
    @DisplayName("toUser: returns null when request is null")
    void toUser_nullRequest_returnsNull() {
        assertThat(userMapper.toUser(null)).isNull();
    }

    @Test
    @DisplayName("toUser: maps profilePicture when present in request")
    void toUser_requestWithProfilePicture_mapsProfilePicture() {
        RegisterRequest request = new RegisterRequest(
                "Mario", "Rossi", "mario@test.com", "password", 1L, 2L, "http://img.url/pic.png", null, null);

        User user = userMapper.toUser(request);

        assertThat(user.getProfilePicture()).isEqualTo("http://img.url/pic.png");
    }

    // ---- toBasicInfoResponse ----

    @Test
    @DisplayName("toBasicInfoResponse: maps all fields including role name")
    void toBasicInfoResponse_mapsAllFields() {
        User user = buildUser(7L, "Luca", "Bianchi", "luca@test.com", Role.CLIENT);

        ClientBasicInfoResponse response = userMapper.toBasicInfoResponse(user);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getFirstName()).isEqualTo("Luca");
        assertThat(response.getLastName()).isEqualTo("Bianchi");
        assertThat(response.getEmail()).isEqualTo("luca@test.com");
        assertThat(response.getProfilePictureUrl()).isEqualTo("http://pic.url/7");
        assertThat(response.getRole()).isEqualTo("CLIENT");
    }

    @Test
    @DisplayName("toBasicInfoResponse: role is null when user has no role")
    void toBasicInfoResponse_nullRole_returnsNullRoleString() {
        User user = new User();
        user.setId(8L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@test.com");
        user.setRole(null);

        ClientBasicInfoResponse response = userMapper.toBasicInfoResponse(user);

        assertThat(response.getRole()).isNull();
    }

    // ---- toProfessionalSummary ----

    @Test
    @DisplayName("toProfessionalSummary: maps id, fullName, and role")
    void toProfessionalSummary_mapsFields() {
        User pro = buildUser(9L, "Giulia", "Marchi", "giulia@test.com", Role.NUTRITIONIST);

        ProfessionalSummaryResponse summary = userMapper.toProfessionalSummary(pro);

        assertThat(summary.getId()).isEqualTo(9L);
        assertThat(summary.getFullName()).isEqualTo("Giulia Marchi");
        assertThat(summary.getRole()).isEqualTo(Role.NUTRITIONIST);
    }
}
