package stud.ntnu.krisefikser.user.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private TokenService tokenService;

  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  private User testUser;
  private UUID testUserId;
  private CreateUser testUserDto;
  private UserResponse testUserResponseResponse;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    Role userRole = new Role();
    userRole.setName(RoleType.USER);

    testUser = User.builder()
        .id(testUserId)
        .email("test@example.com")
        .password("encodedPassword")
        .firstName("Test")
        .lastName("User")
        .roles(new HashSet<>(List.of(userRole)))
        .build();

    testUserDto = new CreateUser(
        "test@example.com",
        "password",
        "Test",
        "User",
        true,
        true,
        true,
        List.of(RoleType.USER)
    );

    testUserResponseResponse = testUser.toDto();
  }

  @Test
  @WithMockUser
  void getAllUsers_Success() throws Exception {
    // Arrange
    List<User> users = Arrays.asList(testUser);
    when(userService.getAllUsers()).thenReturn(users);

    // Act & Assert
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].email").value(testUserResponseResponse.getEmail()))
        .andExpect(jsonPath("$[0].firstName").value(testUserResponseResponse.getFirstName()))
        .andExpect(jsonPath("$[0].lastName").value(testUserResponseResponse.getLastName()));
  }

  @Test
  @WithMockUser
  void updateUser_Success() throws Exception {
    // Arrange
    when(userService.isAdminOrSelf(testUserId)).thenReturn(true);
    when(userService.updateUser(testUserId, testUserDto)).thenReturn(testUser);

    // Act & Assert
    mockMvc.perform(put("/api/users/{userId}", testUserId)
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUserDto)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.email").value(testUserResponseResponse.getEmail()))
        .andExpect(jsonPath("$.firstName").value(testUserResponseResponse.getFirstName()))
        .andExpect(jsonPath("$.lastName").value(testUserResponseResponse.getLastName()));
  }

  @Test
  @WithMockUser
  void updateUser_Unauthorized() throws Exception {
    // Arrange
    when(userService.isAdminOrSelf(testUserId)).thenReturn(false);

    // Act & Assert
    mockMvc.perform(put("/api/users/{userId}", testUserId)
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(testUserDto)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void deleteUser_Success() throws Exception {
    // Arrange
    when(userService.isAdminOrSelf(testUserId)).thenReturn(true);

    // Act & Assert
    mockMvc.perform(delete("/api/users/{userId}", testUserId)
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void deleteUser_Unauthorized() throws Exception {
    // Arrange
    when(userService.isAdminOrSelf(testUserId)).thenReturn(false);

    // Act & Assert
    mockMvc.perform(delete("/api/users/{userId}", testUserId)
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isUnauthorized());
  }
}