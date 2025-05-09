package stud.ntnu.krisefikser.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.auth.dto.AdminInviteRequest;
import stud.ntnu.krisefikser.auth.dto.CompletePasswordResetRequest;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.PasswordResetResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RefreshResponse;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterResponse;
import stud.ntnu.krisefikser.auth.dto.RequestPasswordResetRequest;
import stud.ntnu.krisefikser.auth.dto.UpdatePasswordRequest;
import stud.ntnu.krisefikser.auth.dto.UpdatePasswordResponse;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.service.AuthService;
import stud.ntnu.krisefikser.auth.service.TurnstileService;
import stud.ntnu.krisefikser.config.FrontendConfig;
import stud.ntnu.krisefikser.email.service.EmailAdminService;
import stud.ntnu.krisefikser.email.service.EmailVerificationService;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.repository.UserRepository;

/**
 * REST controller for managing authentication-related operations. Provides endpoints for user
 * registration, login, token refresh, and retrieving user details.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
@Validated
public class AuthController {

  private final UserRepository userRepository;
  private final EmailVerificationService emailVerificationService;
  private final AuthService authService;
  private final TurnstileService turnstileService;
  private final EmailAdminService emailAdminService;
  private final FrontendConfig frontendConfig;

  /**
   * Registers a new user after verifying the CAPTCHA and validating the input.
   *
   * @param request The registration details including Turnstile token.
   * @return ResponseEntity containing the registration response.
   */
  @Operation(summary = "Register a new user", description =
      "Creates a new user account after CAPTCHA "
          + "verification and input validation")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully registered user", content =
      @Content(mediaType = "application/json", schema = @Schema(implementation =
          RegisterResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid registration data or CAPTCHA "
          + "verification failed", content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "500", description = "Unexpected server error", content =
      @Content(mediaType = "application/json"))
  })
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
      @Parameter(description = "Registration details including Turnstile token", required = true)
      @RequestBody RegisterRequest request) {
    RegisterResponse response = authService.registerAndSendVerificationEmail(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Registers a new admin user after verifying the CAPTCHA and validating the input. This endpoint
   * is restricted to users with administrative privileges.
   *
   * @param request The registration details including Turnstile token.
   * @return ResponseEntity containing the registration response.
   */
  @Operation(summary = "Register a new admin user",
      description = "Creates a new admin user account after CAPTCHA "
          + "verification and input validation")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully registered admin user",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = RegisterResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid registration data or CAPTCHA "
          + "verification failed", content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "403",
          description = "Insufficient permissions to create admin account",
          content = @Content(mediaType = "application/json")),
      @ApiResponse(responseCode = "500", description = "Unexpected server error",
          content = @Content(mediaType = "application/json"))
  })
  @PostMapping("/register/admin")
  public ResponseEntity<RegisterResponse> registerAdmin(
      @Parameter(description = "Registration details including Turnstile token", required = true)
      @RequestBody RegisterRequest request) {
    RegisterResponse response = authService.registerAdmin(request);
    return ResponseEntity.ok(response);
  }


  /**
   * Verifies a user's email address using a token.
   *
   * @param token The verification token.
   * @return ResponseEntity containing the status of the verification operation.
   */
  @PostMapping("/verify-email")
  @Operation(summary = "Verify email address",
      description = "Verifies user's email address using a token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Email verified successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid or expired verification token")
  })
  public ResponseEntity<String> verifyEmail(@RequestParam String token) {
    boolean verified = authService.verifyEmail(token);
    if (verified) {
      return ResponseEntity.ok("Email verified successfully.");
    }
    return ResponseEntity.badRequest().body("Invalid or expired verification token.");
  }


  /**
   * Authenticates a user and returns access tokens.
   *
   * @param request The login credentials.
   * @return ResponseEntity containing the access tokens.
   */
  @Operation(summary = "Login user", description = "Authenticates a user and returns access tokens")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully authenticated", content =
      @Content(mediaType = "application/json", schema = @Schema(implementation =
          LoginResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials"),
      @ApiResponse(responseCode = "423", description = "Account is locked")
  })
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Parameter(description = "Login credentials") @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Refreshes the access token using the provided refresh token.
   *
   * @param refreshToken The refresh token used to obtain a new access token.
   * @return ResponseEntity containing the new access token.
   */
  @Operation(summary = "Refresh token", description = "Generates new access token using refresh "
      + "token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully refreshed token", content =
      @Content(mediaType = "application/json", schema = @Schema(implementation =
          RefreshResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid refresh token")
  })
  @PostMapping("/refresh")
  public ResponseEntity<RefreshResponse> refresh(
      @Parameter(description = "Refresh token") @RequestBody RefreshRequest refreshToken) {
    RefreshResponse response = authService.refresh(refreshToken);
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieves the currently authenticated user's details.
   *
   * @return ResponseEntity containing the user's details.
   */
  @Operation(summary = "Get current user", description = "Retrieves the currently authenticated "
      + "user's details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved user details",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              UserResponse.class))),
      @ApiResponse(responseCode = "401", description = "Not authenticated")
  })
  @GetMapping("/me")
  public ResponseEntity<UserResponse> me() {
    UserResponse response = authService.me();
    return ResponseEntity.ok(response);
  }

  /**
   * Updates the password of the currently authenticated user.
   *
   * @param updatePasswordRequest The request containing the new password
   * @return ResponseEntity containing the status of the password update operation
   */
  @Operation(summary = "Update password", description = "Updates the password of the currently "
      + "authenticated user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Password successfully updated", content =
      @Content(mediaType = "application/json", schema = @Schema(implementation =
          UpdatePasswordResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid current password or not "
          + "authenticated")
  })
  @PostMapping("/update-password")
  public ResponseEntity<UpdatePasswordResponse> updatePassword(
      @RequestBody UpdatePasswordRequest updatePasswordRequest
  ) {
    UpdatePasswordResponse response = authService.updatePassword(updatePasswordRequest);
    return ResponseEntity.ok(response);
  }

  /**
   * Requests a password reset by generating a reset token and sending it to the user's email.
   *
   * @param request The request containing the user's email
   * @return ResponseEntity containing the status of the reset request operation
   */
  @Operation(summary = "Request password reset", description = "Generates a password reset token "
      + "and sends it to the user's email")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Password reset request successful",
          content =
          @Content(mediaType = "application/json", schema = @Schema(implementation =
              PasswordResetResponse.class))),
      @ApiResponse(responseCode = "404", description = "User not found"),
      @ApiResponse(responseCode = "500", description = "Error sending email")
  })
  @PostMapping("/request-password-reset")
  public ResponseEntity<PasswordResetResponse> requestPasswordReset(
      @RequestBody RequestPasswordResetRequest request
  ) {
    PasswordResetResponse response = authService.requestPasswordReset(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Completes the password reset process by validating the token and setting a new password.
   *
   * @param request The request containing the email, token, and new password
   * @return ResponseEntity containing the status of the password reset operation
   */
  @Operation(summary = "Complete password reset", description = "Verifies the reset token and "
      + "updates the user's password")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Password successfully reset", content =
      @Content(mediaType = "application/json", schema = @Schema(implementation =
          PasswordResetResponse.class))),
      @ApiResponse(responseCode = "400", description = "Missing required fields"),
      @ApiResponse(responseCode = "401", description = "Invalid or expired token")
  })
  @PostMapping("/complete-password-reset")
  public ResponseEntity<PasswordResetResponse> completePasswordReset(
      @RequestBody CompletePasswordResetRequest request
  ) {
    PasswordResetResponse response = authService.completePasswordReset(request);
    return ResponseEntity.ok(response);
  }

  /**
   * Sends an admin invitation email to the specified email address.
   *
   * @param request The request containing the email address to send the invitation to
   * @return ResponseEntity containing the status of the invitation operation
   */
  @Operation(summary = "Send admin invitation",
      description = "Sends an admin invitation email to the specified address")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Admin invitation sent successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid email address"),
      @ApiResponse(responseCode = "403",
          description = "Insufficient permissions to send admin invitation"),
      @ApiResponse(responseCode = "500", description = "Error sending invitation email")
  })
  @PostMapping("/invite/admin")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<String> inviteAdmin(
      @Parameter(description = "Email address to send the admin invitation to", required = true)
      @RequestBody AdminInviteRequest request
  ) {
    // Generate a unique token for the admin invitation
    String inviteToken = authService.generateAdminInviteToken(request.getEmail());
    String inviteLink = frontendConfig.getUrl() + "/admin/registrer?token=" + inviteToken;

    return emailAdminService.sendAdminInvitation(request.getEmail(), inviteLink);
  }

  /**
   * Verifies an admin invitation token and returns the associated email address.
   *
   * @param token The invitation token to verify
   * @return ResponseEntity containing the email address if token is valid
   */
  @GetMapping("/verify-admin-invite")
  @PermitAll
  public ResponseEntity<Map<String, String>> verifyAdminInviteToken(@RequestParam String token) {
    try {
      String email = authService.verifyAdminInviteToken(token);
      return ResponseEntity.ok(Map.of("email", email));
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PostMapping("/verify-admin-login")
  public ResponseEntity<LoginResponse> verifyAdminLogin(@RequestParam String token) {
    try {
      LoginResponse response = authService.verifyAdminLogin(token);
      return ResponseEntity.ok(response);
    } catch (InvalidTokenException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Allows a superadmin to request a password reset for an admin user.
   *
   * @param request The request containing the admin's email address
   * @return ResponseEntity containing the status of the reset request operation
   */
  @Operation(
    summary = "Request password reset for admin user",
    description = "Generates a password reset token and sends it to the admin user's email. Only accessible by superadmins."
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "Password reset link sent successfully",
      content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))
    ),
    @ApiResponse(
      responseCode = "403",
      description = "Access denied - only superadmins can request admin password resets"
    ),
    @ApiResponse(
      responseCode = "404",
      description = "Admin user not found"
    )
  })
  @PostMapping("/admin/reset-password-link")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<PasswordResetResponse> requestAdminPasswordReset(
    @Validated @RequestBody RequestPasswordResetRequest request
  ) {
    return ResponseEntity.ok(authService.requestAdminPasswordReset(request));
  }

  /**
   * Exception handler for LockedException to return a 423 (Locked) status code
   *
   * @param ex The LockedException that was thrown
   * @return ResponseEntity with error details and 423 status code
   */
  @ExceptionHandler(LockedException.class)
  public ResponseEntity<Map<String, String>> handleLockedException(LockedException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("message", ex.getMessage());
    response.put("error", "Account Locked");
    return new ResponseEntity<>(response, HttpStatus.LOCKED); // HTTP 423
  }
}
