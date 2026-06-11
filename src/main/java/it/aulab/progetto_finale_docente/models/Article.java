package it.aulab.progetto_finale_docente.models;

import java.time.LocalDate;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotEmpty(message = "Il corpo dell'articolo non deve essere vuoto")
    private String body;

    
    @Column(name = "publish_date", nullable = false)
    @NotNull(message = "La data di pubblicazione è obbligatoria")
    private LocalDate publishDate;

    @Column(nullable = true)
    private Boolean isAccepted;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({ "articles" })
    private User user;

    @ManyToOne
    @JsonIgnoreProperties({ "articles" })
    @NotNull(message = "Devi selezionare una categoria per pubblicare l'articolo.")
    private Category category;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({ "article" })
    private java.util.List<Image> images = new java.util.ArrayList<>();

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    
}