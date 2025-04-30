package stud.ntnu.krisefikser.article.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

  private Long id;
  private String title;
  private String text;
  private LocalDateTime createdAt;
  private String imageUrl;
}