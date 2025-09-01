// Marocco Stefano 762192 VA
// Marin Marco 760622 VA
// Gerti Alessia 762405 VA
package services;

import theknife.cache.DataManager;
import theknife.cache.JsonRepository;
import theknife.models.Risposta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servizio per la gestione delle risposte alle recensioni.
 * Fornisce metodi per creare, modificare, eliminare e cercare risposte.
 */
public class RisposteService {
    private final JsonRepository<Risposta> risposteRepository;

    /**
     * Costruttore che inizializza il repository delle risposte.
     */
    public RisposteService() {
        DataManager dataManager = DataManager.getInstance();
        // Registra il repository per le risposte
        dataManager.registerEntityRepository(Risposta.class, "data/risposte.json");
        this.risposteRepository = dataManager.getRepository(Risposta.class);
    }

    /**
     * Crea una nuova risposta per una recensione.
     *
     * @param recensioneId L'ID della recensione a cui rispondere
     * @param testo        Il testo della risposta
     * @return La risposta creata
     * @throws IllegalArgumentException se esiste già una risposta per questa recensione
     */
    public Risposta creaRisposta(String recensioneId, String testo) {
        if (esisteRispostaPerRecensione(recensioneId)) {
            throw new IllegalArgumentException("Esiste già una risposta per questa recensione");
        }

        Risposta risposta = new Risposta(recensioneId, testo);
        return risposteRepository.save(risposta);
    }

    /**
     * Modifica una risposta esistente.
     *
     * @param rispostaId L'ID della risposta da modificare
     * @param testo      Il nuovo testo della risposta
     * @return La risposta modificata, se trovata
     */
    public Optional<Risposta> modificaRisposta(String rispostaId, String testo) {
        Optional<Risposta> rispostaOpt = risposteRepository.findById(rispostaId);

        if (rispostaOpt.isPresent()) {
            Risposta risposta = rispostaOpt.get();
            risposta.modificaTesto(testo);
            return Optional.of(risposteRepository.save(risposta));
        }
        return Optional.empty();
    }

    /**
     * Elimina una risposta.
     *
     * @param rispostaId L'ID della risposta da eliminare
     * @return true se la risposta è stata eliminata, false altrimenti
     */
    public boolean eliminaRisposta(String rispostaId) {
        if (risposteRepository.findById(rispostaId).isPresent()) {
            risposteRepository.deleteById(rispostaId);
            return true;
        }
        return false;
    }

    /**
     * Elimina tutte le risposte associate a una recensione.
     *
     * @param recensioneId L'ID della recensione di cui eliminare le risposte
     * @return true se la risposta è stata eliminata, false se non esisteva
     */
    public boolean eliminaRispostaByRecensione(String recensioneId) {
        Optional<Risposta> rispostaOpt = getRispostaByRecensione(recensioneId);
        if (rispostaOpt.isPresent()) {
            return eliminaRisposta(rispostaOpt.get().getId());
        }
        return false;
    }

    /**
     * Ottiene la risposta associata a una recensione.
     *
     * @param recensioneId L'ID della recensione
     * @return La risposta, se trovata
     */
    public Optional<Risposta> getRispostaByRecensione(String recensioneId) {
        return risposteRepository.findAll().stream()
                .filter(r -> r.getRecensioneId().equals(recensioneId))
                .findFirst();
    }

    /**
     * Ottiene una risposta tramite il suo ID.
     *
     * @param rispostaId L'ID della risposta
     * @return La risposta, se trovata
     */
    public Optional<Risposta> getRispostaById(String rispostaId) {
        return risposteRepository.findById(rispostaId);
    }

    /**
     * Recupera tutte le risposte presenti nel sistema.
     *
     * @return Una lista di tutte le risposte
     */
    public List<Risposta> getAllRisposte() {
        return risposteRepository.findAll();
    }

    /**
     * Recupera tutte le risposte associate alle recensioni specificate.
     *
     * @param recensioniIds Lista di ID delle recensioni
     * @return Lista di risposte associate alle recensioni specificate
     */
    public List<Risposta> getRisposteByRecensioni(List<String> recensioniIds) {
        return risposteRepository.findAll().stream()
                .filter(r -> recensioniIds.contains(r.getRecensioneId()))
                .collect(Collectors.toList());
    }

    /**
     * Verifica se esiste una risposta per una determinata recensione.
     *
     * @param recensioneId ID della recensione
     * @return true se esiste una risposta, false altrimenti
     */
    public boolean esisteRispostaPerRecensione(String recensioneId) {
        return risposteRepository.findAll().stream()
                .anyMatch(r -> r.getRecensioneId().equals(recensioneId));
    }
}