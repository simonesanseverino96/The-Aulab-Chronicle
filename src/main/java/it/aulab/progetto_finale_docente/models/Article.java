package it.aulab.progetto_finale_docente.models;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    @NotEmpty(message = "Il titolo non deve essere vuoto")
    @Size(max = 255)
    private String title;

    @Column(nullable = false, length = 255)
    @NotEmpty(message = "Il sottotitolo non deve essere vuoto")
    @Size(max = 255)
    private String subtitle;

    // Usiamo -columnDefinition = "TEXT" in MySQL non ha limite fisso (fino a 65.000
    // caratteri)
    @Column(nullable = false, columnDefinition = "TEXT")
    @NotEmpty(message = "Il corpo dell''articolo non deve essere vuoto")

    private String body;

    @Column(nullable = true)
    private LocalDate publishDate;

    @Column(nullable = true)
    private Boolean isAccepted;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({ "articles" })
    private User user;

    @ManyToOne
    @JsonIgnoreProperties({ "articles" })
    @NotNull(message = "Devi selezionare una categoria per pubblicare l''articolo.")
    private Category category;

    @jakarta.persistence.OneToMany(mappedBy = "article", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "article" })
    private java.util.List<Image> images = new java.util.ArrayList<>();

    // Contatore visualizzazioni
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Article article = (Article) obj;

        return Objects.equals(title, article.title) &&
                Objects.equals(subtitle, article.subtitle) &&
                Objects.equals(body, article.body) &&
                Objects.equals(publishDate, article.publishDate) &&
                Objects.equals(category != null ? category.getName() : null,
                        article.category != null ? article.category.getName() : null)
                &&
                Objects.equals(images, article.images); // Confronta la lista di immagini in sicurezza
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, subtitle, body, publishDate);
    }
}