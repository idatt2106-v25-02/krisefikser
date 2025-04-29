package stud.ntnu.krisefikser.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.user.dto.CreateUserDto;
import stud.ntnu.krisefikser.user.dto.UserDto;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;
    private UUID testUserId;
    private CreateUserDto testUserDto;
    private UserDto testUserDtoResponse;

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
                .roles(new HashSet<>(Arrays.asList(userRole)))
                .build();

        testUserDto = new CreateUserDto(
                "test@example.com",
                "password",
                "Test",
                "User",
                true,
                true,
                true);

        testUserDtoResponse = testUser.toDto();
    }

    @Test
    void getAllUsers_Success() throws Exception {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].email").value(testUserDtoResponse.getEmail()))
                .andExpect(jsonPath("$[0].firstName").value(testUserDtoResponse.getFirstName()))
                .andExpect(jsonPath("$[0].lastName").value(testUserDtoResponse.getLastName()));
    }

    @Test
    void updateUser_Success() throws Exception {
        // Arrange
        when(userService.isAdminOrSelf(testUserId)).thenReturn(true);
        when(userService.updateUser(testUserId, testUserDto)).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value(testUserDtoResponse.getEmail()))
                .andExpect(jsonPath("$.firstName").value(testUserDtoResponse.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(testUserDtoResponse.getLastName()));
    }

    @Test
    void updateUser_Unauthorized() throws Exception {
        // Arrange
        when(userService.isAdminOrSelf(testUserId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(put("/api/users/{userId}", testUserId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUserDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_Success() throws Exception {
        // Arrange
        when(userService.isAdminOrSelf(testUserId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/users/{userId}", testUserId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_Unauthorized() throws Exception {
        // Arrange
        when(userService.isAdminOrSelf(testUserId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/users/{userId}", testUserId))
                .andExpect(status().isForbidden());
    }
}