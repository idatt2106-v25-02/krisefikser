package stud.ntnu.krisefikser.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CustomUserDetailsService customUserDetailsService;

  @Test
  void loadUserByUsername_shouldMapRolesAndReturnUserDetails() {
    User user = User.builder()
        .email("test@example.com")
        .password("hashed-password")
        .roles(Set.of(Role.builder().name(Role.RoleType.ADMIN).build()))
        .build();
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    var userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

    assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
    assertThat(userDetails.getPassword()).isEqualTo("hashed-password");
    assertThat(userDetails.getAuthorities())
        .extracting("authority")
        .containsExactlyInAnyOrder("ROLE_ADMIN");
  }

  @Test
  void loadUserByUsername_shouldThrowWhenUserMissing() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("missing@example.com"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("missing@example.com");
  }
}
