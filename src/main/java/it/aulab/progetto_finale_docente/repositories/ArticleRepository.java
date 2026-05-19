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
    List<Article> findByAcceptedIsNull();

    @Query("SELECT a FROM Article a WHERE " +
        "LOWER(a.title) LIKE LOWER(CONTACT('%', :searchTerm, '%')) OR " +
        "LOWER(a.subtitle) LIKE LOWER(CONTACT('%', :searchTerm, '%')) OR " +
        "LOWER(a.user.username) LIKE LOWER(CONTACT('%', :searchTerm, '%')) OR " +
        "LOWER(a.category.name) LIKE LOWER(CONTACT('%', :searchTerm, '%'))")
        List<Article> search(@Param("searchTerm") String searchTerm);
    
}
