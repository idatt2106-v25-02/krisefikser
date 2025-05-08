package stud.ntnu.krisefikser.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for requesting an admin invitation.
 *
 * <p>This class encapsulates the data required to send an admin invitation, which is
 * the email address where the invitation will be sent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminInviteRequest {
    String email;
} 