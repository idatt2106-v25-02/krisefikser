package stud.ntnu.krisefikser.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.exception.UnauthorizedAccessException;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * REST controller for managing users in the system. Provides endpoints for user management
 * operations.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management APIs")
@Validated
public class UserController {

  /**
   * The user service for handling user-related operations.
   */
  private final UserService userService;

  @Operation(summary = "Get all users", description = "Retrieves a list of all users in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved users", content = @Content(mediaType = "application/json", array = @ArraySchema(items = @Schema(implementation = UserResponse.class)))),
      @ApiResponse(responseCode = "401", description = "Not authenticated")
  })
  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllUsers() {
    List<UserResponse> users = userService.getAllUsers().stream()
        .map(User::toDto)
        .collect(Collectors.toList());
    return ResponseEntity.ok(users);
  }

  @Operation(summary = "Update user", description = "Updates an existing user's information")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated user", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid user data"),
      @ApiResponse(responseCode = "401", description = "Not authenticated"),
      @ApiResponse(responseCode = "403", description = "Not authorized to update this user"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @PutMapping("/{userId}")
  public ResponseEntity<UserResponse> updateUser(
      @Parameter(description = "User ID") @PathVariable UUID userId,
      @Parameter(description = "Updated user data") @RequestBody CreateUser userDto) {
    if (!userService.isAdminOrSelf(userId)) {
      throw new UnauthorizedAccessException("You are not authorized to update this user");
    }
    User updatedUser = userService.updateUser(userId, userDto);
    return ResponseEntity.ok(updatedUser.toDto());
  }

  @Operation(summary = "Delete user", description = "Deletes a user from the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully deleted user"),
      @ApiResponse(responseCode = "401", description = "Not authenticated"),
      @ApiResponse(responseCode = "403", description = "Not authorized to delete this user"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "User ID") @PathVariable UUID userId) {
    if (!userService.isAdminOrSelf(userId)) {
      throw new UnauthorizedAccessException("You are not authorized to delete this user");
    }
    userService.deleteUser(userId);
    return ResponseEntity.ok().build();
  }
}
