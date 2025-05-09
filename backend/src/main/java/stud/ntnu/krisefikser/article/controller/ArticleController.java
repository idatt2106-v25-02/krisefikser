package stud.ntnu.krisefikser.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.article.dto.ArticleRequest;
import stud.ntnu.krisefikser.article.dto.ArticleResponse;
import stud.ntnu.krisefikser.article.service.ArticleService;

/**
 * REST controller for managing articles in the system. Provides endpoints for CRUD operations on
 * articles.
 *
 * @since 1.0
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "Article", description = "Article management APIs")
public class ArticleController {

  private final ArticleService articleService;

  /**
   * Retrieves all articles from the system.
   *
   * @return ResponseEntity containing a list of all articles.
   */
  @Operation(summary = "Get all articles", description = "Retrieves a list of all articles in the"
      + " system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved articles",
          content = @Content(mediaType = "application/json", array = @ArraySchema(schema =
          @Schema(implementation = ArticleResponse.class))))
  })
  @GetMapping
  public ResponseEntity<List<ArticleResponse>> getAllArticles() {
    return ResponseEntity.ok(articleService.getAllArticles());
  }

  /**
   * Retrieves a specific article by its ID.
   *
   * @param id The ID of the article to retrieve.
   * @return ResponseEntity containing the requested article.
   * @since 1.0
   */
  @Operation(summary = "Get an article by ID", description = "Retrieves a specific article by its"
      + " ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the article",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              ArticleResponse.class))),
      @ApiResponse(responseCode = "404", description = "Article not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ArticleResponse> getArticleById(
      @Parameter(description = "ID of the article to retrieve") @PathVariable Long id) {
    return ResponseEntity.ok(articleService.getArticleById(id));
  }

  /**
   * Creates a new article in the system. Only accessible to users with ADMIN role.
   *
   * @param articleRequest The article to create.
   * @return ResponseEntity containing the created article.
   * @since 1.0
   */
  @Operation(summary = "Create a new article", description = "Creates a new article in the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Successfully created the article",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              ArticleResponse.class))),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<ArticleResponse> createArticle(
      @Parameter(description = "Article to create") @RequestBody @Valid ArticleRequest articleRequest) {
    return new ResponseEntity<>(articleService.createArticle(articleRequest), HttpStatus.CREATED);
  }

  /**
   * Updates an existing article in the system. Only accessible to users with ADMIN role.
   *
   * @param id             The ID of the article to update.
   * @param articleRequest The updated article data.
   * @return ResponseEntity containing the updated article.
   * @since 1.0
   */
  @Operation(summary = "Update an article", description = "Updates an existing article by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully updated the article",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation =
              ArticleResponse.class))),
      @ApiResponse(responseCode = "404", description = "Article not found"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<ArticleResponse> updateArticle(
      @Parameter(description = "ID of the article to update") @PathVariable Long id,
      @Parameter(description = "Updated article details")
      @RequestBody @Valid ArticleRequest articleRequest
  ) {
    return ResponseEntity.ok(articleService.updateArticle(id, articleRequest));
  }

  /**
   * Deletes an article from the system. Only accessible to users with ADMIN role.
   *
   * @param id The ID of the article to delete.
   * @return ResponseEntity with no content on successful deletion.
   * @since 1.0
   */
  @Operation(summary = "Delete an article", description = "Deletes an article from the system")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Successfully deleted the article"),
      @ApiResponse(responseCode = "404", description = "Article not found"),
      @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> deleteArticle(
      @Parameter(description = "ID of the article to delete") @PathVariable Long id) {
    articleService.deleteArticle(id);
    return ResponseEntity.noContent().build();
  }
}