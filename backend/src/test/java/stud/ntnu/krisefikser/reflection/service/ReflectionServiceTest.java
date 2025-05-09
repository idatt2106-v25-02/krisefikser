package stud.ntnu.krisefikser.reflection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
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
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.household.entity.Household;
import stud.ntnu.krisefikser.household.exception.HouseholdNotFoundException;
import stud.ntnu.krisefikser.household.service.HouseholdService;
import stud.ntnu.krisefikser.map.service.EventService;
import stud.ntnu.krisefikser.reflection.dto.CreateReflectionRequest;
import stud.ntnu.krisefikser.reflection.dto.ReflectionResponse;
import stud.ntnu.krisefikser.reflection.dto.UpdateReflectionRequest;
import stud.ntnu.krisefikser.reflection.entity.Reflection;
import stud.ntnu.krisefikser.reflection.enums.VisibilityType;
import stud.ntnu.krisefikser.reflection.exception.ReflectionNotFoundException;
import stud.ntnu.krisefikser.reflection.exception.UnauthorizedReflectionAccessException;
import stud.ntnu.krisefikser.reflection.repository.ReflectionRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class ReflectionServiceTest {

  @Mock
  private ReflectionRepository reflectionRepository;

  @Mock
  private UserService userService;

  @Mock
  private HouseholdService householdService;

  @Mock
  private EventService eventService;

  @InjectMocks
  private ReflectionService reflectionService;

  private User currentUser;
  private User otherUser;
  private User adminUser;
  private Household household;
  private Reflection publicReflection;
  private Reflection privateReflection;
  private Reflection householdReflection;
  private CreateReflectionRequest createRequest;
  private UpdateReflectionRequest updateRequest;
  private UUID validId;

  @BeforeEach
  void setUp() {
    validId = UUID.randomUUID();
    UUID householdId = UUID.randomUUID();

    // Set up roles
    Role userRole = new Role();
    userRole.setName(RoleType.USER);

    Role adminRole = new Role();
    adminRole.setName(RoleType.ADMIN);

    // Set up users
    currentUser = User.builder()
        .id(UUID.randomUUID())
        .email("current@example.com")
        .firstName("Current")
        .lastName("User")
        .roles(new HashSet<>(Collections.singletonList(userRole)))
        .build();

    otherUser = User.builder()
        .id(UUID.randomUUID())
        .email("other@example.com")
        .firstName("Other")
        .lastName("User")
        .roles(new HashSet<>(Collections.singletonList(userRole)))
        .build();

    adminUser = User.builder()
        .id(UUID.randomUUID())
        .email("admin@example.com")
        .firstName("Admin")
        .lastName("User")
        .roles(new HashSet<>(Arrays.asList(userRole, adminRole)))
        .build();

    // Set up household
    household = Household.builder()
        .id(householdId)
        .name("Test Household")
        .owner(currentUser)
        .build();

    currentUser.setActiveHousehold(household);

    // Set up reflections
    publicReflection = Reflection.builder()
        .id(validId)
        .title("Public Reflection")
        .content("This is a public reflection")
        .author(currentUser)
        .visibility(VisibilityType.PUBLIC)
        .build();

    privateReflection = Reflection.builder()
        .id(UUID.randomUUID())
        .title("Private Reflection")
        .content("This is a private reflection")
        .author(currentUser)
        .visibility(VisibilityType.PRIVATE)
        .build();

    householdReflection = Reflection.builder()
        .id(UUID.randomUUID())
        .title("Household Reflection")
        .content("This is a household reflection")
        .author(currentUser)
        .visibility(VisibilityType.HOUSEHOLD)
        .household(household)
        .build();

    // Set up requests
    createRequest = CreateReflectionRequest.builder()
        .title("New Reflection")
        .content("This is a new reflection")
        .visibility(VisibilityType.PUBLIC)
        .build();

    updateRequest = UpdateReflectionRequest.builder()
        .title("Updated Reflection")
        .content("This is an updated reflection")
        .visibility(VisibilityType.PUBLIC)
        .build();
  }

  @Test
  void createReflection_whenPublic_shouldReturnCreatedReflection() {
    // Arrange
    createRequest.setVisibility(VisibilityType.PUBLIC);

    Reflection savedReflection = Reflection.builder()
        .id(validId)
        .title(createRequest.getTitle())
        .content(createRequest.getContent())
        .author(currentUser)
        .visibility(createRequest.getVisibility())
        .build();

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(reflectionRepository.save(any(Reflection.class))).thenReturn(savedReflection);

    // Act
    ReflectionResponse result = reflectionService.createReflection(createRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(validId);
    assertThat(result.getTitle()).isEqualTo(createRequest.getTitle());
    assertThat(result.getContent()).isEqualTo(createRequest.getContent());
    assertThat(result.getVisibility()).isEqualTo(VisibilityType.PUBLIC);
    assertThat(result.getAuthorId()).isEqualTo(currentUser.getId());
    assertThat(result.getHouseholdId()).isNull();

    verify(userService).getCurrentUser();
    verify(reflectionRepository).save(any(Reflection.class));
  }

  @Test
  void createReflection_whenPrivate_shouldReturnCreatedReflection() {
    // Arrange
    createRequest.setVisibility(VisibilityType.PRIVATE);

    Reflection savedReflection = Reflection.builder()
        .id(validId)
        .title(createRequest.getTitle())
        .content(createRequest.getContent())
        .author(currentUser)
        .visibility(createRequest.getVisibility())
        .build();

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(reflectionRepository.save(any(Reflection.class))).thenReturn(savedReflection);

    // Act
    ReflectionResponse result = reflectionService.createReflection(createRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getVisibility()).isEqualTo(VisibilityType.PRIVATE);
    assertThat(result.getHouseholdId()).isNull();

    verify(userService).getCurrentUser();
    verify(reflectionRepository).save(any(Reflection.class));
  }

  @Test
  void createReflection_whenHousehold_withActiveHousehold_shouldReturnCreatedReflection() {
    // Arrange
    createRequest.setVisibility(VisibilityType.HOUSEHOLD);

    Reflection savedReflection = Reflection.builder()
        .id(validId)
        .title(createRequest.getTitle())
        .content(createRequest.getContent())
        .author(currentUser)
        .visibility(createRequest.getVisibility())
        .household(household)
        .build();

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(reflectionRepository.save(any(Reflection.class))).thenReturn(savedReflection);

    // Act
    ReflectionResponse result = reflectionService.createReflection(createRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getVisibility()).isEqualTo(VisibilityType.HOUSEHOLD);
    assertThat(result.getHouseholdId()).isEqualTo(household.getId());
    assertThat(result.getHouseholdName()).isEqualTo(household.getName());

    verify(userService).getCurrentUser();
    verify(reflectionRepository).save(any(Reflection.class));
  }

  @Test
  void createReflection_whenHousehold_withSpecifiedHouseholdId_shouldReturnCreatedReflection() {
    // Arrange
    createRequest.setVisibility(VisibilityType.HOUSEHOLD);
    createRequest.setHouseholdId(household.getId());

    Reflection savedReflection = Reflection.builder()
        .id(validId)
        .title(createRequest.getTitle())
        .content(createRequest.getContent())
        .author(currentUser)
        .visibility(createRequest.getVisibility())
        .household(household)
        .build();

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(householdService.getHouseholdById(household.getId())).thenReturn(household);
    when(reflectionRepository.save(any(Reflection.class))).thenReturn(savedReflection);

    // Act
    ReflectionResponse result = reflectionService.createReflection(createRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getVisibility()).isEqualTo(VisibilityType.HOUSEHOLD);
    assertThat(result.getHouseholdId()).isEqualTo(household.getId());

    verify(userService).getCurrentUser();
    verify(householdService).getHouseholdById(household.getId());
    verify(reflectionRepository).save(any(Reflection.class));
  }

  @Test
  void createReflection_whenHousehold_withNoHousehold_shouldThrowException() {
    // Arrange
    createRequest.setVisibility(VisibilityType.HOUSEHOLD);

    User userWithoutHousehold = User.builder()
        .id(UUID.randomUUID())
        .email("nohousehold@example.com")
        .firstName("No")
        .lastName("Household")
        .build();

    when(userService.getCurrentUser()).thenReturn(userWithoutHousehold);

    // Act & Assert
    assertThatThrownBy(() -> reflectionService.createReflection(createRequest))
        .isInstanceOf(HouseholdNotFoundException.class);

    verify(userService).getCurrentUser();
  }

  @Test
  void getReflectionById_whenPublicReflection_shouldReturnReflection() {
    // Arrange
    when(reflectionRepository.findById(validId)).thenReturn(Optional.of(publicReflection));
    when(userService.getCurrentUser()).thenReturn(otherUser);

    // Act
    ReflectionResponse result = reflectionService.getReflectionById(validId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(validId);
    assertThat(result.getTitle()).isEqualTo(publicReflection.getTitle());

    verify(reflectionRepository).findById(validId);
    verify(userService).getCurrentUser();
  }

  @Test
  void getReflectionById_whenPrivateReflection_asAuthor_shouldReturnReflection() {
    // Arrange
    when(reflectionRepository.findById(privateReflection.getId())).thenReturn(
        Optional.of(privateReflection));
    when(userService.getCurrentUser()).thenReturn(currentUser);

    // Act
    ReflectionResponse result = reflectionService.getReflectionById(privateReflection.getId());

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(privateReflection.getId());
    assertThat(result.getTitle()).isEqualTo(privateReflection.getTitle());

    verify(reflectionRepository).findById(privateReflection.getId());
    verify(userService).getCurrentUser();
  }

  @Test
  void getReflectionById_whenPrivateReflection_asOtherUser_shouldThrowException() {
    // Arrange
    when(reflectionRepository.findById(privateReflection.getId())).thenReturn(
        Optional.of(privateReflection));
    when(userService.getCurrentUser()).thenReturn(otherUser);

    // Act & Assert
    assertThatThrownBy(() -> reflectionService.getReflectionById(privateReflection.getId()))
        .isInstanceOf(UnauthorizedReflectionAccessException.class);

    verify(reflectionRepository).findById(privateReflection.getId());
    verify(userService).getCurrentUser();
  }

  @Test
  void getReflectionById_whenPrivateReflection_asAdmin_shouldReturnReflection() {
    // Arrange
    when(reflectionRepository.findById(privateReflection.getId())).thenReturn(
        Optional.of(privateReflection));
    when(userService.getCurrentUser()).thenReturn(adminUser);

    // Act
    ReflectionResponse result = reflectionService.getReflectionById(privateReflection.getId());

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(privateReflection.getId());

    verify(reflectionRepository).findById(privateReflection.getId());
    verify(userService).getCurrentUser();
  }

  @Test
  void getReflectionById_whenReflectionNotFound_shouldThrowException() {
    // Arrange
    UUID nonExistentId = UUID.randomUUID();
    when(reflectionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> reflectionService.getReflectionById(nonExistentId))
        .isInstanceOf(ReflectionNotFoundException.class);

    verify(reflectionRepository).findById(nonExistentId);
  }

  @Test
  void updateReflection_asAuthor_shouldReturnUpdatedReflection() {
    // Arrange
    when(reflectionRepository.findById(validId)).thenReturn(Optional.of(publicReflection));
    when(userService.getCurrentUser()).thenReturn(currentUser);

    Reflection updatedReflection = Reflection.builder()
        .id(validId)
        .title(updateRequest.getTitle())
        .content(updateRequest.getContent())
        .author(currentUser)
        .visibility(updateRequest.getVisibility())
        .build();

    when(reflectionRepository.save(any(Reflection.class))).thenReturn(updatedReflection);

    // Act
    ReflectionResponse result = reflectionService.updateReflection(validId, updateRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(updateRequest.getTitle());
    assertThat(result.getContent()).isEqualTo(updateRequest.getContent());

    verify(reflectionRepository).findById(validId);
    verify(userService).getCurrentUser();
    verify(reflectionRepository).save(any(Reflection.class));
  }

  @Test
  void updateReflection_asAdmin_shouldReturnUpdatedReflection() {
    // Arrange
    Reflection otherUserReflection = Reflection.builder()
        .id(validId)
        .title("Other User's Reflection")
        .content("This is another user's reflection")
        .author(otherUser)
        .visibility(VisibilityType.PUBLIC)
        .build();

    when(reflectionRepository.findById(validId)).thenReturn(Optional.of(otherUserReflection));
    when(userService.getCurrentUser()).thenReturn(adminUser);

    Reflection updatedReflection = Reflection.builder()
        .id(validId)
        .title(updateRequest.getTitle())
        .content(updateRequest.getContent())
        .author(otherUser)
        .visibility(updateRequest.getVisibility())
        .build();

    when(reflectionRepository.save(any(Reflection.class))).thenReturn(updatedReflection);

    // Act
    ReflectionResponse result = reflectionService.updateReflection(validId, updateRequest);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(updateRequest.getTitle());
    assertThat(result.getAuthorId()).isEqualTo(otherUser.getId());

    verify(reflectionRepository).findById(validId);
    verify(userService).getCurrentUser();
    verify(reflectionRepository).save(any(Reflection.class));
  }

  @Test
  void updateReflection_asOtherUser_shouldThrowException() {
    // Arrange
    when(reflectionRepository.findById(validId)).thenReturn(Optional.of(publicReflection));
    when(userService.getCurrentUser()).thenReturn(otherUser);

    // Act & Assert
    assertThatThrownBy(() -> reflectionService.updateReflection(validId, updateRequest))
        .isInstanceOf(UnauthorizedReflectionAccessException.class);

    verify(reflectionRepository).findById(validId);
    verify(userService).getCurrentUser();
  }

  @Test
  void deleteReflection_asAuthor_shouldDeleteReflection() {
    // Arrange
    when(reflectionRepository.findById(validId)).thenReturn(Optional.of(publicReflection));
    when(userService.getCurrentUser()).thenReturn(currentUser);

    // Act
    reflectionService.deleteReflection(validId);

    // Assert
    verify(reflectionRepository).findById(validId);
    verify(userService).getCurrentUser();
    verify(reflectionRepository).delete(publicReflection);
  }

  @Test
  void deleteReflection_asAdmin_shouldDeleteReflection() {
    // Arrange
    Reflection otherUserReflection = Reflection.builder()
        .id(validId)
        .title("Other User's Reflection")
        .content("This is another user's reflection")
        .author(otherUser)
        .visibility(VisibilityType.PUBLIC)
        .build();

    when(reflectionRepository.findById(validId)).thenReturn(Optional.of(otherUserReflection));
    when(userService.getCurrentUser()).thenReturn(adminUser);

    // Act
    reflectionService.deleteReflection(validId);

    // Assert
    verify(reflectionRepository).findById(validId);
    verify(userService).getCurrentUser();
    verify(reflectionRepository).delete(otherUserReflection);
  }

  @Test
  void deleteReflection_asOtherUser_shouldThrowException() {
    // Arrange
    when(reflectionRepository.findById(validId)).thenReturn(Optional.of(publicReflection));
    when(userService.getCurrentUser()).thenReturn(otherUser);

    // Act & Assert
    assertThatThrownBy(() -> reflectionService.deleteReflection(validId))
        .isInstanceOf(UnauthorizedReflectionAccessException.class);

    verify(reflectionRepository).findById(validId);
    verify(userService).getCurrentUser();
  }

  @Test
  void getAccessibleReflections_shouldReturnAccessibleReflections() {
    // Arrange
    List<Reflection> accessibleReflections = Arrays.asList(publicReflection, privateReflection,
        householdReflection);

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(reflectionRepository.findReflectionsAccessibleToUser(currentUser.getId()))
        .thenReturn(accessibleReflections);

    // Act
    List<ReflectionResponse> result = reflectionService.getAccessibleReflections();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(3);

    verify(userService).getCurrentUser();
    verify(reflectionRepository).findReflectionsAccessibleToUser(currentUser.getId());
  }

  @Test
  void getCurrentUserReflections_shouldReturnUserReflections() {
    // Arrange
    List<Reflection> userReflections =
        Arrays.asList(publicReflection, privateReflection, householdReflection);

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(reflectionRepository.findByAuthorId(currentUser.getId())).thenReturn(userReflections);

    // Act
    List<ReflectionResponse> result = reflectionService.getCurrentUserReflections();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(3);

    verify(userService).getCurrentUser();
    verify(reflectionRepository).findByAuthorId(currentUser.getId());
  }

  @Test
  void getPublicReflections_shouldReturnPublicReflections() {
    // Arrange
    List<Reflection> publicReflections = Collections.singletonList(publicReflection);

    when(reflectionRepository.findByVisibility(VisibilityType.PUBLIC)).thenReturn(
        publicReflections);

    // Act
    List<ReflectionResponse> result = reflectionService.getPublicReflections();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);

    verify(reflectionRepository).findByVisibility(VisibilityType.PUBLIC);
  }

  @Test
  void getHouseholdReflections_withActiveHousehold_shouldReturnHouseholdReflections() {
    // Arrange
    List<Reflection> householdReflections = Collections.singletonList(householdReflection);

    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(reflectionRepository.findByHouseholdId(household.getId())).thenReturn(
        householdReflections);

    // Act
    List<ReflectionResponse> result = reflectionService.getHouseholdReflections();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);

    verify(userService).getCurrentUser();
    verify(reflectionRepository).findByHouseholdId(household.getId());
  }

  @Test
  void getHouseholdReflections_withNoActiveHousehold_shouldThrowException() {
    // Arrange
    User userWithoutHousehold = User.builder()
        .id(UUID.randomUUID())
        .email("nohousehold@example.com")
        .firstName("No")
        .lastName("Household")
        .build();

    when(userService.getCurrentUser()).thenReturn(userWithoutHousehold);

    // Act & Assert
    assertThatThrownBy(() -> reflectionService.getHouseholdReflections())
        .isInstanceOf(HouseholdNotFoundException.class);

    verify(userService).getCurrentUser();
  }

  @Test
  void getAllReflections_asAdmin_shouldReturnAllReflections() {
    // Arrange
    List<Reflection> allReflections =
        Arrays.asList(publicReflection, privateReflection, householdReflection);

    when(userService.getCurrentUser()).thenReturn(adminUser);
    when(reflectionRepository.findAll()).thenReturn(allReflections);

    // Act
    List<ReflectionResponse> result = reflectionService.getAllReflections();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(3);

    verify(userService).getCurrentUser();
    verify(reflectionRepository).findAll();
  }

  @Test
  void getAllReflections_asRegularUser_shouldThrowException() {
    // Arrange
    when(userService.getCurrentUser()).thenReturn(currentUser);

    // Act & Assert
    assertThatThrownBy(() -> reflectionService.getAllReflections())
        .isInstanceOf(UnauthorizedReflectionAccessException.class);

    verify(userService).getCurrentUser();
  }
}