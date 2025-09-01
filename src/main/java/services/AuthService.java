// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
package services;

import theknife.cache.DataManager;
import theknife.cache.JsonRepository;
import theknife.models.Utente;

import java.util.Optional;

/**
 * Servizio che gestisce l'autenticazione e la registrazione degli utenti.
 * Fornisce funzionalità per registrare nuovi utenti, eseguire il login
 * e recuperare informazioni sugli utenti.
 *
 * @author Stefano Marocco
 * @version 1.0
 */
public class AuthService {
    private final JsonRepository<Utente> utenteRepository;
    private RecensioneService recensioneService;
    private RistoranteService ristoranteService;

    /**
     * Costruttore che inizializza il servizio ottenendo l'istanza del repository degli utenti.
     * Registra il repository per gli utenti se non è già stato fatto in precedenza.
     */
    public AuthService() {
        DataManager dataManager = DataManager.getInstance();
        // Registra il repository se non è già fatto
        dataManager.registerEntityRepository(Utente.class, "data/utenti.json");
        this.utenteRepository = dataManager.getRepository(Utente.class);
    }

    /**
     * Imposta i servizi necessari per le operazioni di eliminazione a cascata.
     *
     * @param recensioneService servizio per la gestione delle recensioni
     * @param ristoranteService servizio per la gestione dei ristoranti
     */
    public void setServices(RecensioneService recensioneService, RistoranteService ristoranteService) {
        this.recensioneService = recensioneService;
        this.ristoranteService = ristoranteService;
    }

    /**
     * Registra un nuovo utente nel sistema.
     * Verifica che username ed email non siano già in uso prima di procedere.
     *
     * @param username Nome utente del nuovo account
     * @param password Password del nuovo account
     * @param email    Indirizzo email del nuovo account
     * @param ruolo    Ruolo dell'utente nel sistema
     * @return true se la registrazione ha avuto successo, false altrimenti
     */
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

    /**
     * Autentica un utente nel sistema.
     * L'utente può accedere utilizzando username o email insieme alla password.
     *
     * @param usernameOrEmail Username o email dell'utente
     * @param password        Password dell'utente
     * @return Optional contenente l'utente autenticato, o Optional vuoto se l'autenticazione fallisce
     */
    public Optional<Utente> login(String usernameOrEmail, String password) {
        return utenteRepository.findAll().stream()
                .filter(u -> (u.getUsername().equals(usernameOrEmail) ||
                        u.getEmail().equals(usernameOrEmail)) &&
                        u.verificaPassword(password))
                .findFirst();
    }

    /**
     * Recupera un utente dal sistema tramite il suo ID.
     *
     * @param id ID dell'utente da recuperare
     * @return Optional contenente l'utente trovato, o Optional vuoto se non esiste
     */
    public Optional<Utente> getUtenteById(String id) {
        return utenteRepository.findById(id);
    }

    /**
     * Elimina un utente e tutte le entità associate (recensioni, ristoranti, ecc.)
     *
     * @param utenteId ID dell'utente da eliminare
     * @return true se l'eliminazione ha avuto successo, false altrimenti
     */
    public boolean eliminaUtente(String utenteId) {
        if (recensioneService == null || ristoranteService == null) {
            throw new IllegalStateException("I servizi necessari non sono stati inizializzati");
        }

        Optional<Utente> utenteOpt = utenteRepository.findById(utenteId);
        if (!utenteOpt.isPresent()) {
            return false;
        }

        Utente utente = utenteOpt.get();

        // Se l'utente è un ristoratore, elimina tutti i suoi ristoranti
        if (utente.getRuolo() == Utente.Ruolo.RISTORATORE) {
            ristoranteService.eliminaRistorantiByProprietario(utenteId);
        }

        // Elimina tutte le recensioni dell'utente
        recensioneService.eliminaRecensioniByUtente(utenteId);

        // Elimina l'utente
        utenteRepository.deleteById(utenteId);

        return true;
    }
}