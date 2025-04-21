package services;

import com.example.cache.DataManager;
import com.example.cache.JsonRepository;
import com.example.models.Utente;

import java.util.Optional;

public class AuthService {
    private final JsonRepository<Utente> utenteRepository;

    public AuthService() {
        DataManager dataManager = DataManager.getInstance();
        // Registra il repository se non è già fatto
        dataManager.registerEntityRepository(Utente.class, "data/utenti.json");
        this.utenteRepository = dataManager.getRepository(Utente.class);
    }

    public boolean registraUtente(String username, String password, String email, Utente.Ruolo ruolo) {
        // Verifica che username o email non siano già in uso
        if (utenteRepository.findAll().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username) ||
                        u.getEmail().equalsIgnoreCase(email))) {
            return false; // Username o email già esistenti
        }

        // Crea e salva il nuovo utente
        Utente nuovoUtente = new Utente(username, password, email, ruolo);
        utenteRepository.save(nuovoUtente);
        return true;
    }

    public Optional<Utente> login(String usernameOrEmail, String password) {
        return utenteRepository.findAll().stream()
                .filter(u -> (u.getUsername().equals(usernameOrEmail) ||
                        u.getEmail().equals(usernameOrEmail)) &&
                        u.verificaPassword(password))
                .findFirst();
    }

    public Optional<Utente> getUtenteById(String id) {
        return utenteRepository.findById(id);
    }
}