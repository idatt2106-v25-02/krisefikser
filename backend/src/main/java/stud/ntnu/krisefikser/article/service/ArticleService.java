package stud.ntnu.krisefikser.article.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.article.data.ArticleDTO;
import stud.ntnu.krisefikser.article.entity.Article;
import stud.ntnu.krisefikser.article.exception.ArticleNotFoundException;
import stud.ntnu.krisefikser.article.repository.ArticleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article not found with id: " + id));
        return convertToDTO(article);
    }

    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        Article article = Article.builder()
                .title(articleDTO.getTitle())
                .text(articleDTO.getText())
                .imageUrl(articleDTO.getImageUrl())
                .createdAt(LocalDateTime.now())
                .build();

        Article savedArticle = articleRepository.save(article);
        return convertToDTO(savedArticle);
    }

    public ArticleDTO updateArticle(Long id, ArticleDTO articleDTO) {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article not found with id: " + id));

        existingArticle.setTitle(articleDTO.getTitle());
        existingArticle.setText(articleDTO.getText());
        existingArticle.setImageUrl(articleDTO.getImageUrl());

        Article updatedArticle = articleRepository.save(existingArticle);
        return convertToDTO(updatedArticle);
    }

    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ArticleNotFoundException("Article not found with id: " + id);
        }
        articleRepository.deleteById(id);
    }

    private ArticleDTO convertToDTO(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .text(article.getText())
                .createdAt(article.getCreatedAt())
                .imageUrl(article.getImageUrl())
                .build();
    }
}