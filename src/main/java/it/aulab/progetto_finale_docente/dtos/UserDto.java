package it.aulab.progetto_finale_docente.dtos;

import jakarta.validation.constraints.Pattern;
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

    
    @Size(min = 5, message = "Il nome è obbligatorio e deve avere almeno 5 caratteri")
    private String firstName;

    
    @Size(min = 5, message = "Il cognome è obbligatorio e deve avere almeno 5 caratteri")
    private String lastName;

    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "L'' email è obbligatoria, formato valido (es. mario@email.com)")
    private String email;

    @Size(min = 5, message = "La password è obbligatorio e deve avere almeno 5 caratteri")
    private String password;
}