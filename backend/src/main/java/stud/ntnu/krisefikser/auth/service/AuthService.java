package stud.ntnu.krisefikser.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.auth.dto.LoginRequest;
import stud.ntnu.krisefikser.auth.dto.LoginResponse;
import stud.ntnu.krisefikser.auth.dto.RefreshRequest;
import stud.ntnu.krisefikser.auth.dto.RefreshResponse;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterResponse;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.repository.RefreshTokenRepository;
import stud.ntnu.krisefikser.user.dto.CreateUser;
import stud.ntnu.krisefikser.user.dto.UserResponse;
import stud.ntnu.krisefikser.user.service.UserService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final UserService userService;
  private final CustomUserDetailsService userDetailsService;
  private final TokenService tokenService;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenRepository refreshTokenRepository;

  public RegisterResponse register(RegisterRequest registerRequest) {
    userService.createUser(new CreateUser(
        registerRequest.getEmail(),
        registerRequest.getPassword(),
        registerRequest.getFirstName(),
        registerRequest.getLastName(),
        true,
        true,
        true));

    UserDetails userDetails = userDetailsService.loadUserByUsername(registerRequest.getEmail());

    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).build());

    return new RegisterResponse(
        accessToken,
        refreshToken);
  }

  public LoginResponse login(LoginRequest loginRequest) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(),
            loginRequest.getPassword()));

    UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).build());

    return new LoginResponse(
        accessToken,
        refreshToken);
  }

  public RefreshResponse refresh(RefreshRequest refreshRequest) {
    RefreshToken existingToken = refreshTokenRepository.findByToken(
        refreshRequest.getRefreshToken()).orElseThrow(
        RefreshTokenDoesNotExistException::new);

    String email = tokenService.extractEmail(existingToken.getToken());
    if (email == null) {
      throw new InvalidTokenException();
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    String accessToken = tokenService.generateAccessToken(userDetails);
    String refreshToken = tokenService.generateRefreshToken(userDetails);

    refreshTokenRepository.delete(existingToken);
    refreshTokenRepository.save(RefreshToken.builder().token(refreshToken).build());

    return new RefreshResponse(
        accessToken,
        refreshToken);
  }

  public UserResponse me() {
    return userService.getCurrentUser().toDto();
  }

//  private String createAccessToken(UserDetails userDetails) {
//    Map<String, Object> claims = new HashMap<>();
//    claims.put("type", "access");
//    return tokenService.generate(userDetails, getAccessTokenExpiration(), claims);
//  }
//
//  private String createRefreshToken(UserDetails userDetails) {
//    Map<String, Object> claims = new HashMap<>();
//    claims.put("type", "refresh");
//    return tokenService.generate(userDetails, getRefreshTokenExpiration(), claims);
//  }
//
//  private Date getAccessTokenExpiration() {
//    return new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration());
//  }
//
//  private Date getRefreshTokenExpiration() {
//    return new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration());
//  }
}
