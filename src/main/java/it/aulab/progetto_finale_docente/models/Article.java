package it.aulab.progetto_finale_docente.models;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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

    @Column(nullable = false, length = 100)
    @NotEmpty(message = "Il titolo non deve essere vuoto")
    @Size(max = 100)
    private String title;

    @Column(nullable = false, length = 100)
    @NotEmpty(message = "Il sottotitolo non deve essere vuoto")
    @Size(max = 100)
    private String subtitle;

    @Column(nullable = false, length = 1000)
    @NotEmpty(message = "Il corpo dell''articolo non deve essere vuoto")
    @Size(max = 1000)
    private String body;

    @Column(nullable = true, length = 8)
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

    @OneToOne(mappedBy = "article")
    @JsonIgnoreProperties({ "article" })
    private Image image;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Article article = (Article) obj;
        if (title.equals(article.getTitle()) &&
                subtitle.equals(article.getSubtitle()) &&
                body.equals(article.getBody()) &&
                publishDate.equals(article.getPublishDate()) &&
                category.getName().equals(article.getCategory().getName()) &&
                image != null && article.getImage() != null &&
                image.getPath().equals(article.getImage().getPath())) {
            return true;
        }
        return false;
    }
}