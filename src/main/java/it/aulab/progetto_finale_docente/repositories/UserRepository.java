package it.aulab.progetto_finale_docente.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import it.aulab.progetto_finale_docente.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findByResetToken(String token);
}
