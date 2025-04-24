package stud.ntnu.krisefikser.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * REST controller for managing users in the system.
 * Provides endpoints for user management operations.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User management APIs")
public class UserController {
  /**
   * The user service for handling user-related operations.
   */
  private final UserService userService;
}
