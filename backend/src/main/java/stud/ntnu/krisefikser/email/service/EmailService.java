package stud.ntnu.krisefikser.email.service;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import java.time.Year;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.user.entity.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
  private final JavaMailSender mailSender;
  private final TemplateEngine thymeleaf;
  private final TokenService tokenService;

  @Value("${app.frontend-url:https://krisefikser.no}")
  private String frontendUrl;

  @Value("${app.email.from:noreply@krisefikser.app}")
  private String emailFrom;

  public void sendWelcomeEmail(User user, UserDetails userDetails) {
    // Create verification token that expires in 24 hours
    Map<String, Object> claims = new HashMap<>();
    claims.put("purpose", "email_verification");

    String token = tokenService.generate(
        userDetails,
        new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000),
        claims
    );

    Context ctx = new Context();
    String name = user.getFirstName() != null && !user.getFirstName().isEmpty()
        ? user.getFirstName() : user.getEmail();
    ctx.setVariable("name", name);

    String verificationLink = frontendUrl + "/verify?token=" + token;
    ctx.setVariable("verificationLink", verificationLink);
    ctx.setVariable("expiryHours", 24);
    ctx.setVariable("currentYear", Year.now().getValue());

    String body = thymeleaf.process("welcome-email", ctx);

    MimeMessagePreparator msg =
        mime -> {
          mime.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
          mime.setFrom(emailFrom);
          mime.setSubject("Welcome to Krisefikser!");
          mime.setContent(body, "text/html; charset=utf-8");
        };

    mailSender.send(msg);
    log.info("Sent welcome email to {}", user.getEmail());
  }

  public void sendPasswordResetEmail(User user, UserDetails userDetails) {
    // Create password reset token that expires in 1 hour
    Map<String, Object> claims = new HashMap<>();
    claims.put("purpose", "password_reset");

    String token = tokenService.generate(
        userDetails,
        new Date(System.currentTimeMillis() + 60 * 60 * 1000),
        claims
    );

    Context ctx = new Context();
    String name = user.getFirstName() != null && !user.getFirstName().isEmpty()
        ? user.getFirstName() : user.getEmail();
    ctx.setVariable("name", name);

    String resetLink = frontendUrl + "/reset-password?token=" + token;
    ctx.setVariable("resetLink", resetLink);
    ctx.setVariable("expiryHours", 1);
    ctx.setVariable("currentYear", Year.now().getValue());

    String body = thymeleaf.process("reset-password-email", ctx);

    MimeMessagePreparator msg =
        mime -> {
          mime.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
          mime.setFrom(emailFrom);
          mime.setSubject("Reset Your Krisefikser Password");
          mime.setContent(body, "text/html; charset=utf-8");
        };

    mailSender.send(msg);
    log.info("Sent password reset email to {}", user.getEmail());
  }
}