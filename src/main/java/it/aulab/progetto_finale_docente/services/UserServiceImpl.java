package it.aulab.progetto_finale_docente.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // Iniezione tramite costruttore (best practice)
    public UserServiceImpl(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
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
}