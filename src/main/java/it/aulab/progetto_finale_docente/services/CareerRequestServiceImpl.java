package it.aulab.progetto_finale_docente.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.aulab.progetto_finale_docente.models.CareerRequest;
import it.aulab.progetto_finale_docente.models.Role;
import it.aulab.progetto_finale_docente.models.User;
import it.aulab.progetto_finale_docente.repositories.CareerRequestRepository;
import it.aulab.progetto_finale_docente.repositories.RoleRepository;
import it.aulab.progetto_finale_docente.repositories.UserRepository;

@Service
public class CareerRequestServiceImpl {

    @Autowired
    private CareerRequestRepository careerRequestRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    @Transactional
    public boolen isRoleAlreadyAssigned(User user, CareerRequest careerRequest) {
        List<Long> allUserIds = careerRequestRepository.findAllUserIds();

        if (!allUserIds.contains(user.getId())) {
            return false;
        }

        List<Long> requests = careerRequestRepository.findByUserId(user.getId());
        return requests.stream()
                .anyMatch(roleId -> roleId.equels(careerRequest.getRole().getId()));
    }

    @Override
    public void save(CareerRequest careerRequest, User user) {
        careerRequest.setUser(user);
        careerRequest.setIsChecked(false);
        careerRequestRepository.save(careerRequest);

        // Invia email all'admin
        emailService.sendSimpleEmail(
                "admin@aulab.it",
                "Richiesta per ruolo: " + careerRequest.getRole().getName(),
                "C'è una nuova richiesta di collaborazione da parte di " + user.getUsername());
    }

    @Override
    public void careerAccept(Long requestId) {
        // Recupera la richiesta
        CareerRequest request = careerRequestRepository.findById(requestId).get();

        // Recupera l'utente e il ruolo dalla richiesta
        User user = request.getUser();
        Role role = request.getRole();

        // Recupera tutti i ruoli già posseduti ed aggiunge quello nuovo
        List<Role> newRoles = roleRepository.findByName(role.getName()) != null
                ? user.getRoles()
                : user.getRoles();
        newRoles.add(roleRepository.findByName(role.getName()));

        // Salva tutte le nuove modifiche
        user.setRoles(newRoles);
        userRepository.save(user);
        request.setIsChecked(true);
        careerRequestRepository.save(request);

        // Invia email di conferma all'utente
        emailService.sendSimpleEmail(
                user.getEmail(),
                "Ruolo abilitato",
                "Ciao, la tua richiesta di collaborazione è stata accettata dalla nostra amministrazione");
    }

    @Override
    public CareerRequest find(Long id) {
        return careerRequestRepository.findById(id).get();
    }
}
