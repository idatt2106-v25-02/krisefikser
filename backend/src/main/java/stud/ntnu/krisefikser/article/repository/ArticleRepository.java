package stud.ntnu.krisefikser.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.article.entity.Article;

/**
 * Repository interface for managing {@link Article} entities. This interface extends JpaRepository
 * to provide CRUD operations and custom query methods.
 */
public interface ArticleRepository extends JpaRepository<Article, Long> {

}