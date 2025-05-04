package stud.ntnu.krisefikser.email.dto;

// Using record for simplicity (requires Java 16+)
public record EmailRequestDto(
    String toEmail,
    String name,
    String verificationLink
) {} 