package com.project.kore.service.impl;

import com.project.kore.enums.Role;
import com.project.kore.exception.common.CustomResourceNotFoundException;
import com.project.kore.model.User;
import com.project.kore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User clientUser;
    private User ptUser;
    private User nutriUser;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId(1L);
        clientUser.setEmail("client@test.com");
        clientUser.setFirstName("Luca");
        clientUser.setLastName("Rossi");
        clientUser.setRole(Role.CLIENT);
        clientUser.setDeleted(false);

        ptUser = new User();
        ptUser.setId(2L);
        ptUser.setEmail("pt@test.com");
        ptUser.setFirstName("Marco");
        ptUser.setLastName("Bianchi");
        ptUser.setRole(Role.PERSONAL_TRAINER);
        ptUser.setDeleted(false);

        nutriUser = new User();
        nutriUser.setId(3L);
        nutriUser.setEmail("nutri@test.com");
        nutriUser.setFirstName("Sara");
        nutriUser.setLastName("Verdi");
        nutriUser.setRole(Role.NUTRITIONIST);
        nutriUser.setDeleted(false);
    }

    // ---- getUserById ----

    @Test
    @DisplayName("getUserById: returns user when found by id")
    void getUserById_found_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));

        User result = userService.getUserById(1L);

        assertThat(result).isSameAs(clientUser);
    }

    @Test
    @DisplayName("getUserById: throws ResourceNotFoundException when user id does not exist")
    void getUserById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(CustomResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---- getUserByEmail ----

    @Test
    @DisplayName("getUserByEmail: returns active user when email exists and is not deleted")
    void getUserByEmail_activeUser_returnsUser() {
        when(userRepository.findByEmailAndDeletedFalse("client@test.com"))
                .thenReturn(Optional.of(clientUser));

        User result = userService.getUserByEmail("client@test.com");

        assertThat(result).isSameAs(clientUser);
    }

    @Test
    @DisplayName("getUserByEmail: throws ResourceNotFoundException when email is not found or user is deleted")
    void getUserByEmail_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByEmailAndDeletedFalse("ghost@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("ghost@test.com"))
                .isInstanceOf(CustomResourceNotFoundException.class)
                .hasMessageContaining("ghost@test.com");
    }

    // ---- existsByEmail ----

    @Test
    @DisplayName("existsByEmail: returns true when an active user with that email exists")
    void existsByEmail_activeUserExists_returnsTrue() {
        when(userRepository.findByEmailAndDeletedFalse("client@test.com"))
                .thenReturn(Optional.of(clientUser));

        assertThat(userService.existsByEmail("client@test.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail: returns false when no active user with that email exists")
    void existsByEmail_noUser_returnsFalse() {
        when(userRepository.findByEmailAndDeletedFalse("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThat(userService.existsByEmail("unknown@test.com")).isFalse();
    }

    // ---- save ----

    @Test
    @DisplayName("save: persists user and returns the saved entity")
    void save_persistsAndReturnsUser() {
        when(userRepository.save(clientUser)).thenReturn(clientUser);

        User result = userService.save(clientUser);

        assertThat(result).isSameAs(clientUser);
        verify(userRepository).save(clientUser);
    }

    // ---- findByRole ----

    @Test
    @DisplayName("findByRole: returns all active users with the given role")
    void findByRole_returnsActiveUsersWithRole() {
        when(userRepository.findByRoleAndDeletedFalse(Role.PERSONAL_TRAINER))
                .thenReturn(List.of(ptUser));

        List<User> result = userService.findByRole(Role.PERSONAL_TRAINER);

        assertThat(result).hasSize(1).containsExactly(ptUser);
        verify(userRepository).findByRoleAndDeletedFalse(Role.PERSONAL_TRAINER);
    }

    @Test
    @DisplayName("findByRole: returns empty list when no active user has the given role")
    void findByRole_noMatch_returnsEmpty() {
        when(userRepository.findByRoleAndDeletedFalse(Role.ADMIN)).thenReturn(List.of());

        assertThat(userService.findByRole(Role.ADMIN)).isEmpty();
    }

    // ---- findAll ----

    @Test
    @DisplayName("findAll: returns all non-deleted users")
    void findAll_returnsAllActiveUsers() {
        when(userRepository.findAllByDeletedFalse()).thenReturn(List.of(clientUser, ptUser, nutriUser));

        List<User> result = userService.findAll();

        assertThat(result).hasSize(3).containsExactlyInAnyOrder(clientUser, ptUser, nutriUser);
    }

    // ---- countByAssignedPT ----

    @Test
    @DisplayName("countByAssignedPT: delegates to repository and returns active client count")
    void countByAssignedPT_returnsCorrectCount() {
        when(userRepository.countByAssignedPTAndDeletedFalse(ptUser)).thenReturn(5L);

        assertThat(userService.countByAssignedPT(ptUser)).isEqualTo(5L);
        verify(userRepository).countByAssignedPTAndDeletedFalse(ptUser);
    }

    @Test
    @DisplayName("countByAssignedPT: returns zero when PT has no assigned active clients")
    void countByAssignedPT_noClients_returnsZero() {
        when(userRepository.countByAssignedPTAndDeletedFalse(ptUser)).thenReturn(0L);

        assertThat(userService.countByAssignedPT(ptUser)).isZero();
    }

    // ---- countByAssignedNutritionist ----

    @Test
    @DisplayName("countByAssignedNutritionist: delegates to repository and returns active client count")
    void countByAssignedNutritionist_returnsCorrectCount() {
        when(userRepository.countByAssignedNutritionistAndDeletedFalse(nutriUser)).thenReturn(3L);

        assertThat(userService.countByAssignedNutritionist(nutriUser)).isEqualTo(3L);
        verify(userRepository).countByAssignedNutritionistAndDeletedFalse(nutriUser);
    }

    // ---- findByAssignedPT ----

    @Test
    @DisplayName("findByAssignedPT: returns active clients assigned to the given PT")
    void findByAssignedPT_returnsAssignedClients() {
        clientUser.setAssignedPT(ptUser);
        when(userRepository.findByAssignedPTAndDeletedFalse(ptUser)).thenReturn(List.of(clientUser));

        List<User> result = userService.findByAssignedPT(ptUser);

        assertThat(result).containsExactly(clientUser);
    }

    @Test
    @DisplayName("findByAssignedPT: returns empty list when PT has no active assigned clients")
    void findByAssignedPT_noAssignedClients_returnsEmpty() {
        when(userRepository.findByAssignedPTAndDeletedFalse(ptUser)).thenReturn(List.of());

        assertThat(userService.findByAssignedPT(ptUser)).isEmpty();
    }

    // ---- findByAssignedNutritionist ----

    @Test
    @DisplayName("findByAssignedNutritionist: returns active clients assigned to the given nutritionist")
    void findByAssignedNutritionist_returnsAssignedClients() {
        clientUser.setAssignedNutritionist(nutriUser);
        when(userRepository.findByAssignedNutritionistAndDeletedFalse(nutriUser))
                .thenReturn(List.of(clientUser));

        List<User> result = userService.findByAssignedNutritionist(nutriUser);

        assertThat(result).containsExactly(clientUser);
    }

    // ---- existsUserByEmailExcluding ----

    @Test
    @DisplayName("existsUserByEmailExcluding: returns true when another active user already has that email")
    void existsUserByEmailExcluding_conflictExists_returnsTrue() {
        when(userRepository.findByEmailAndIdIsNotAndDeletedFalse("client@test.com", 99L))
                .thenReturn(Optional.of(clientUser));

        assertThat(userService.existsUserByEmailExcluding("client@test.com", 99L)).isTrue();
    }

    @Test
    @DisplayName("existsUserByEmailExcluding: returns false when email is unique excluding the given user")
    void existsUserByEmailExcluding_noConflict_returnsFalse() {
        when(userRepository.findByEmailAndIdIsNotAndDeletedFalse("new@test.com", 1L))
                .thenReturn(Optional.empty());

        assertThat(userService.existsUserByEmailExcluding("new@test.com", 1L)).isFalse();
    }

    // ---- deleteUser ----

    @Test
    @DisplayName("deleteUser: soft-deletes user by setting deleted=true and saving")
    void deleteUser_client_softDeletesWithoutClearingAssignments() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(clientUser));
        when(userRepository.save(any())).thenReturn(clientUser);

        userService.deleteUser(1L);

        assertThat(clientUser.isDeleted()).isTrue();
        verify(userRepository).save(clientUser);
        verify(userRepository, never()).clearAssignedPT(any());
        verify(userRepository, never()).clearAssignedNutritionist(any());
    }

    @Test
    @DisplayName("deleteUser: when deleting a PERSONAL_TRAINER also clears PT assignments on all clients")
    void deleteUser_personalTrainer_softDeletesAndClearsPTAssignments() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(ptUser));
        when(userRepository.save(ptUser)).thenReturn(ptUser);

        userService.deleteUser(2L);

        assertThat(ptUser.isDeleted()).isTrue();
        verify(userRepository).save(ptUser);
        verify(userRepository).clearAssignedPT(2L);
        verify(userRepository, never()).clearAssignedNutritionist(any());
    }

    @Test
    @DisplayName("deleteUser: when deleting a NUTRITIONIST also clears nutritionist assignments on all clients")
    void deleteUser_nutritionist_softDeletesAndClearsNutritionistAssignments() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(nutriUser));
        when(userRepository.save(nutriUser)).thenReturn(nutriUser);

        userService.deleteUser(3L);

        assertThat(nutriUser.isDeleted()).isTrue();
        verify(userRepository).save(nutriUser);
        verify(userRepository).clearAssignedNutritionist(3L);
        verify(userRepository, never()).clearAssignedPT(any());
    }

    @Test
    @DisplayName("deleteUser: throws ResourceNotFoundException when user id does not exist")
    void deleteUser_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(CustomResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteUser: does not call clearAssignedPT or clearAssignedNutritionist for MODERATOR role")
    void deleteUser_moderator_softDeletesWithoutClearingAssignments() {
        User moderator = new User();
        moderator.setId(10L);
        moderator.setRole(Role.MODERATOR);
        when(userRepository.findById(10L)).thenReturn(Optional.of(moderator));
        when(userRepository.save(moderator)).thenReturn(moderator);

        userService.deleteUser(10L);

        assertThat(moderator.isDeleted()).isTrue();
        verify(userRepository, never()).clearAssignedPT(any());
        verify(userRepository, never()).clearAssignedNutritionist(any());
    }

    // ---- encodePassword ----

    @Test
    @DisplayName("encodePassword: returns the BCrypt-encoded hash from the PasswordEncoder")
    void encodePassword_returnsEncodedHash() {
        when(passwordEncoder.encode("mySecret123")).thenReturn("$2a$10$hashedvalue");

        String result = userService.encodePassword("mySecret123");

        assertThat(result).isEqualTo("$2a$10$hashedvalue");
        verify(passwordEncoder).encode("mySecret123");
    }

    @Test
    @DisplayName("encodePassword: delegates encoding to PasswordEncoder without modifying the raw value")
    void encodePassword_delegatesToPasswordEncoder() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        userService.encodePassword("pass");

        verify(passwordEncoder, times(1)).encode("pass");
    }
}
