package it.aulab.progetto_finale_docente.services;

import it.aulab.progetto_finale_docente.dtos.UserDto;
import it.aulab.progetto_finale_docente.models.Role;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.repositories.RoleRepository;
import it.aulab.progetto_finale_docente.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Iniezione tramite costruttore (best practice)
    public UserServiceImpl(UserRepository userRepository, 
                           RoleRepository roleRepository, 
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void saveUser(UserDto userDto) {
        User user = new User();
        
        // Mappiamo i dati dal DTO al Model
        // Dato che nel DB avevi 'username', qui usiamo firstName come username o una combinazione
        user.setUsername(userDto.getFirstName()); 
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
    }
}