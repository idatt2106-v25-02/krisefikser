package stud.ntnu.krisefikser.article.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for representing an article response. This class is used to transfer
 * data between the server and client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

  @NotNull
  private Long id;
  @NotNull
  private String title;
  @NotNull
  private String text;
  @NotNull
  private LocalDateTime createdAt;
  @NotNull
  private String imageUrl;
}