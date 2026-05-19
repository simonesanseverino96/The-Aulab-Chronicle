package it.aulab.progetto_finale_docente.services;

import java.security.Principal;
import java.util.List;

import jakarta.mail.Multipart;

public interface CrudService<ReadDto, Model, Key> {

    List <ReadDto> readAll();
    ReadDto read(Key key);
    ReadDto create(Model model, Principal principal, Multipart file);
    ReadDto update(Key key, Model model, Multipart file);
    void delete(Key key);

}
