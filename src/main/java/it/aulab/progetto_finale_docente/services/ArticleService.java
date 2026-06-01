package it.aulab.progetto_finale_docente.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import it.aulab.progetto_finale_docente.dtos.ArticleDto;
import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.repositories.ArticleRepository;
import it.aulab.progetto_finale_docente.repositories.UserRepository;

@Service
public class ArticleService implements CrudService<ArticleDto, Article, Long> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ImageService imageService;

    @Override
    public List<ArticleDto> readAll() {
        List<ArticleDto> dtos = new ArrayList<>();
        for (Article article : articleRepository.findAll()) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    @Override
    public ArticleDto read(Long key) {
        Optional<Article> optArticle = articleRepository.findById(key);
        if (optArticle.isPresent()) {
            return modelMapper.map(optArticle.get(), ArticleDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Article id=" + key + " not found");
        }
    }

    
    @Override
    public ArticleDto create(Article article, Principal principal, MultipartFile file) {
        String url = "";

        // Recuperiamo l'utente loggato e lo associamo all'articolo
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = (userRepository.findById(userDetails.getId())).get();
            article.setUser(user);
        }

        // Gestione immagine (Salvataggio su Cloud asincrono)
        if (!file.isEmpty()) {
            try {
                CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                url = futureUrl.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Articolo va in revisione di default
        article.setIsAccepted(null);

        ArticleDto dto = modelMapper.map(articleRepository.save(article), ArticleDto.class);

        // Se l'immagine è presente, colleghiamo l'URL dell'immagine all'articolo nel DB
        if (!file.isEmpty()) {
            imageService.saveImageOnDB(url, article);
        }
        return dto;
    }

    @Override
    public ArticleDto update(Long key, Article updatedArticle, MultipartFile file) {
        String url = "";
        if (articleRepository.existsById(key)) {
            updatedArticle.setId(key);
            Article article = articleRepository.findById(key).get();

            // Impostiamo l'utente dell'articolo originale
            updatedArticle.setUser(article.getUser());

            if (!file.isEmpty()) {
                try {
                    // Eliminiamo la vecchia immagine se esiste
                    if (article.getImage() != null) {
                        imageService.deleteImage(article.getImage().getPath());
                    }
                    // Salviamo la nuova immagine
                    CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                    url = futureUrl.get();
                    imageService.saveImageOnDB(url, updatedArticle);
                    // L'articolo torna in revisione
                    updatedArticle.setIsAccepted(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (article.getImage() == null) {
                // Nessuna immagine né prima né dopo
                updatedArticle.setIsAccepted(article.getIsAccepted());
            } else {
                // Immagine non modificata
                updatedArticle.setImage(article.getImage());
                if (!updatedArticle.equals(article)) {
                    // Articolo modificato, torna in revisione
                    updatedArticle.setIsAccepted(null);
                } else {
                    // Articolo non modificato
                    updatedArticle.setIsAccepted(article.getIsAccepted());
                }
            }
            return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public void delete(Long key) {
        if (articleRepository.existsById(key)) {
            Article article = articleRepository.findById(key).get();
            try {
                if (article.getImage() != null) {
                    String path = article.getImage().getPath();
                    // Prima eliminiamo l'immagine dal DB
                    imageService.deleteImage(path);
                    // Aspettiamo un po' per l'operazione asincrona
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Poi eliminiamo l'articolo
            articleRepository.deleteById(key);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    // Ricerca per categoria
    public List<ArticleDto> searchByCategory(it.aulab.progetto_finale_docente.models.Category category) {
        List<ArticleDto> dtos = new ArrayList<>();
        for (Article article : articleRepository.findByCategory(category)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    // Ricerca per autore
    public List<ArticleDto> searchByAuthor(User user) {
        List<ArticleDto> dtos = new ArrayList<>();
        for (Article article : articleRepository.findByUser(user)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    // Accetta o rifiuta articolo
    public void setIsAccepted(Boolean result, Long id) {
        Article article = articleRepository.findById(id).get();
        article.setIsAccepted(result);
        articleRepository.save(article);
    }

    // Ricerca full-text
    public List<ArticleDto> search(String keyword) {
        List<ArticleDto> dtos = new ArrayList<>();
        for (Article article : articleRepository.search(keyword)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }
}