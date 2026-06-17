package it.aulab.progetto_finale_docente.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.progetto_finale_docente.dtos.UserDto;
import it.aulab.progetto_finale_docente.models.Role;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.repositories.RoleRepository;
import it.aulab.progetto_finale_docente.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final EmailService emailService;

    // Iniezione tramite costruttore
    public UserServiceImpl(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService customUserDetailsService,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.emailService = emailService;
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User find(Long id) {
        return userRepository.findById(id).get();
    }

    @Override
    public void saveUser(UserDto userDto, RedirectAttributes redirectAttributes,
            HttpServletRequest request, HttpServletResponse response) {
        User user = new User();

        // Mappiamo i dati dal DTO al Model
        user.setUsername(userDto.getFirstName() + " " + userDto.getLastName());
        user.setEmail(userDto.getEmail());

        // Cifratura della password prima del salvataggio
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Assegnazione del ruolo di default ROLE_USER
        Role role = roleRepository.findByName("ROLE_USER");
        if (role == null) {
            // Se il ruolo non esiste nel DB, lo creiamo (gestione di emergenza)
            role = new Role();
            role.setName("ROLE_USER");
            roleRepository.save(role);
        }

        user.setRoles(List.of(role));
        userRepository.save(user);

        // Login automatico dopo la registrazione
        authenticateUserAndSetSession(user, userDto, request);
    }

    public void authenticateUserAndSetSession(User user, UserDto userDto, HttpServletRequest request) {
        try {
            CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    userDto.getPassword(),
                    userDetails.getAuthorities());

            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Salviamo il contesto di sicurezza nella sessione
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProfile(Long id, String gender, LocalDate birthDate) {
        User user = userRepository.findById(id).get();
        user.setGender(gender);
        user.setBirthDate(birthDate);
        userRepository.save(user);
    }

    @Override
    public void changePassword(Long id, String currentPassword, String newPassword) throws Exception {
        User user = userRepository.findById(id).get();
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("Password corrente non corretta.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null)
            return;

        String token = java.util.UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDate.now().plusDays(1));
        userRepository.save(user);

        emailService.sendSimpleEmail(
                email,
                "Reset password - Aulab Chronicle",
                "Clicca il link per resettare la password: http://localhost:8080/reset-password?token=" + token);
    }

    @Override
    public void resetPassword(String token, String newPassword) throws Exception {
        User user = userRepository.findByResetToken(token);
        if (user == null || user.getResetTokenExpiry().isBefore(LocalDate.now())) {
            throw new Exception("Token non valido o scaduto");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

}