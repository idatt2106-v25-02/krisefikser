package stud.ntnu.krisefikser.email.service;

import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import java.time.Year;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import stud.ntnu.krisefikser.user.entity.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
  private final JavaMailSender mailSender;
  private final TemplateEngine thymeleaf;

  public void sendWelcomeEmail(User user, String token) {
    Context ctx = new Context();
    // Use firstName if available, otherwise use email
    String name = user.getFirstName() != null && !user.getFirstName().isEmpty()
        ? user.getFirstName() : user.getEmail();
    ctx.setVariable("name", name);

    // Add the required variables for the email template
    String verificationLink = "https://krisefikser.no/verify?token=" + token;
    ctx.setVariable("verificationLink", verificationLink);
    ctx.setVariable("expiryHours", 24); // Link expires in 24 hours
    ctx.setVariable("currentYear", Year.now().getValue());

    String body = thymeleaf.process("welcome-email", ctx);

    MimeMessagePreparator msg =
        mime -> {
          mime.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
          mime.setFrom("noreply@krisefikser.app");
          mime.setSubject("Welcome to Krisefikser!");
          mime.setContent(body, "text/html; charset=utf-8");
        };

    mailSender.send(msg);
    log.info("Sent welcome email to {}", user.getEmail());
  }

  public void sendPasswordResetEmail(User user, String token) {
    Context ctx = new Context();
    // Use firstName if available, otherwise use email
    String name = user.getFirstName() != null && !user.getFirstName().isEmpty()
        ? user.getFirstName() : user.getEmail();
    ctx.setVariable("name", name);

    String resetLink = "https://krisefikser.no/reset-password?token=" + token;
    ctx.setVariable("resetLink", resetLink);
    ctx.setVariable("expiryHours", 1); // Link expires in 1 hour
    ctx.setVariable("currentYear", Year.now().getValue());

    String body = thymeleaf.process("reset-password-email", ctx);

    MimeMessagePreparator msg =
        mime -> {
          mime.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
          mime.setFrom("noreply@krisefikser.app");
          mime.setSubject("Reset Your Krisefikser Password");
          mime.setContent(body, "text/html; charset=utf-8");
        };

    mailSender.send(msg);
    log.info("Sent password reset email to {}", user.getEmail());
  }
}