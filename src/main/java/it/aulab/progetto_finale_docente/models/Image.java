package it.aulab.progetto_finale_docente.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String path;

    // Cambiato da @OneToOne a @ManyToOne per supportare più immagini per lo stesso articolo
    @ManyToOne
    @JoinColumn(name = "article_id")
    @JsonIgnoreProperties({ "images" })
    private Article article;

    // Nuovo campo: indica se questa immagine è la principale (copertina)
    @Column(nullable = false)
    private boolean isPrimary = false;
}