package it.aulab.progetto_finale_docente.services;

import java.security.Principal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface CrudService<DTO, MODEL, ID> {
    List<DTO> readAll();

    DTO read(ID key);

    DTO create(MODEL model, Principal principal, MultipartFile file); // Usa MultipartFile

    DTO update(ID key, MODEL model, MultipartFile file); // Usa MultipartFile

    void delete(ID key);
}
