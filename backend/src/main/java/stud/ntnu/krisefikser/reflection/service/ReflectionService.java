package stud.ntnu.krisefikser.reflection.service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

/**
 * Service for managing reflections.
 *
 * <p>
 * Provides business logic for creating, retrieving, updating, and deleting
 * reflections,
 * with appropriate access controls based on visibility settings and user roles.
 * </p>
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class ReflectionService {

  private final ReflectionRepository reflectionRepository;
  private final UserService userService;
  private final HouseholdService householdService;
  private final EventService eventService;

  /**
   * Creates a new reflection.
   *
   * @param request the data for creating the reflection
   * @return the created reflection
   */
  @Transactional
  public ReflectionResponse createReflection(CreateReflectionRequest request) {
    User currentUser = userService.getCurrentUser();
    Reflection reflection = Reflection.builder()
        .title(request.getTitle())
        .content(request.getContent())
        .author(currentUser)
        .visibility(request.getVisibility())
        .event(eventService.getEventEntityById(request.getEventId()))
        .build();

    // Set household if visibility is HOUSEHOLD
    if (request.getVisibility() == VisibilityType.HOUSEHOLD) {
      Household household;
      if (request.getHouseholdId() != null) {
        household = householdService.getHouseholdById(request.getHouseholdId());
      } else if (currentUser.getActiveHousehold() != null) {
        household = currentUser.getActiveHousehold();
      } else {
        throw new HouseholdNotFoundException();
      }
      reflection.setHousehold(household);
    }

    Reflection savedReflection = reflectionRepository.save(reflection);
    return toResponse(savedReflection);
  }

  /**
   * Retrieves a reflection by its ID.
   *
   * <p>
   * Access is restricted based on the reflection's visibility settings and the
   * current user's permissions.
   * </p>
   *
   * @param id the ID of the reflection to retrieve
   * @return the requested reflection
   */
  public ReflectionResponse getReflectionById(UUID id) {
    Reflection reflection = findReflectionById(id);
    User currentUser = userService.getCurrentUser();

    // Check if user has access
    if (!hasAccessToReflection(reflection, currentUser)) {
      throw new UnauthorizedReflectionAccessException(
          "You don't have permission to view this reflection");
    }

    return toResponse(reflection);
  }

  /**
   * Updates an existing reflection.
   *
   * <p>
   * Only the author or an admin can update a reflection.
   * </p>
   *
   * @param id      the ID of the reflection to update
   * @param request the updated reflection data
   * @return the updated reflection
   */
  @Transactional
  public ReflectionResponse updateReflection(UUID id, UpdateReflectionRequest request) {
    Reflection reflection = findReflectionById(id);
    User currentUser = userService.getCurrentUser();

    // Check if user is author or admin
    if (!isAuthorOrAdmin(reflection, currentUser)) {
      throw new UnauthorizedReflectionAccessException(
          "You don't have permission to update this reflection");
    }

    reflection.setTitle(request.getTitle());
    reflection.setContent(request.getContent());
    reflection.setVisibility(request.getVisibility());
    reflection.setEvent(eventService.getEventEntityById(request.getEventId()));

    // Update household if visibility is HOUSEHOLD
    if (request.getVisibility() == VisibilityType.HOUSEHOLD) {
      Household household;
      if (request.getHouseholdId() != null) {
        household = householdService.getHouseholdById(request.getHouseholdId());
      } else if (currentUser.getActiveHousehold() != null) {
        household = currentUser.getActiveHousehold();
      } else {
        throw new HouseholdNotFoundException();
      }
      reflection.setHousehold(household);
    } else {
      reflection.setHousehold(null);
    }

    Reflection updatedReflection = reflectionRepository.save(reflection);
    return toResponse(updatedReflection);
  }

  /**
   * Deletes a reflection.
   *
   * <p>
   * Only the author or an admin can delete a reflection.
   * </p>
   *
   * @param id the ID of the reflection to delete
   */
  @Transactional
  public void deleteReflection(UUID id) {
    Reflection reflection = findReflectionById(id);
    User currentUser = userService.getCurrentUser();

    // Check if user is author or admin
    if (!isAuthorOrAdmin(reflection, currentUser)) {
      throw new UnauthorizedReflectionAccessException(
          "You don't have permission to delete this reflection");
    }

    reflectionRepository.delete(reflection);
  }

  /**
   * Retrieves all reflections accessible to the current user.
   *
   * <p>
   * This includes all public reflections, the user's own private reflections,
   * and household reflections for households the user belongs to.
   * </p>
   *
   * @return a list of accessible reflections
   */
  public List<ReflectionResponse> getAccessibleReflections() {
    User currentUser = userService.getCurrentUser();
    List<Reflection> reflections =
        reflectionRepository.findReflectionsAccessibleToUser(currentUser.getId());
    return reflections.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves reflections authored by the current user.
   *
   * @return a list of the user's reflections
   */
  public List<ReflectionResponse> getCurrentUserReflections() {
    User currentUser = userService.getCurrentUser();
    List<Reflection> reflections = reflectionRepository.findByAuthorId(currentUser.getId());
    return reflections.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves public reflections.
   *
   * @return a list of public reflections
   */
  public List<ReflectionResponse> getPublicReflections() {
    List<Reflection> reflections = reflectionRepository.findByVisibility(VisibilityType.PUBLIC);
    return reflections.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves household reflections for the current user's active household.
   *
   * @return a list of reflections for the current user's household
   */
  public List<ReflectionResponse> getHouseholdReflections() {
    User currentUser = userService.getCurrentUser();
    if (currentUser.getActiveHousehold() == null) {
      throw new HouseholdNotFoundException();
    }

    List<Reflection> reflections =
        reflectionRepository.findByHouseholdId(currentUser.getActiveHousehold().getId());
    return reflections.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Retrieves reflections for a specific event, accessible to the current user.
   *
   * @param eventId the ID of the event
   * @return a list of accessible reflections for the given event
   */
  public List<ReflectionResponse> getReflectionsByEventId(Long eventId) {
    User currentUser = userService.getCurrentUser();
    List<Reflection> reflectionsForEvent = reflectionRepository.findByEventId(
        eventId); // Assumes this method will be added to repository

    return reflectionsForEvent.stream()
        .filter(reflection -> hasAccessToReflection(reflection, currentUser))
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Admin method to retrieve all reflections in the system.
   *
   * @return a list of all reflections
   */
  public List<ReflectionResponse> getAllReflections() {
    User currentUser = userService.getCurrentUser();
    boolean isAdmin = currentUser.getRoles().stream()
        .anyMatch(
            role -> role.getName() == RoleType.ADMIN || role.getName() == RoleType.SUPER_ADMIN);

    if (!isAdmin) {
      throw new UnauthorizedReflectionAccessException("Only admins can view all reflections");
    }

    List<Reflection> reflections = reflectionRepository.findAll();
    return reflections.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
  }

  /**
   * Finds a reflection by its ID.
   *
   * @param id the ID of the reflection to find
   * @return the found reflection
   * @throws ReflectionNotFoundException if no reflection with the given ID exists
   */
  private Reflection findReflectionById(UUID id) {
    return reflectionRepository.findById(id)
        .orElseThrow(() -> new ReflectionNotFoundException(id));
  }

  /**
   * Checks if a user has access to a reflection based on visibility settings.
   *
   * @param reflection the reflection to check access for
   * @param user       the user to check access for
   * @return true if the user has access, false otherwise
   */
  private boolean hasAccessToReflection(Reflection reflection, User user) {
    // Admin can access any reflection
    boolean isAdmin = user.getRoles().stream()
        .anyMatch(
            role -> role.getName() == RoleType.ADMIN || role.getName() == RoleType.SUPER_ADMIN);
    if (isAdmin) {
      return true;
    }

    // Author can access their own reflection
    if (reflection.getAuthor().getId().equals(user.getId())) {
      return true;
    }

    // Check visibility
    switch (reflection.getVisibility()) {
      case PUBLIC:
        return true;
      case HOUSEHOLD:
        if (reflection.getHousehold() == null) {
          return false;
        }
        // Check if user is in the same household
        if (user.getActiveHousehold() != null) {
          return user.getActiveHousehold().getId().equals(reflection.getHousehold().getId());
        }
        return false;
      case PRIVATE:
        return false;
      default:
        return false;
    }
  }

  /**
   * Checks if a user is the author of a reflection or an admin.
   *
   * @param reflection the reflection to check
   * @param user       the user to check
   * @return true if the user is the author or an admin, false otherwise
   */
  private boolean isAuthorOrAdmin(Reflection reflection, User user) {
    boolean isAdmin = user.getRoles().stream()
        .anyMatch(
            role -> role.getName() == RoleType.ADMIN || role.getName() == RoleType.SUPER_ADMIN);

    return isAdmin || reflection.getAuthor().getId().equals(user.getId());
  }

  /**
   * Converts a Reflection entity to a ReflectionResponse DTO.
   *
   * @param reflection the reflection entity to convert
   * @return the response DTO
   */
  private ReflectionResponse toResponse(Reflection reflection) {
    return ReflectionResponse.builder()
        .id(reflection.getId())
        .title(reflection.getTitle())
        .content(reflection.getContent())
        .authorId(reflection.getAuthor().getId())
        .authorName(getAuthorName(reflection.getAuthor()))
        .visibility(reflection.getVisibility())
        .householdId(reflection.getHousehold() != null ? reflection.getHousehold().getId() : null)
        .householdName(
            reflection.getHousehold() != null ? reflection.getHousehold().getName() : null)
        .eventId(reflection.getEvent().getId())
        .createdAt(reflection.getCreatedAt())
        .updatedAt(reflection.getUpdatedAt())
        .build();
  }

  /**
   * Gets the display name of a user.
   *
   * @param user the user
   * @return the display name (first name + last name, or email if names are not
   * available)
   */
  private String getAuthorName(User user) {
    if (user.getFirstName() != null && user.getLastName() != null) {
      return user.getFirstName() + " " + user.getLastName();
    } else if (user.getFirstName() != null) {
      return user.getFirstName();
    } else {
      return user.getEmail();
    }
  }
}