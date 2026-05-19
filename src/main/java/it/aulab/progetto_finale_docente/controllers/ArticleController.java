package it.aulab.progetto_finale_docente.controllers;

import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.progetto_finale_docente.dtos.ArticleDto;
import it.aulab.progetto_finale_docente.dtos.CategoryDto;
import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.Category;
import it.aulab.progetto_finale_docente.services.ArticleService;
import it.aulab.progetto_finale_docente.services.CrudService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    @Qualifier("categoryService")
    private CrudService<CategoryDto, Category, Long> categoryService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ModelMapper modelMapper;

    // Rotta index degli articoli

    @GetMapping
    public String articlesIndex(Model viewModel) {
        viewModel.addAttribute("title", "Tutti gli articoli");

        List<ArticleDto> articles = articleService.readAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsAccepted()))
                .collect(Collectors.toList());

        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());
        viewModel.addAttribute("articles", articles);
        return "article/articles";
    }

    // Rotta per la creazione di un nuovo articolo
    @GetMapping("create")
    public String articleCreate(Model viewModel) {
        viewModel.addAttribute("title", "Crea un articolo");
        viewModel.addAttribute("article", new Article());
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/create";
    }

    // Rotta per lo store di un articolo
    @PostMapping
    public String articleStore(@Valid @ModelAttribute("article") Article article, BindingResult result,
            RedirectAttributes redirectAttributes, Principal principal, MultipartFile file, Model viewModel) {

        // Controllo degli errori con validazioni
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Crea un articolo");
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/create";
        }

        // Salvo l'articolo e lo redirecto alla home
        articleService.create(article, principal, file);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo aggiunto con successo!");

        return "redirect:/";
    }

}
