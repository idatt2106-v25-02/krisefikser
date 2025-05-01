package stud.ntnu.krisefikser.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RefreshResponse;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterResponse;
import stud.ntnu.krisefikser.auth.service.AuthService;
import stud.ntnu.krisefikser.user.dto.UserResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
@Validated
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "Register a new user", description = "Creates a new user account in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully registered user", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class))),
      @ApiResponse(responseCode = "400", description = "Invalid registration data")
  })
  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
      @Parameter(description = "Registration details") @RequestBody RegisterRequest request) {
    RegisterResponse response = authService.register(request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Login user", description = "Authenticates a user and returns access tokens")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials")
  })
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Parameter(description = "Login credentials") @RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Refresh token", description = "Generates new access token using refresh token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully refreshed token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefreshResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid refresh token")
  })
  @PostMapping("/refresh")
  public ResponseEntity<RefreshResponse> refresh(
      @Parameter(description = "Refresh token") @RequestBody RefreshRequest refreshToken) {
    RefreshResponse response = authService.refresh(refreshToken);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get current user", description = "Retrieves the currently authenticated user's details")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved user details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
      @ApiResponse(responseCode = "401", description = "Not authenticated")
  })
  @GetMapping("/me")
  public ResponseEntity<UserResponse> me() {
    UserResponse response = authService.me();
    return ResponseEntity.ok(response);
  }
}
