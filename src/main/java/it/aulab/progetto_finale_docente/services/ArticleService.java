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
    @org.springframework.transaction.annotation.Transactional
    public List<ArticleDto> readAll() {
        List<ArticleDto> dtos = new ArrayList<>();
        for (Article article : articleRepository.findAll()) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ArticleDto read(Long key) {
        Optional<Article> optArticle = articleRepository.findById(key);
        if (optArticle.isPresent()) {
            return modelMapper.map(optArticle.get(), ArticleDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Article id=" + key + " not found");
        }
    }

    public ArticleDto createMultiple(Article article, Principal principal, MultipartFile[] files) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = (userRepository.findById(userDetails.getId())).get();
            article.setUser(user);
        }

        article.setIsAccepted(null);
        Article savedArticle = articleRepository.save(article);

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                if (!file.isEmpty()) {
                    try {
                        CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                        String url = futureUrl.get();

                        boolean isPrimary = (i == 0);
                        saveImageOnDBWithPrimaryFlag(url, savedArticle, isPrimary);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return modelMapper.map(savedArticle, ArticleDto.class);
    }

    @Override
    public ArticleDto create(Article article, Principal principal, MultipartFile file) {
        return createMultiple(article, principal, new MultipartFile[] { file });
    }

    @Override
    public ArticleDto update(Long key, Article updatedArticle, MultipartFile file) {
        if (articleRepository.existsById(key)) {
            updatedArticle.setId(key);
            Article article = articleRepository.findById(key).get();

            updatedArticle.setUser(article.getUser());

            if (!file.isEmpty()) {
                try {
                    if (article.getImages() != null && !article.getImages().isEmpty()) {
                        for (it.aulab.progetto_finale_docente.models.Image oldImg : article.getImages()) {
                            imageService.deleteImage(oldImg.getPath());
                        }
                        article.getImages().clear();
                    }

                    CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                    String url = futureUrl.get();
                    saveImageOnDBWithPrimaryFlag(url, updatedArticle, true);

                    updatedArticle.setIsAccepted(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (article.getImages() == null || article.getImages().isEmpty()) {
                updatedArticle.setIsAccepted(article.getIsAccepted());
            } else {
                updatedArticle.setImages(article.getImages());
                if (!updatedArticle.equals(article)) {
                    updatedArticle.setIsAccepted(null);
                } else {
                    updatedArticle.setIsAccepted(article.getIsAccepted());
                }
            }
            return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void delete(Long key) {
        if (articleRepository.existsById(key)) {
            Article article = articleRepository.findById(key).get();
            try {
                if (article.getImages() != null && !article.getImages().isEmpty()) {
                    for (it.aulab.progetto_finale_docente.models.Image img : article.getImages()) {
                        imageService.deleteImage(img.getPath());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            articleRepository.delete(article);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private void saveImageOnDBWithPrimaryFlag(String url, Article article, boolean isPrimary) {
        it.aulab.progetto_finale_docente.models.Image image = new it.aulab.progetto_finale_docente.models.Image();
        image.setPath(url);
        image.setArticle(article);
        image.setPrimary(isPrimary);
        imageService.saveImageOnDB(url, article);
    }

    @org.springframework.transaction.annotation.Transactional
    public List<ArticleDto> searchByCategory(it.aulab.progetto_finale_docente.models.Category category) {
        List<ArticleDto> dtos = new ArrayList<>();
        for (Article article : articleRepository.findByCategory(category)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    @org.springframework.transaction.annotation.Transactional
    public List<ArticleDto> searchByAuthor(User user) {
        List<ArticleDto> dtos = new ArrayList<>();
        for (Article article : articleRepository.findByUser(user)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    public void setIsAccepted(Boolean result, Long id) {
        Article article = articleRepository.findById(id).get();
        article.setIsAccepted(result);
        articleRepository.save(article);
    }

    @org.springframework.transaction.annotation.Transactional
    public List<ArticleDto> search(String keyword) {
        List<ArticleDto> dtos = new ArrayList<>();
        for (Article article : articleRepository.search(keyword)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

    @org.springframework.transaction.annotation.Transactional
    public void incrementViews(Long id) {
        Optional<Article> optArticle = articleRepository.findById(id);
        if (optArticle.isPresent()) {
            Article article = optArticle.get();
            article.setViewCount(article.getViewCount() + 1);
            articleRepository.save(article);
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public List<ArticleDto> readMostRead() {
        List<ArticleDto> dtos = new ArrayList<>();
        for (Article article : articleRepository.findTop3ByOrderByViewCountDesc()) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }
}