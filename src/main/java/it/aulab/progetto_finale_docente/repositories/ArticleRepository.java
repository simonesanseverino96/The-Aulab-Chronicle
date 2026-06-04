package it.aulab.progetto_finale_docente.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.Category;
import it.aulab.progetto_finale_docente.models.User;

public interface ArticleRepository extends ListCrudRepository<Article, Long> {

    List<Article> findByCategory(Category category);

    List<Article> findByUser(User user);

    List<Article> findByIsAcceptedTrue();

    List<Article> findByIsAcceptedFalse();

    // Cambiato da findByAcceptedIsNull a findByIsAcceptedIsNull
    List<Article> findByIsAcceptedIsNull();

    long countByIsAcceptedIsNull();

    @Query("SELECT a FROM Article a WHERE " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.subtitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.category.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Article> search(@Param("searchTerm") String searchTerm);

    // Sostituisci il vecchio metodo automatico con questa query esplicita
    // nativa/JPQL:
    @Query(value = "SELECT * FROM articles WHERE is_accepted = true ORDER BY view_count DESC LIMIT 3", nativeQuery = true)
    List<Article> findTop3ByOrderByViewCountDesc();
}