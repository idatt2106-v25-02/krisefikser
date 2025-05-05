package stud.ntnu.krisefikser.article.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) for creating or updating an article. This class is used to transfer
 * data between the client and server.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {

  @NotBlank(message = "Title is required")
  private String title;
  @NotBlank(message = "Text is required")
  private String text;
  @NotBlank(message = "Image URL is required")
  private String imageUrl;
}