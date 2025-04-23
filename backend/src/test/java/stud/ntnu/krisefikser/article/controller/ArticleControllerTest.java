package stud.ntnu.krisefikser.article.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import stud.ntnu.krisefikser.article.data.ArticleDTO;
import stud.ntnu.krisefikser.article.exception.ArticleNotFoundException;
import stud.ntnu.krisefikser.article.service.ArticleService;
import stud.ntnu.krisefikser.config.TestSecurityConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
@Import(TestSecurityConfig.class)
class ArticleControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ArticleService articleService;

        private ArticleDTO articleDTO;
        private final LocalDateTime now = LocalDateTime.now();

        @BeforeEach
        void setUp() {
                articleDTO = ArticleDTO.builder()
                                .id(1L)
                                .title("Test Article")
                                .text("Test Content")
                                .createdAt(now)
                                .imageUrl("http://example.com/image.jpg")
                                .build();
        }

        @Test
        void getAllArticles_ShouldReturnListOfArticles() throws Exception {
                when(articleService.getAllArticles()).thenReturn(List.of(articleDTO));

                mockMvc.perform(get("/api/articles"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$[0].id").value(articleDTO.getId()))
                                .andExpect(jsonPath("$[0].title").value(articleDTO.getTitle()))
                                .andExpect(jsonPath("$[0].text").value(articleDTO.getText()));
        }

        @Test
        void getArticleById_WhenArticleExists_ShouldReturnArticle() throws Exception {
                when(articleService.getArticleById(1L)).thenReturn(articleDTO);

                mockMvc.perform(get("/api/articles/1"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id").value(articleDTO.getId()))
                                .andExpect(jsonPath("$.title").value(articleDTO.getTitle()))
                                .andExpect(jsonPath("$.text").value(articleDTO.getText()));
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
                when(articleService.createArticle(any(ArticleDTO.class))).thenReturn(articleDTO);

                mockMvc.perform(post("/api/articles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(articleDTO)))
                                .andExpect(status().isCreated())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id").value(articleDTO.getId()))
                                .andExpect(jsonPath("$.title").value(articleDTO.getTitle()))
                                .andExpect(jsonPath("$.text").value(articleDTO.getText()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateArticle_WhenArticleExists_ShouldReturnUpdatedArticle() throws Exception {
                when(articleService.updateArticle(eq(1L), any(ArticleDTO.class))).thenReturn(articleDTO);

                mockMvc.perform(put("/api/articles/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(articleDTO)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.id").value(articleDTO.getId()))
                                .andExpect(jsonPath("$.title").value(articleDTO.getTitle()))
                                .andExpect(jsonPath("$.text").value(articleDTO.getText()));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void updateArticle_WhenArticleDoesNotExist_ShouldReturn404() throws Exception {
                when(articleService.updateArticle(eq(1L), any(ArticleDTO.class)))
                                .thenThrow(new ArticleNotFoundException("Article not found with id: 1"));

                mockMvc.perform(put("/api/articles/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(articleDTO)))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteArticle_WhenArticleExists_ShouldReturn204() throws Exception {
                mockMvc.perform(delete("/api/articles/1"))
                                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void deleteArticle_WhenArticleDoesNotExist_ShouldReturn404() throws Exception {
                doThrow(new ArticleNotFoundException("Article not found with id: 1"))
                                .when(articleService).deleteArticle(1L);

                mockMvc.perform(delete("/api/articles/1"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void createArticle_WhenUnauthorized_ShouldReturn403() throws Exception {
                mockMvc.perform(post("/api/articles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(articleDTO)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "USER")
        void createArticle_WhenNotAdmin_ShouldReturn403() throws Exception {
                mockMvc.perform(post("/api/articles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(articleDTO)))
                                .andExpect(status().isForbidden());
        }
}