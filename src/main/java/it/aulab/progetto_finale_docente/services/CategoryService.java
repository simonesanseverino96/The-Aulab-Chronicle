package it.aulab.progetto_finale_docente.services;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import ch.qos.logback.core.model.Model;
import it.aulab.progetto_finale_docente.dtos.CategoryDto;
import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.Category;
import jakarta.transaction.Transactional;

@Service
public class CategoryService implements CrudService<CategoryDto, Category, Long>{

    @Autowired
    private CategoryRepository categoryRepository;

   @Autowired
   private ModelMapper modelMapper;

   @Override
   public List<CategoryDto> readAll() {
      List<CategoryDto> dtos = new ArrayList<>();
        for(Category category : categoryRepository.findAll()) {
            dtos.add(modelMapper.map(category, CategoryDto.class));
        }
        return dtos;
   }
   
   @Override
    public CategoryDto read(Long key) {
        return modelMapper.map(categoryRepository.findById(key), destinationType:CategoryDto.class);
    }

    @Override
    public CategoryDto create(Category model, Principal principal, MultipartFile file) {
        return modelMapper.map(categoryRepository.save(model), CategoryDto.class);
    }

    @Override
    public CategoryDto update(Long key, Category model, MultipartFile file) {
        if(categoryRepository.existsById(key)) {
            model.setId(key);
            return modelMapper.map(categoryRepository.save(model), CategoryDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public void delete(Long key) {
        if(categoryRepository.existsById(key)) {
            Category category = categoryRepository.findById(key).get();
            // Sgancia la relazione con gli articoli prima di cancellare
            if(category.getArticles() != null) {
                Iterable<Article> articles = category.getArticles();
                for(Article article : articles) {
                    article.setCategory(null);
                }
            }
            categoryRepository.deleteById(key);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }


}
