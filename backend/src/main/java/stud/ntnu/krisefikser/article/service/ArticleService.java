package stud.ntnu.krisefikser.article.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.article.dto.ArticleRequest;
import stud.ntnu.krisefikser.article.dto.ArticleResponse;
import stud.ntnu.krisefikser.article.entity.Article;
import stud.ntnu.krisefikser.article.exception.ArticleNotFoundException;
import stud.ntnu.krisefikser.article.repository.ArticleRepository;

@Service
@RequiredArgsConstructor
public class ArticleService {

  private final ArticleRepository articleRepository;

  public List<ArticleResponse> getAllArticles() {
    return articleRepository.findAll().stream()
        .map(this::convertToDto)
        .toList();
  }

  private ArticleResponse convertToDto(Article article) {
    return ArticleResponse.builder()
        .id(article.getId())
        .title(article.getTitle())
        .text(article.getText())
        .createdAt(article.getCreatedAt())
        .imageUrl(article.getImageUrl())
        .build();
  }

  public ArticleResponse getArticleById(Long id) {
    Article article = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found with id: " + id));
    return convertToDto(article);
  }

  public ArticleResponse createArticle(ArticleRequest articleRequest) {
    Article article = Article.builder()
        .title(articleRequest.getTitle())
        .text(articleRequest.getText())
        .imageUrl(articleRequest.getImageUrl())
        .createdAt(LocalDateTime.now())
        .build();

    Article savedArticle = articleRepository.save(article);
    return convertToDto(savedArticle);
  }

  public ArticleResponse updateArticle(Long id, ArticleRequest articleRequest) {
    Article existingArticle = articleRepository.findById(id)
        .orElseThrow(() -> new ArticleNotFoundException("Article not found with id: " + id));

    existingArticle.setTitle(articleRequest.getTitle());
    existingArticle.setText(articleRequest.getText());
    existingArticle.setImageUrl(articleRequest.getImageUrl());

    Article updatedArticle = articleRepository.save(existingArticle);
    return convertToDto(updatedArticle);
  }

  public void deleteArticle(Long id) {
    if (!articleRepository.existsById(id)) {
      throw new ArticleNotFoundException("Article not found with id: " + id);
    }
    articleRepository.deleteById(id);
  }
}