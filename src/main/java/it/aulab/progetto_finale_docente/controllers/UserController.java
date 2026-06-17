package it.aulab.progetto_finale_docente.controllers;

import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import it.aulab.progetto_finale_docente.models.Role;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.progetto_finale_docente.dtos.ArticleDto;
import it.aulab.progetto_finale_docente.dtos.UserDto;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.repositories.ArticleRepository;
import it.aulab.progetto_finale_docente.repositories.CareerRequestRepository;
import it.aulab.progetto_finale_docente.repositories.RoleRepository;
import it.aulab.progetto_finale_docente.services.ArticleService;
import it.aulab.progetto_finale_docente.services.CategoryService;
import it.aulab.progetto_finale_docente.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class UserController {

    private final ArticleService articleService;
    private final UserService userService;
    private final ArticleRepository articleRepository;
    private final it.aulab.progetto_finale_docente.repositories.UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CareerRequestRepository careerRequestRepository;
    private final CategoryService categoryService;

    public UserController(ArticleService articleService,
            UserService userService,
            ArticleRepository articleRepository,
            it.aulab.progetto_finale_docente.repositories.UserRepository userRepository,
            RoleRepository roleRepository,
            CareerRequestRepository careerRequestRepository,
            CategoryService categoryService) {
        this.articleService = articleService;
        this.userService = userService;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.careerRequestRepository = careerRequestRepository;
        this.categoryService = categoryService;
    }

    // Rotta di home
    @GetMapping("/")
    public String home(Model viewModel) {
        viewModel.addAttribute("title", "Home");
        List<ArticleDto> articles = articleService.readAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsAccepted()))
                .collect(Collectors.toList());

        Collections.sort(articles, Comparator.comparing(
                ArticleDto::getPublishDate,
                Comparator.nullsLast(Comparator.reverseOrder())));

        List<ArticleDto> lastThreeArticles = articles.stream()
                .limit(3)
                .collect(Collectors.toList());

        viewModel.addAttribute("articles", lastThreeArticles);

        List<ArticleDto> mostReadArticles = articleService.readMostRead();

        viewModel.addAttribute("mostRead", mostReadArticles);

        return "home";
    }

    // Rotta per la registrazione
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "Registrati");
        model.addAttribute("user", new UserDto());
        return "auth/register";
    }

    // Rotta per la login
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Accedi");
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
                    "Questa email è già usata da un'altro utente");
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

        // Mostra solo articoli accettati
        List<ArticleDto> acceptedArticles = articles.stream()
                .filter(article -> Boolean.TRUE.equals(article.getIsAccepted()))
                .collect(Collectors.toList());

        viewModel.addAttribute("articles", acceptedArticles);
        return "article/articles";

    }

    @GetMapping("/admin/users")
    public String adminUsers(Model viewModel, Principal principal) {
        viewModel.addAttribute("title", "Gestione Utenti");

        List<User> users = userRepository.findAll()
                .stream()
                .filter(u -> u.getRoles().stream()
                        .noneMatch(r -> r.getName().equals("ROLE_ADMIN")))
                .collect(Collectors.toList());

        viewModel.addAttribute("users", users);
        viewModel.addAttribute("roles", roleRepository.findAll()
                .stream()
                .filter(r -> !r.getName().equals("ROLE_ADMIN"))
                .collect(Collectors.toList()));
        return "admin/users";
    }

    @PostMapping("/admin/users/{id}/role")
    public String adminChangeRole(@PathVariable Long id,
            @RequestParam("roleId") Long roleId,
            RedirectAttributes redirectAttributes) {
        User user = userService.find(id);
        Role role = roleRepository.findById(roleId).get();
        List<Role> newRoles = new java.util.ArrayList<>();
        newRoles.add(role);
        user.setRoles(newRoles);
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("successMessage", "Ruolo aggiornato con successo!");
        return "redirect:/admin/users";
    }

    // Rotta per la dashboard dell'admin
    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model viewModel) {
        viewModel.addAttribute("title", "Richieste ricevute");
        viewModel.addAttribute("requests",
                careerRequestRepository.findByIsCheckedFalse());
        viewModel.addAttribute("categories", categoryService.readAll());
        return "admin/dashboard";
    }

    // Rotta per la dashboard del revisore
    @GetMapping("/revisor/dashboard")
    public String revisorDashboard(Model viewModel) {
        viewModel.addAttribute("title", "Articoli da revisionare");
        viewModel.addAttribute("articles",
                articleRepository.findByIsAcceptedIsNull());
        return "revisor/dashboard";
    }

    // Rotta per la dashboard del writer
    @GetMapping("/writer/dashboard")
    public String writerDashboard(Model viewModel, Principal principal) {
        viewModel.addAttribute("title", "I tuoi articoli");
        List<ArticleDto> userArticles = articleService.readAll()
                .stream()
                .filter(article -> article.getUser().getEmail().equals(principal.getName()))
                .toList();
        viewModel.addAttribute("articles", userArticles);
        return "writer/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model viewModel, Principal principal) {
        User user = userService.findUserByEmail(principal.getName());
        viewModel.addAttribute("title", "Il mio profilo");
        viewModel.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String profileUpdate(@RequestParam(required = false) String gender,
            @RequestParam(required = false) String birthDate, Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = userService.findUserByEmail(principal.getName());
        java.time.LocalDate bd = (birthDate != null && !birthDate.isEmpty())
                ? java.time.LocalDate.parse(birthDate)
                : null;
        userService.updateProfile(user.getId(), gender, bd);
        redirectAttributes.addFlashAttribute("successMessage", "Profilo aggiornato!");
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
            @RequestParam String newPassword,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = userService.findUserByEmail(principal.getName());
        try {
            userService.changePassword(user.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password cambiata con successo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model viewModel) {
        viewModel.addAttribute("title", "Reset password");
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSend(@RequestParam String email,
            RedirectAttributes redirectAttributes) {
        userService.sendPasswordResetEmail(email);
        redirectAttributes.addFlashAttribute("successMessage",
                "Se l'email esiste riceverai le istruzioni per il reset!");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model viewModel) {
        viewModel.addAttribute("title", "Nuova password");
        viewModel.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSave(@RequestParam String token,
            @RequestParam String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            userService.resetPassword(token, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password resettata! Accedi con la nuova password.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(Principal principal,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        User user = userService.findUserByEmail(principal.getName());
        userService.deleteAccount(user.getId());
        request.getSession().invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Account eliminato con successo.");
        return "redirect:/";
    }

    @GetMapping("/error/403")
    public String error403(Model viewModel) {
        viewModel.addAttribute("title", "Accesso negato");
        return "error/403";
    }
}