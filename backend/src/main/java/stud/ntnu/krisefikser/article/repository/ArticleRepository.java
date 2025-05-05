package stud.ntnu.krisefikser.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.article.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {

}