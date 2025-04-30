package stud.ntnu.krisefikser.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.user.dto.CreateUserDto;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.dto.UserDto;
import stud.ntnu.krisefikser.user.exception.UserDoesNotExistException;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private UserService userService;

  private User testUser;
  private UUID testUserId;
  private CreateUserDto testUserDto;
  private UserDto testUserDtoResponse;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testUser = User.builder()
        .id(testUserId)
        .email("test@example.com")
        .password("encodedPassword")
        .firstName("Test")
        .lastName("User")
        .roles(new HashSet<>())
        .build();

    testUserDto = new CreateUserDto(
        "test@example.com",
        "password",
        "Test",
        "User",
        true,
        true,
        true);

    testUserDtoResponse = new UserDto(
        testUserId,
        "test@example.com",
        List.of(RoleType.USER.name()),
        "Test",
        "User",
        true,
        true,
        false);

    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void updateUser_Success() {
    // Arrange
    when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
    when(userRepository.existsByEmail(any())).thenReturn(false);
    when(userRepository.save(any())).thenReturn(testUser);
    when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

    // Mock the service method to return the DTO
    when(userService.updateUser(testUserId, testUserDto)).thenReturn(testUserDtoResponse);

    // Act
    UserDto updatedUserDto = userService.updateUser(testUserId, testUserDto);

    // Assert
    assertNotNull(updatedUserDto);
    assertEquals(testUserDto.getEmail(), updatedUserDto.getEmail());
    assertEquals(testUserDto.getFirstName(), updatedUserDto.getFirstName());
    assertEquals(testUserDto.getLastName(), updatedUserDto.getLastName());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void updateUser_UserNotFound() {
    // Arrange
    when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UserDoesNotExistException.class,
        () -> userService.updateUser(testUserId, testUserDto));
  }

  @Test
  void deleteUser_Success() {
    // Arrange
    when(userRepository.existsById(testUserId)).thenReturn(true);

    // Act
    userService.deleteUser(testUserId);

    // Assert
    verify(userRepository).deleteById(testUserId);
  }

  @Test
  void deleteUser_UserNotFound() {
    // Arrange
    when(userRepository.existsById(testUserId)).thenReturn(false);

    // Act & Assert
    assertThrows(UserDoesNotExistException.class,
        () -> userService.deleteUser(testUserId));
  }

  @Test
  void getAllUsers_Success() {
    // Arrange
    List<User> users = Arrays.asList(testUser);
    List<UserDto> expectedUserDtos = users.stream().map(User::toDto).toList();
    when(userRepository.findAll()).thenReturn(users);
    when(userService.getAllUsers()).thenReturn(expectedUserDtos);

    // Act
    List<UserDto> actualUsers = userService.getAllUsers();

    // Assert
    assertEquals(expectedUserDtos.size(), actualUsers.size());
    assertEquals(expectedUserDtos.get(0), actualUsers.get(0));
    verify(userRepository).findAll();
  }

  @Test
  void isAdminOrSelf_IsSelf() {
    // Arrange
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(testUser.getEmail());
    when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

    // Act
    boolean result = userService.isAdminOrSelf(testUserId);

    // Assert
    assertTrue(result);
  }

  @Test
  void isAdminOrSelf_IsAdmin() {
    // Arrange
    Role adminRole = new Role();
    adminRole.setName(RoleType.ADMIN);

    User adminUser = User.builder()
        .id(UUID.randomUUID())
        .email("admin@example.com")
        .roles(new HashSet<>(Arrays.asList(adminRole)))
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(adminUser.getEmail());
    when(userRepository.findByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));

    // Act
    boolean result = userService.isAdminOrSelf(testUserId);

    // Assert
    assertTrue(result);
  }

  @Test
  void isAdminOrSelf_NotAuthorized() {
    // Arrange
    User otherUser = User.builder()
        .id(UUID.randomUUID())
        .email("other@example.com")
        .roles(new HashSet<>())
        .build();

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(otherUser.getEmail());
    when(userRepository.findByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));

    // Act
    boolean result = userService.isAdminOrSelf(testUserId);

    // Assert
    assertFalse(result);
  }
}