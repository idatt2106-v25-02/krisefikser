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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RefreshResponse;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterResponse;
import stud.ntnu.krisefikser.auth.exception.TurnstileVerificationException;
import stud.ntnu.krisefikser.auth.service.AuthService;
import stud.ntnu.krisefikser.auth.service.TurnstileService;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for managing authentication-related operations. Provides
 * endpoints for user
 * registration, login, token refresh, and retrieving user details.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
@Validated
public class AuthController {

    private final AuthService authService;
    private final TurnstileService turnstileService;

    /**
     * Registers a new user after verifying the CAPTCHA and validating the input.
     *
     * @param request The registration details including Turnstile token.
     * @return ResponseEntity containing the registration response.
     */
    @Operation(summary = "Register a new user", description = "Creates a new user account after CAPTCHA "
            + "verification and input validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully registered user", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid registration data or CAPTCHA "
                    + "verification failed", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Parameter(description = "Registration details including Turnstile token", required = true) @RequestBody RegisterRequest request) {
        boolean isHuman = turnstileService.verify(request.getTurnstileToken());
        if (!isHuman) {
            throw new TurnstileVerificationException(); // Will trigger 400 with message if
            // @RestControllerAdvice is used
        }
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticates a user and returns access tokens.
     *
     * @param request The login credentials.
     * @return ResponseEntity containing the access tokens.
     */
    @Operation(summary = "Login user", description = "Authenticates a user and returns access tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
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
            @ApiResponse(responseCode = "200", description = "Successfully refreshed token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefreshResponse.class))),
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
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        UserResponse response = authService.me();
        return ResponseEntity.ok(response);
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
