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

    // FASE 10: Rotta index degli articoli (Verificata e Completa)
    @GetMapping
    public String articlesIndex(Model viewModel) {
        viewModel.addAttribute("title", "Tutti gli articoli");

        // Prendiamo tutti gli articoli, filtriamo solo quelli accettati dai revisori
        // (isAccepted == true)
        List<ArticleDto> articles = articleService.readAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsAccepted()))
                .collect(Collectors.toList());

        // Ordiniamo gli articoli in base alla data di pubblicazione, invertendo
        // l'ordine (dal più recente)
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

        // Se ci sono errori di validazione nei campi del form, ricarichiamo la pagina
        // mostrando i problemi
        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Crea un articolo");
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/create";
        }

        // Invochiamo il servizio che salverà l'articolo sul DB e caricherà l'immagine
        // sul cloud
        articleService.create(article, principal, file);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo aggiunto con successo!");

        return "redirect:/";
    }

    // Rotta di dettaglio di un articolo
    @GetMapping("detail/{id}")
    public String detailArticle(@PathVariable Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "article/detail";
    }

    // Rotta dettaglio di un articolo per il revisore
    @GetMapping("revisor/detail/{id}")
    public String revisorDetailArticle(@PathVariable Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article detail");
        viewModel.addAttribute("article", articleService.read(id));
        return "revisor/detail";
    }

    // Rotta dedicata all'azione del revisore
    @PostMapping("/accept")
    public String articleSetAccepted(@RequestParam("action") String action,
            @RequestParam("articleId") Long articleId,
            RedirectAttributes redirectAttributes) {
        if (action.equals("accept")) {
            articleService.setIsAccepted(true, articleId);
            redirectAttributes.addFlashAttribute("resultMessage", "Articolo accettato!");
        } else if (action.equals("reject")) {
            articleService.setIsAccepted(false, articleId);
            redirectAttributes.addFlashAttribute("resultMessage", "Articolo rifiutato!");
        } else {
            redirectAttributes.addFlashAttribute("resultMessage", "Azione non corretta!");
        }
        return "redirect:/revisor/dashboard";
    }

    // Rotta di ricerca di un articolo
    @GetMapping("/search")
    public String articleSearch(@RequestParam("keyword") String keyword, Model viewModel) {
        viewModel.addAttribute("title", "Tutti gli articoli trovati");

        List<ArticleDto> articles = articleService.search(keyword);
        List<ArticleDto> acceptedArticles = articles.stream()
                .filter(article -> Boolean.TRUE.equals(article.getIsAccepted()))
                .collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);
        return "article/articles";
    }

    // Rotta di modifica di un articolo
    @GetMapping("/edit/{id}")
    public String editArticle(@PathVariable Long id, Model viewModel) {
        viewModel.addAttribute("title", "Article update");
        viewModel.addAttribute("article", articleService.read(id));
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/edit";
    }

    // Rotta di memorizzazione modifica di un articolo
    @PostMapping("/update/{id}")
    public String articleUpdate(@PathVariable Long id,
            @Valid @ModelAttribute("article") Article article,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Principal principal,
            MultipartFile file,
            Model viewModel) {

        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Article update");
            article.setImage(articleService.read(id).getImage());
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/edit";
        }

        articleService.update(id, article, file);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo modificato con successo!");
        return "redirect:/articles";
    }

    // Rotta per la cancellazione di un articolo
    @GetMapping("/delete/{id}")
    public String articleDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        articleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo cancellato con successo!");
        return "redirect:/writer/dashboard";
    }
}