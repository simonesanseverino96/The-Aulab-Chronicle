package it.aulab.progetto_finale_docente.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.progetto_finale_docente.dtos.ArticleDto;
import it.aulab.progetto_finale_docente.dtos.UserDto;
import it.aulab.progetto_finale_docente.models.Article;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.services.ArticleService;
import it.aulab.progetto_finale_docente.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;

    // Rotta di home
    @GetMapping("/")
    public String home(Model viewModel) {
        return "home";
    }

    // Rotta per la registrazione
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserDto());
        return "auth/register";
    }

    // Rotta per la login
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // Rotta per il salvataggio della registrazione
    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") UserDto userDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            HttpServletResponse response) {

        // Controllo se esiste già un utente con la stessa email
        User existingUser = userService.findUserByEmail(userDto.getEmail());
        if (existingUser != null && existingUser.getEmail() != null
                && !existingUser.getEmail().isEmpty()) {
            result.rejectValue("email", null,
                    "There is already an account registered with the same email");
        }

        // Controllo errori di validazione
        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "auth/register";
        }

        // Salvataggio utente e redirect alla home
        userService.saveUser(userDto, redirectAttributes, request, response);
        redirectAttributes.addFlashAttribute("successMessage", "Registrazione avvenuta!");
        return "redirect:/";
    }

    // Rotta per la ricerca degli articoli in base all'utente
    @GetMapping("/search/{id}")
    public String userArticlesSearch(@PathVariable Long id, Model viewModel) {
        User user = userService.find(id);
        viewModel.addAttribute("title",
                "Tutti gli articoli trovati per utente " + user.getUsername());

        List<ArticleDto> articles = articleService.searchByAuthor(user);

        //Mostra solo articoli accettati
        List<ArticleDto> acceptedArticles = articles.stream()
                .filter(article -> Boolean.TRUE.equals(article.getIsAccepted()))
                .collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);
        return "article/articles";

    }
}