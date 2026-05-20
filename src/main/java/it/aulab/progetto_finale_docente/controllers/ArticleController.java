package it.aulab.progetto_finale_docente.controllers;

import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors; // AGGIUNTO: Import fondamentale per il corretto funzionamento dello stream

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    // FASE 10: Rotta index degli articoli (Verificata e Completa)
    @GetMapping
    public String articlesIndex(Model viewModel) {
        viewModel.addAttribute("title", "Tutti gli articoli");

        // Prendiamo tutti gli articoli, filtriamo solo quelli accettati dai revisori (isAccepted == true)
        List<ArticleDto> articles = articleService.readAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsAccepted()))
                .collect(Collectors.toList());

        // Ordiniamo gli articoli in base alla data di pubblicazione, invertendo l'ordine (dal più recente)
        Collections.sort(articles, Comparator.comparing(ArticleDto::getPublishDate).reversed());
        viewModel.addAttribute("articles", articles);
        return "article/articles";
    }

    // Rotta per la visualizzazione della pagina di creazione di un nuovo articolo
    @GetMapping("create")
    public String articleCreate(Model viewModel) {
        viewModel.addAttribute("title", "Crea un articolo");
        viewModel.addAttribute("article", new Article());
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/create";
    }

    // Rotta per il salvataggio effettivo dell'articolo (e dell'immagine allegata)
    @PostMapping
    public String articleStore(@Valid @ModelAttribute("article") Article article, BindingResult result,
            RedirectAttributes redirectAttributes, Principal principal, MultipartFile file, Model viewModel) {

        // Se ci sono errori di validazione nei campi del form, ricarichiamo la pagina mostrando i problemi
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Crea un articolo");
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/create";
        }

        // Invochiamo il servizio che salverà l'articolo sul DB e caricherà l'immagine sul cloud
        articleService.create(article, principal, file);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo aggiunto con successo!");

        return "redirect:/";
    }
}