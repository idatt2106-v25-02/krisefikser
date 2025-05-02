package stud.ntnu.krisefikser.article.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.article.dto.ArticleRequest;
import stud.ntnu.krisefikser.article.dto.ArticleResponse;
import stud.ntnu.krisefikser.article.exception.ArticleNotFoundException;
import stud.ntnu.krisefikser.article.service.ArticleService;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.common.TestSecurityConfig;

@WebMvcTest(ArticleController.class)
@Import(TestSecurityConfig.class)
class ArticleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ArticleService articleService;

  @MockitoBean
  private TokenService tokenService;

  @MockitoBean
  private CustomUserDetailsService userDetailsService;

  private ArticleResponse articleResponse;
  private final LocalDateTime now = LocalDateTime.now();

  @BeforeEach
  void setUp() {
    articleResponse = ArticleResponse.builder()
        .id(1L)
        .title("Test Article")
        .text("Test Content")
        .createdAt(now)
        .imageUrl("http://example.com/image.jpg")
        .build();
  }

  @Test
  void getAllArticles_ShouldReturnListOfArticles() throws Exception {
    when(articleService.getAllArticles()).thenReturn(List.of(articleResponse));

    mockMvc.perform(get("/api/articles"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].id").value(articleResponse.getId()))
        .andExpect(jsonPath("$[0].title").value(articleResponse.getTitle()))
        .andExpect(jsonPath("$[0].text").value(articleResponse.getText()));
  }

  @Test
  void getArticleById_WhenArticleExists_ShouldReturnArticle() throws Exception {
    when(articleService.getArticleById(1L)).thenReturn(articleResponse);

    mockMvc.perform(get("/api/articles/1"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(articleResponse.getId()))
        .andExpect(jsonPath("$.title").value(articleResponse.getTitle()))
        .andExpect(jsonPath("$.text").value(articleResponse.getText()));
  }

  @Test
  void getArticleById_WhenArticleDoesNotExist_ShouldReturn404() throws Exception {
    when(articleService.getArticleById(1L))
        .thenThrow(new ArticleNotFoundException("Article not found with id: 1"));

    mockMvc.perform(get("/api/articles/1"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createArticle_ShouldReturnCreatedArticle() throws Exception {
    when(articleService.createArticle(any(ArticleRequest.class))).thenReturn(articleResponse);

    mockMvc.perform(post("/api/articles")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(articleResponse)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(articleResponse.getId()))
        .andExpect(jsonPath("$.title").value(articleResponse.getTitle()))
        .andExpect(jsonPath("$.text").value(articleResponse.getText()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateArticle_WhenArticleExists_ShouldReturnUpdatedArticle() throws Exception {
    when(articleService.updateArticle(eq(1L), any(ArticleRequest.class))).thenReturn(
        articleResponse);

    mockMvc.perform(put("/api/articles/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(articleResponse)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(articleResponse.getId()))
        .andExpect(jsonPath("$.title").value(articleResponse.getTitle()))
        .andExpect(jsonPath("$.text").value(articleResponse.getText()));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateArticle_WhenArticleDoesNotExist_ShouldReturn404() throws Exception {
    when(articleService.updateArticle(eq(1L), any(ArticleRequest.class)))
        .thenThrow(new ArticleNotFoundException("Article not found with id: 1"));

    mockMvc.perform(put("/api/articles/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(articleResponse)))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteArticle_WhenArticleExists_ShouldReturn204() throws Exception {
    mockMvc.perform(delete("/api/articles/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteArticle_WhenArticleDoesNotExist_ShouldReturn404() throws Exception {
    doThrow(new ArticleNotFoundException("Article not found with id: 1"))
        .when(articleService).deleteArticle(1L);

    mockMvc.perform(delete("/api/articles/1")
            .with(SecurityMockMvcRequestPostProcessors.csrf()))
        .andExpect(status().isNotFound());
  }

  @Test
  void createArticle_WhenUnauthorized_ShouldReturn401() throws Exception {
    mockMvc.perform(post("/api/articles")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(articleResponse)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void createArticle_WhenNotAdmin_ShouldReturn403() throws Exception {
    mockMvc.perform(post("/api/articles")
            .with(SecurityMockMvcRequestPostProcessors.csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(articleResponse)))
        .andExpect(status().isForbidden());
  }
}