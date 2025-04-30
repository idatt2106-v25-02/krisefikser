package stud.ntnu.krisefikser.article.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.article.dto.ArticleRequest;
import stud.ntnu.krisefikser.article.dto.ArticleResponse;
import stud.ntnu.krisefikser.article.entity.Article;
import stud.ntnu.krisefikser.article.exception.ArticleNotFoundException;
import stud.ntnu.krisefikser.article.repository.ArticleRepository;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

  @Mock
  private ArticleRepository articleRepository;

  @InjectMocks
  private ArticleService articleService;

  private Article article;
  private ArticleRequest articleRequest;
  private ArticleResponse articleResponse;
  private final LocalDateTime now = LocalDateTime.now();

  @BeforeEach
  void setUp() {
    article = Article.builder()
        .id(1L)
        .title("Test Article")
        .text("Test Content")
        .createdAt(now)
        .imageUrl("http://example.com/image.jpg")
        .build();

    articleRequest = ArticleRequest.builder().
        title("Test Article")
        .text("Test Content")
        .imageUrl("http://example.com/image.jpg")
        .build();

    articleResponse = ArticleResponse.builder()
        .id(1L)
        .title("Test Article")
        .text("Test Content")
        .createdAt(now)
        .imageUrl("http://example.com/image.jpg")
        .build();
  }

  @Test
  void getAllArticles_ShouldReturnListOfArticles() {
    when(articleRepository.findAll()).thenReturn(List.of(article));

    List<ArticleResponse> result = articleService.getAllArticles();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo(article.getTitle());
    verify(articleRepository).findAll();
  }

  @Test
  void getArticleById_WhenArticleExists_ShouldReturnArticle() {
    when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

    ArticleResponse result = articleService.getArticleById(1L);

    assertThat(result.getId()).isEqualTo(article.getId());
    assertThat(result.getTitle()).isEqualTo(article.getTitle());
    verify(articleRepository).findById(1L);
  }

  @Test
  void getArticleById_WhenArticleDoesNotExist_ShouldThrowException() {
    when(articleRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleService.getArticleById(1L))
        .isInstanceOf(ArticleNotFoundException.class)
        .hasMessageContaining("Article not found with id: 1");
  }

  @Test
  void createArticle_ShouldReturnCreatedArticle() {
    when(articleRepository.save(any(Article.class))).thenReturn(article);

    ArticleResponse result = articleService.createArticle(articleRequest);

    assertThat(result.getTitle()).isEqualTo(articleResponse.getTitle());
    assertThat(result.getText()).isEqualTo(articleResponse.getText());
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void updateArticle_WhenArticleExists_ShouldReturnUpdatedArticle() {
    when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
    when(articleRepository.save(any(Article.class))).thenReturn(article);

    ArticleRequest updatedDTO = ArticleRequest.builder()
        .title("Updated Title")
        .text("Updated Content")
        .imageUrl("http://example.com/updated.jpg")
        .build();

    ArticleResponse result = articleService.updateArticle(1L, updatedDTO);

    assertThat(result.getTitle()).isEqualTo(article.getTitle());
    verify(articleRepository).findById(1L);
    verify(articleRepository).save(any(Article.class));
  }

  @Test
  void updateArticle_WhenArticleDoesNotExist_ShouldThrowException() {
    when(articleRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> articleService.updateArticle(1L, articleRequest))
        .isInstanceOf(ArticleNotFoundException.class)
        .hasMessageContaining("Article not found with id: 1");
  }

  @Test
  void deleteArticle_WhenArticleExists_ShouldDeleteArticle() {
    when(articleRepository.existsById(1L)).thenReturn(true);

    articleService.deleteArticle(1L);

    verify(articleRepository).deleteById(1L);
  }

  @Test
  void deleteArticle_WhenArticleDoesNotExist_ShouldThrowException() {
    when(articleRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> articleService.deleteArticle(1L))
        .isInstanceOf(ArticleNotFoundException.class)
        .hasMessageContaining("Article not found with id: 1");
  }
}