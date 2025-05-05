package stud.ntnu.krisefikser.email.dto;

/**
 * Data Transfer Object (DTO) for email requests. This class is used to encapsulate the data
 * required to send an email.
 *
 * @param toEmail          The recipient's email address.
 * @param name             The name of the recipient.
 * @param verificationLink The verification link to be included in the email.
 */
public record EmailRequestDto(
    String toEmail,
    String name,
    String verificationLink
) {

}