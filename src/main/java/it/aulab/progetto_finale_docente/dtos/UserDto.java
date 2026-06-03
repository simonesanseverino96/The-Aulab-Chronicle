package it.aulab.progetto_finale_docente.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotEmpty(message = "Il nome non può essere vuoto")
    @Size(min = 4, message = "Il nome deve avere almeno 4 caratteri")
    private String firstName;

    @NotEmpty(message = "Il cognome non può essere vuoto")
    @Size(min = 4, message = "il cognome deve avere almeno 4 caratteri")
    private String lastName;

    @NotEmpty(message = "L'email non può essere vuota")
    @Email(message = "Inserisci un indirizzo email valido")
    private String email;

    @NotEmpty(message = "La password non può essere vuota")
    @Size(min = 4, message = "La password deve avere almeno 4 caratteri")
    private String password;
}