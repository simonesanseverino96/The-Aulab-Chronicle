package it.aulab.progetto_finale_docente.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import it.aulab.progetto_finale_docente.repositories.ArticleRepository;
import it.aulab.progetto_finale_docente.repositories.CareerRequestRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class NotificationInterceptor implements HandlerInterceptor {

    private final CareerRequestRepository careerRequestRepository;
    private final ArticleRepository articleRepository;

    public NotificationInterceptor(CareerRequestRepository careerRequestRepository,
            ArticleRepository articleRepository) {
        this.careerRequestRepository = careerRequestRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

        if (modelAndView != null && request.isUserInRole("ROLE_ADMIN")) {
            int careerCount = careerRequestRepository.findByIsCheckedFalse().size();
            modelAndView.addObject("careerRequests", careerCount);

        }

        if (modelAndView != null && request.isUserInRole("ROLE_REVISOR")) {
            int revisorCount = articleRepository.findByIsAcceptedIsNull().size();
            modelAndView.addObject("articleToBeRevised", revisorCount);

        }
    }
}
