package it.aulab.progetto_finale_docente.services;

import java.time.LocalDate;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.aulab.progetto_finale_docente.dtos.UserDto;
import it.aulab.progetto_finale_docente.models.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    void updateProfile(Long id, String gender, LocalDate birthDate);

    void changePassword(Long id, String currentPassword, String newPassword) throws Exception;

    void deleteAccount(Long id);

    void sendPasswordResetEmail(String email);

    void resetPassword(String token, String newPassword) throws Exception;

    void saveUser(UserDto userDto, RedirectAttributes ra, HttpServletRequest req, HttpServletResponse res);

    User findUserByEmail(String email);

    User find(Long id);
}
