package it.aulab.progetto_finale_docente.controllers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import it.aulab.progetto_finale_docente.repositories.ArticleRepository;
import it.aulab.progetto_finale_docente.repositories.CareerRequestRepository;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final ArticleRepository articleRepository;
    private final CareerRequestRepository careerRequestRepository;

    public GlobalControllerAdvice(ArticleRepository articleRepository,
            CareerRequestRepository careerRequestRepository) {
        this.articleRepository = articleRepository;
        this.careerRequestRepository = careerRequestRepository;
    }

    @ModelAttribute("articlesToBeRevised")
    public long articlesToBeRevised() {
        return articleRepository.countByIsAcceptedIsNull();
    }

    @ModelAttribute("careerRequests")
    public long careerRequests() {
        return careerRequestRepository.countByIsCheckedFalse();
    }
}
