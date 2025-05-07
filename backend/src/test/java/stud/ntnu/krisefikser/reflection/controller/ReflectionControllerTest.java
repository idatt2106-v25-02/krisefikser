package stud.ntnu.krisefikser.reflection.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.common.TestDataFactory;
import stud.ntnu.krisefikser.common.TestSecurityConfig;
import stud.ntnu.krisefikser.household.exception.HouseholdNotFoundException;
import stud.ntnu.krisefikser.reflection.dto.CreateReflectionRequest;
import stud.ntnu.krisefikser.reflection.dto.ReflectionResponse;
import stud.ntnu.krisefikser.reflection.dto.UpdateReflectionRequest;
import stud.ntnu.krisefikser.reflection.enums.VisibilityType;
import stud.ntnu.krisefikser.reflection.exception.ReflectionNotFoundException;
import stud.ntnu.krisefikser.reflection.exception.UnauthorizedReflectionAccessException;
import stud.ntnu.krisefikser.reflection.service.ReflectionService;

@WebMvcTest(controllers = ReflectionController.class)
@Import(TestSecurityConfig.class)
class ReflectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReflectionService reflectionService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private TokenService tokenService;

    private CreateReflectionRequest createReflectionRequest;
    private UpdateReflectionRequest updateReflectionRequest;
    private ReflectionResponse reflectionResponse;
    private UUID validId;
    private UUID authorId;
    private UUID householdId;

    @BeforeEach
    void setUp() {
        validId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        householdId = UUID.randomUUID();

        createReflectionRequest = TestDataFactory.createTestReflectionRequest(
                "Test Reflection",
                "This is a test reflection content",
                VisibilityType.PUBLIC,
                null);

        updateReflectionRequest = TestDataFactory.createTestUpdateReflectionRequest(
                "Updated Test Reflection",
                "This is an updated test reflection content",
                VisibilityType.PUBLIC,
                null);

        reflectionResponse = TestDataFactory.createTestReflectionResponse(
                validId,
                "Test Reflection",
                "This is a test reflection content",
                VisibilityType.PUBLIC,
                authorId,
                "Test User",
                null,
                null);
    }

    @Test
    @WithMockUser
    void createReflection_whenAuthenticated_shouldReturnCreatedReflection() throws Exception {
        // Arrange
        when(reflectionService.createReflection(any(CreateReflectionRequest.class)))
                .thenReturn(reflectionResponse);

        // Act & Assert
        mockMvc.perform(post("/api/reflections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReflectionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(validId.toString()))
                .andExpect(jsonPath("$.title").value("Test Reflection"))
                .andExpect(jsonPath("$.content").value("This is a test reflection content"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.authorId").value(authorId.toString()));

        verify(reflectionService).createReflection(any(CreateReflectionRequest.class));
    }

    @Test
    void createReflection_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/reflections")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReflectionRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getReflectionById_whenAuthenticated_shouldReturnReflection() throws Exception {
        // Arrange
        when(reflectionService.getReflectionById(validId)).thenReturn(reflectionResponse);

        // Act & Assert
        mockMvc.perform(get("/api/reflections/" + validId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validId.toString()))
                .andExpect(jsonPath("$.title").value("Test Reflection"))
                .andExpect(jsonPath("$.content").value("This is a test reflection content"));

        verify(reflectionService).getReflectionById(validId);
    }

    @Test
    @WithMockUser
    void getReflectionById_whenReflectionNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(reflectionService.getReflectionById(nonExistentId))
                .thenThrow(new ReflectionNotFoundException(nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/api/reflections/" + nonExistentId))
                .andExpect(status().isNotFound());

        verify(reflectionService).getReflectionById(nonExistentId);
    }

    @Test
    @WithMockUser
    void getReflectionById_whenUnauthorized_shouldReturnForbidden() throws Exception {
        // Arrange
        when(reflectionService.getReflectionById(validId))
                .thenThrow(new UnauthorizedReflectionAccessException());

        // Act & Assert
        mockMvc.perform(get("/api/reflections/" + validId))
                .andExpect(status().isForbidden());

        verify(reflectionService).getReflectionById(validId);
    }

    @Test
    void getReflectionById_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reflections/" + validId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void updateReflection_whenAuthenticated_shouldReturnUpdatedReflection() throws Exception {
        // Arrange
        ReflectionResponse updatedResponse = TestDataFactory.createTestReflectionResponse(
                validId,
                "Updated Test Reflection",
                "This is an updated test reflection content",
                VisibilityType.PUBLIC,
                authorId,
                "Test User",
                null,
                null);

        when(reflectionService.updateReflection(validId, updateReflectionRequest))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/reflections/" + validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReflectionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Test Reflection"))
                .andExpect(jsonPath("$.content").value("This is an updated test reflection content"));

        verify(reflectionService).updateReflection(validId, updateReflectionRequest);
    }

    @Test
    @WithMockUser
    void updateReflection_whenReflectionNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(reflectionService.updateReflection(nonExistentId, updateReflectionRequest))
                .thenThrow(new ReflectionNotFoundException(nonExistentId));

        // Act & Assert
        mockMvc.perform(put("/api/reflections/" + nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReflectionRequest)))
                .andExpect(status().isNotFound());

        verify(reflectionService).updateReflection(nonExistentId, updateReflectionRequest);
    }

    @Test
    @WithMockUser
    void updateReflection_whenUnauthorized_shouldReturnForbidden() throws Exception {
        // Arrange
        when(reflectionService.updateReflection(validId, updateReflectionRequest))
                .thenThrow(new UnauthorizedReflectionAccessException());

        // Act & Assert
        mockMvc.perform(put("/api/reflections/" + validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReflectionRequest)))
                .andExpect(status().isForbidden());

        verify(reflectionService).updateReflection(validId, updateReflectionRequest);
    }

    @Test
    void updateReflection_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/reflections/" + validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReflectionRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deleteReflection_whenAuthenticated_shouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/reflections/" + validId))
                .andExpect(status().isNoContent());

        verify(reflectionService).deleteReflection(validId);
    }

    @Test
    @WithMockUser
    void deleteReflection_whenReflectionNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new ReflectionNotFoundException(nonExistentId))
                .when(reflectionService).deleteReflection(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/api/reflections/" + nonExistentId))
                .andExpect(status().isNotFound());

        verify(reflectionService).deleteReflection(nonExistentId);
    }

    @Test
    @WithMockUser
    void deleteReflection_whenUnauthorized_shouldReturnForbidden() throws Exception {
        // Arrange
        doThrow(new UnauthorizedReflectionAccessException())
                .when(reflectionService).deleteReflection(validId);

        // Act & Assert
        mockMvc.perform(delete("/api/reflections/" + validId))
                .andExpect(status().isForbidden());

        verify(reflectionService).deleteReflection(validId);
    }

    @Test
    void deleteReflection_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/reflections/" + validId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getAccessibleReflections_whenAuthenticated_shouldReturnReflectionsList() throws Exception {
        // Arrange
        List<ReflectionResponse> reflections = Collections.singletonList(reflectionResponse);
        when(reflectionService.getAccessibleReflections()).thenReturn(reflections);

        // Act & Assert
        mockMvc.perform(get("/api/reflections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(validId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Reflection"));

        verify(reflectionService).getAccessibleReflections();
    }

    @Test
    void getAccessibleReflections_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reflections"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getCurrentUserReflections_whenAuthenticated_shouldReturnReflectionsList() throws Exception {
        // Arrange
        List<ReflectionResponse> reflections = Collections.singletonList(reflectionResponse);
        when(reflectionService.getCurrentUserReflections()).thenReturn(reflections);

        // Act & Assert
        mockMvc.perform(get("/api/reflections/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(validId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Reflection"));

        verify(reflectionService).getCurrentUserReflections();
    }

    @Test
    void getCurrentUserReflections_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reflections/my"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getPublicReflections_whenAuthenticated_shouldReturnReflectionsList() throws Exception {
        // Arrange
        List<ReflectionResponse> reflections = Collections.singletonList(reflectionResponse);
        when(reflectionService.getPublicReflections()).thenReturn(reflections);

        // Act & Assert
        mockMvc.perform(get("/api/reflections/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(validId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Reflection"));

        verify(reflectionService).getPublicReflections();
    }

    @Test
    void getPublicReflections_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reflections/public"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getHouseholdReflections_whenAuthenticated_shouldReturnReflectionsList() throws Exception {
        // Arrange
        ReflectionResponse householdReflection = TestDataFactory.createTestReflectionResponse(
                validId,
                "Household Reflection",
                "This is a household reflection",
                VisibilityType.HOUSEHOLD,
                authorId,
                "Test User",
                householdId,
                "Test Household");

        List<ReflectionResponse> reflections = Collections.singletonList(householdReflection);
        when(reflectionService.getHouseholdReflections()).thenReturn(reflections);

        // Act & Assert
        mockMvc.perform(get("/api/reflections/household"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(validId.toString()))
                .andExpect(jsonPath("$[0].title").value("Household Reflection"))
                .andExpect(jsonPath("$[0].householdId").value(householdId.toString()));

        verify(reflectionService).getHouseholdReflections();
    }

    @Test
    @WithMockUser
    void getHouseholdReflections_whenNoActiveHousehold_shouldReturnNotFound() throws Exception {
        // Arrange
        when(reflectionService.getHouseholdReflections()).thenThrow(new HouseholdNotFoundException());

        // Act & Assert
        mockMvc.perform(get("/api/reflections/household"))
                .andExpect(status().isNotFound());

        verify(reflectionService).getHouseholdReflections();
    }

    @Test
    void getHouseholdReflections_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reflections/household"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllReflections_whenAdmin_shouldReturnAllReflections() throws Exception {
        // Arrange
        List<ReflectionResponse> reflections = Collections.singletonList(reflectionResponse);
        when(reflectionService.getAllReflections()).thenReturn(reflections);

        // Act & Assert
        mockMvc.perform(get("/api/reflections/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(validId.toString()))
                .andExpect(jsonPath("$[0].title").value("Test Reflection"));

        verify(reflectionService).getAllReflections();
    }

    @Test
    @WithMockUser
    void getAllReflections_whenNotAdmin_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reflections/admin/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllReflections_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/reflections/admin/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminUpdateReflection_whenAdmin_shouldReturnUpdatedReflection() throws Exception {
        // Arrange
        ReflectionResponse updatedResponse = TestDataFactory.createTestReflectionResponse(
                validId,
                "Admin Updated Reflection",
                "This is an admin updated reflection",
                VisibilityType.PUBLIC,
                authorId,
                "Test User",
                null,
                null);

        when(reflectionService.updateReflection(validId, updateReflectionRequest))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/reflections/admin/" + validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReflectionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin Updated Reflection"));

        verify(reflectionService).updateReflection(validId, updateReflectionRequest);
    }

    @Test
    @WithMockUser
    void adminUpdateReflection_whenNotAdmin_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/reflections/admin/" + validId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReflectionRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDeleteReflection_whenAdmin_shouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/reflections/admin/" + validId))
                .andExpect(status().isNoContent());

        verify(reflectionService).deleteReflection(validId);
    }

    @Test
    @WithMockUser
    void adminDeleteReflection_whenNotAdmin_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/reflections/admin/" + validId))
                .andExpect(status().isForbidden());
    }
}