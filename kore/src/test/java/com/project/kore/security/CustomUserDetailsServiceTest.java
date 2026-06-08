package com.project.kore.security;

import com.project.kore.model.User;
import com.project.kore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService unit tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(userRepository);

        user = new User();
        user.setId(1L);
        user.setEmail("mario@test.com");
        user.setPassword("encoded_pass");
    }

    // ─── getUserDetails bean ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserDetails: user found by email → returns User as UserDetails")
    void getUserDetails_userFound_returnsUserDetails() {
        when(userRepository.findByEmailAndDeletedFalse("mario@test.com"))
                .thenReturn(Optional.of(user));

        UserDetailsService service = customUserDetailsService.getUserDetails();
        UserDetails result = service.loadUserByUsername("mario@test.com");

        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("getUserDetails: user not found by email → throws UsernameNotFoundException")
    void getUserDetails_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmailAndDeletedFalse("unknown@test.com"))
                .thenReturn(Optional.empty());

        UserDetailsService service = customUserDetailsService.getUserDetails();

        assertThatThrownBy(() -> service.loadUserByUsername("unknown@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown@test.com");
    }

    @Test
    @DisplayName("getUserDetails: deleted user (soft-deleted) is not returned → throws UsernameNotFoundException")
    void getUserDetails_deletedUser_throwsUsernameNotFoundException() {
        // findByEmailAndDeletedFalse excludes deleted users at query level; repo returns empty
        when(userRepository.findByEmailAndDeletedFalse("deleted@test.com"))
                .thenReturn(Optional.empty());

        UserDetailsService service = customUserDetailsService.getUserDetails();

        assertThatThrownBy(() -> service.loadUserByUsername("deleted@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("getUserDetails: returns a non-null UserDetailsService bean")
    void getUserDetails_returnsBeanNotNull() {
        UserDetailsService service = customUserDetailsService.getUserDetails();

        assertThat(service).isNotNull();
    }
}
