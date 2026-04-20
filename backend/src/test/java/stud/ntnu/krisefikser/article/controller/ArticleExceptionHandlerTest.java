package stud.ntnu.krisefikser.article.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestControllerAdvice;

class ArticleExceptionHandlerTest {

  @Test
  void shouldBeConfiguredAsRestControllerAdviceForArticlePackage() {
    RestControllerAdvice annotation = ArticleExceptionHandler.class.getAnnotation(
        RestControllerAdvice.class);

    assertThat(annotation).isNotNull();
    assertThat(annotation.basePackages()).contains("stud.ntnu.krisefikser.article");
  }
}
