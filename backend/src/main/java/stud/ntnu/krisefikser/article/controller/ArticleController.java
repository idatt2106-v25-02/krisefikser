package stud.ntnu.krisefikser.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.article.data.ArticleDTO;
import stud.ntnu.krisefikser.article.service.ArticleService;

import java.util.List;

/**
 * REST controller for managing articles in the system.
 * Provides endpoints for CRUD operations on articles.
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
     * @since 1.0
     */
    @Operation(summary = "Get all articles", description = "Retrieves a list of all articles in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved articles", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    /**
     * Retrieves a specific article by its ID.
     *
     * @param id The ID of the article to retrieve.
     * @return ResponseEntity containing the requested article.
     * @since 1.0
     */
    @Operation(summary = "Get an article by ID", description = "Retrieves a specific article by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the article", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Article not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(
            @Parameter(description = "ID of the article to retrieve") @PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    /**
     * Creates a new article in the system.
     *
     * @param articleDTO The article to create.
     * @return ResponseEntity containing the created article.
     * @since 1.0
     */
    @Operation(summary = "Create a new article", description = "Creates a new article in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created the article", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class)))
    })
    @PostMapping
    public ResponseEntity<ArticleDTO> createArticle(
            @Parameter(description = "Article to create") @RequestBody ArticleDTO articleDTO) {
        return new ResponseEntity<>(articleService.createArticle(articleDTO), HttpStatus.CREATED);
    }

    /**
     * Updates an existing article in the system.
     *
     * @param id         The ID of the article to update.
     * @param articleDTO The updated article data.
     * @return ResponseEntity containing the updated article.
     * @since 1.0
     */
    @Operation(summary = "Update an article", description = "Updates an existing article by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the article", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ArticleDTO.class))),
            @ApiResponse(responseCode = "404", description = "Article not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ArticleDTO> updateArticle(
            @Parameter(description = "ID of the article to update") @PathVariable Long id,
            @Parameter(description = "Updated article details") @RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDTO));
    }

    /**
     * Deletes an article from the system.
     *
     * @param id The ID of the article to delete.
     * @return ResponseEntity with no content on successful deletion.
     * @since 1.0
     */
    @Operation(summary = "Delete an article", description = "Deletes an article from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the article"),
            @ApiResponse(responseCode = "404", description = "Article not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(
            @Parameter(description = "ID of the article to delete") @PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}