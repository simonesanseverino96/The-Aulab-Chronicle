package it.aulab.progetto_finale_docente.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import it.aulab.progetto_finale_docente.models.Image;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    // Questo metodo serve per eliminare l'immagine dal DB usando il path stringa
    void deleteByPath(String path);
}