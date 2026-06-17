package it.aulab.progetto_finale_docente.controllers;

import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
@RequestMapping("/articles")
public class ArticleController {

    private final CrudService<CategoryDto, Category, Long> categoryService;
    private final org.modelmapper.ModelMapper modelMapper;
    private final ArticleService articleService;

    public ArticleController(@Qualifier("categoryService") CrudService<CategoryDto, Category, Long> categoryService,
            org.modelmapper.ModelMapper modelMapper,
            ArticleService articleService) {
        this.categoryService = categoryService;
        this.modelMapper = modelMapper;
        this.articleService = articleService;
    }

    @GetMapping
    public String articlesIndex(Model viewModel) {
        viewModel.addAttribute("title", "Tutti gli articoli");

        List<ArticleDto> articles = articleService.readAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsAccepted()))
                .collect(Collectors.toList());

        Collections.sort(articles, Comparator.comparing(
                ArticleDto::getPublishDate,
                Comparator.nullsLast(Comparator.reverseOrder())));

        viewModel.addAttribute("articles", articles);
        return "article/articles";
    }

    @GetMapping("create")
    public String articleCreate(Model viewModel) {
        viewModel.addAttribute("title", "Crea un articolo");
        viewModel.addAttribute("article", new Article());
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/create";
    }

    @PostMapping
    public String articleStore(@Valid @ModelAttribute("article") Article article, BindingResult result,
            RedirectAttributes redirectAttributes, Principal principal, @RequestParam("files") MultipartFile[] files,
            Model viewModel) {

        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Crea un articolo");
            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/create";
        }

        articleService.createMultiple(article, principal, files);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo aggiunto con successo!");

        return "redirect:/";
    }

    @GetMapping("detail/{id}")
    public String detailArticle(@PathVariable Long id, Model viewModel) {
        ArticleDto articleDto = articleService.read(id);
        viewModel.addAttribute("title", articleDto.getTitle());
        viewModel.addAttribute("article", articleDto);
        articleService.incrementViews(id);
        return "article/detail";
    }

    @GetMapping("revisor/detail/{id}")
    public String revisorDetailArticle(@PathVariable Long id, Model viewModel,
            Principal principal, RedirectAttributes redirectAttributes) {
        ArticleDto articleDto = articleService.read(id);

        if (articleDto.getUser().getEmail().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Non puoi valutare un tuo articolo!");
            return "redirect:/revisor/dashboard";
        }

        viewModel.addAttribute("title", articleDto.getTitle());
        viewModel.addAttribute("article", articleDto);
        return "revisor/detail";
    }

    @PostMapping("/accept")
    public String articleSetAccepted(@RequestParam("action") String action,
            @RequestParam("articleId") Long articleId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        ArticleDto articleDto = articleService.read(articleId);

        if (articleDto.getUser().getEmail().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Non puoi valutare un tuo articolo!");
            return "redirect:/revisor/dashboard";
        }

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

    @GetMapping("/edit/{id}")
    public String editArticle(@PathVariable Long id, Model viewModel) {
        ArticleDto articleDto = articleService.read(id);
        viewModel.addAttribute("title", "Modifica: " + articleDto.getTitle());
        viewModel.addAttribute("article", articleDto);
        viewModel.addAttribute("categories", categoryService.readAll());
        return "article/edit";
    }

    @PostMapping("/update/{id}")
    public String articleUpdate(@PathVariable Long id,
            @Valid @ModelAttribute("article") Article article,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Principal principal,
            @RequestParam("files") MultipartFile[] files,
            Model viewModel) {

        if (result.hasErrors()) {
            viewModel.addAttribute("title", "Modifica: " + article.getTitle());

            it.aulab.progetto_finale_docente.dtos.ArticleDto originalDto = articleService.read(id);
            if (originalDto != null) {
                article.setImages(modelMapper.map(originalDto, Article.class).getImages());
            }

            viewModel.addAttribute("article", article);
            viewModel.addAttribute("categories", categoryService.readAll());
            return "article/edit";
        }

        articleService.updateMultiple(id, article, files);

        redirectAttributes.addFlashAttribute("successMessage", "Articolo modificato con successo!");
        return "redirect:/articles";
    }

    @GetMapping("/delete/{id}")
    public String articleDelete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        articleService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Articolo cancellato con successo!");
        return "redirect:/writer/dashboard";
    }

    @GetMapping("/category/{id}")
    @org.springframework.transaction.annotation.Transactional
    public String articlesByCategory(@PathVariable Long id, Model viewModel) {
        Category category = categoryService.readAll()
                .stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .map(dto -> modelMapper.map(dto, Category.class))
                .orElseThrow();

        List<ArticleDto> articles = articleService.searchByCategory(category)
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsAccepted()))
                .collect(Collectors.toList());

        viewModel.addAttribute("title", "Categoria: " + category.getName());
        viewModel.addAttribute("articles", articles);
        return "article/articles";
    }
}